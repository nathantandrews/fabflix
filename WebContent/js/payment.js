function fetchTotalPrice() {
    $.ajax({
        url: window.location.origin + '/Fabflix/api/cart',
        type: 'GET',
        dataType: 'json',
        success: function(data) {

            $('#totalPrice').text(`$${data.totalPrice.toFixed(2)}`);


            const movieTitles = data.cart.map(item => item.title);
            console.log("Movie Titles:", movieTitles);


            $('#paymentForm').data('movieTitles', movieTitles);
        },
        error: function(xhr, status, error) {
            console.error('Error fetching total price and movie titles:', error);
            console.log(xhr.responseText);
        }
    });
}

$('#paymentForm').on('submit', function(event) {
    event.preventDefault();


    const formData = $(this).serialize();
    const movieTitles = $(this).data('movieTitles');

    //VERY annoying to have had to do it this way... do not know why but this took while a long time to figure out
    const requestData = {
        ...JSON.parse('{"' + decodeURIComponent(formData).replace(/&/g, '","').replace(/=/g, '":"') + '"}'),
        movieTitles: movieTitles
    };

    $.ajax({
        url: window.location.origin + '/Fabflix/api/payment',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(requestData),
        dataType: 'json',
        success: function(data) {
            if (data.success) {
                alert("Payment successful! Your order is placed.");
                window.location.href = "confirmation.html"
            } else {
                let errorMessage = $('#error-message');
                errorMessage.text(data.message || "Payment failed! Please check your details and try again.");
                errorMessage.show();
            }
        },
        error: function(xhr, status, error) {
            console.error('Error:', error);
            console.log(xhr.responseText);
            alert("An error occurred. Please try again.");
        }
    });
});

$(document).ready(function() {
    fetchTotalPrice();
});