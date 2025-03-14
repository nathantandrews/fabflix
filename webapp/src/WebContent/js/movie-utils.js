function getQueryString(queryParamsDict)
{
    let queryParams = [];
    for (const [key, value] of Object.entries(queryParamsDict))
    {
        if (value !== null) {
            queryParams.push(`${key}=${encodeURIComponent(value.toString())}`);
        }
    }
    return queryParams.length ? `?${queryParams.join("&")}` : "";
}

function getDefaultConstraints(queryParamsDict)
{
    const sortBy = "rating-desc-title-asc";
    const moviesPerPage = "10";
    const page = "1";
    queryParamsDict["sortBy"] = sortBy;
    queryParamsDict["moviesPerPage"] = moviesPerPage;
    queryParamsDict["page"] = page;
}

function addToCart(title)
{
    let url = window.location.origin + "/fabflix/api/cart";
    $.ajax({
        url: url,
        method: "POST",
        data: {
            title: title,
            action: "add",
            quantity: 1
        },
        success: () => {
            console.log(`Added ${title} to cart!`);
            alert(`Added ${title} to cart!`);
        },
        error: (xhr, status, error) => {
            console.error("Error adding movie to cart: ", error);
        }
    });
}

document.addEventListener("click", function (event)
{
    if (event.target.classList.contains("add-to-cart"))
    {
        const movieTitle = event.target.getAttribute("data-title");
        addToCart(movieTitle);
    }
});
