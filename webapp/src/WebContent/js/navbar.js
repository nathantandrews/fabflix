function createNavbar()
{
    $(document).ready(() => {
        let navbar = $("<div>", { id: "navbar", class: "navbar" });
        $("body").prepend(navbar);

        // Fetch and insert navbar content
        let url = window.location.origin + "/fabflix/pages/navbar.html"
        // console.log("fetching " + url);
        $.get(url)
            .done((data) => {
                navbar.html(data);
            })
            .fail((error) => {
                console.error("Error loading navbar:", error);
            });
    });
}

function backToList()
{
    let url = sessionStorage.getItem("lastMovieListURL");
    if (!url)
    {
        url = `movie-list.html?action=movies&title=a&sortBy=rating-desc-title-asc&page=1&moviesPerPage=10`;
    }
    window.location.href = url;
}

function backToMain()
{
    window.location.href = "main-page.html";
}

function checkout()
{
    window.location.href='shopping-cart.html';
}

createNavbar();