let numItems = 0;
function proceedToPayment()
{
    if (numItems !== 0)
    {
        window.location.href = 'payment.html';
    }
    else
    {
        displayError();
    }
}

function displayError()
{
    let errorElement = $("#empty-error-message");
    errorElement.empty();
    errorElement.append(`Your Cart is empty...`);
}

function fetchCart()
{
    $.ajax({
        url: window.location.origin + '/Fabflix/api/cart',
        method: 'GET',
        success: function (data)
        {
            renderCart(data["cart"] || []);
        },
        error: function (xhr, status, error)
        {
            console.error("Error fetching cart:", error);
        }
    });
}

function renderCart(cart)
{
    let cartBody = $("#cartBody");
    cartBody.empty();
    let totalPrice = 0;
    numItems = 0;
    cart.forEach(item => {
        let price = item["cost"];
        totalPrice += price * item.quantity;
        ++numItems;

        let row = `
                <tr>
                    <td>${item.title}</td>
                    <td>
                        <input type="number" class="quantity-input" min="0" value="${item.quantity}" data-title="${item.title}">
                    </td>
                    <td>$${(price * item.quantity).toFixed(2)}</td>
                    <td>
                        <button class="btn btn-primary update-btn" data-title="${item.title}">Update</button>
                    </td>
                </tr>
            `;
        cartBody.append(row);
    });

    $("#totalPrice").text(totalPrice.toFixed(2));
}

function updateCart(title, quantity)
{
    $.ajax({
        url: window.location.origin + "/Fabflix/api/cart",
        method: "POST",
        data: {
            title: title,
            quantity: quantity,
            action: quantity === 0 ? "remove" : "update"
        },
        success: function () {
            fetchCart();
        },
        error: function (xhr, status, error) {
            console.error("Error updating cart:", error);
        }
    });
}

$(document).ready(fetchCart);

$(document).on("click", ".update-btn", function ()
{
    let title = $(this).data("title");
    let quantity = $(this).closest("tr").find(".quantity-input").val();
    quantity = parseInt(quantity);

    if (isNaN(quantity) || quantity < 0) {
        alert("Invalid quantity");
        return;
    }

    updateCart(title, quantity);
});
