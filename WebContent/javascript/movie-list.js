// Set jQuery elements
let movieListElement = jQuery("#movie_list");
let moviesPerPageSelect = jQuery("#moviesPerPageSelect");
let sortOptions = jQuery("#sort-options");

let totalMovies = 0;
let totalPages = 0;

// Grab session constraints
let currentPage = sessionStorage.getItem("page");
if (currentPage == null)
{
    currentPage = 1;
}
currentPage = parseInt(currentPage);
let moviesPerPage = sessionStorage.getItem("moviesPerPage");
if (moviesPerPage == null)
{
    moviesPerPage = 10;
}
moviesPerPage = parseInt(moviesPerPage);

let sortBy = sessionStorage.getItem("sortBy");
if (sortBy == null)
{
    sortBy = "rating-desc-title-asc";
}

// set the selected sortOptions
sortOptions.val(sortBy);

// On change in the sort options, set sortBy to new option
// save it to the session
sortOptions.on("change", function (event)
{
    if (event.originalEvent !== undefined)
    {
        console.log("User triggered sort change");
        sessionStorage.setItem("sortBy", jQuery(this).val());
        currentPage = 1;
        sessionStorage.setItem("page", currentPage);
        fetchMovies();
    }
    else
    {
        console.log("Programmatic change detected");
    }
});


moviesPerPageSelect.val(moviesPerPage);
moviesPerPageSelect.on("change", function (event)
{
    if (event.originalEvent !== undefined)
    {
        console.log("User triggered sort change");
        moviesPerPage = parseInt(jQuery(this).val());
        sessionStorage.setItem("moviesPerPage", moviesPerPage);
        currentPage = 1;
        sessionStorage.setItem("page", currentPage);
        fetchMovies();
    }
    else
    {
        console.log("Programmatic change detected");
    }
});

document.addEventListener("DOMContentLoaded", () =>
{
    sessionStorage.setItem("lastMovieListURL", window.location.href);
});

fetchMovies();

function fetchMovies()
{
    let queryParamsDict = {};
    getQueryParams(queryParamsDict);
    let url = `movie-list.html${getQueryString(queryParamsDict)}`;
    console.log(url);
    sessionStorage.setItem("lastMovieListURL", url);
    $.ajax({
        url: "/api/movie-list",
        method: "GET",
        data: queryParamsDict,
        success: (response) => {
            totalMovies = response["totalMovies"];
            displayMovies(response["movies"]);
        },
        error: (xhr, status, error) => {
            console.error("Error fetching movies:", error);
        }
    });
}

function displayMovies(movies)
{
    movieListElement.empty();
    if (movies.length === 0) {
        movieListElement.append("<p>No movies found.</p>");
        return;
    }

    let movieCards = movies.map(movie => {
        let genres = movie.genres.split(',').map(g => {
            let genreId = g.match(/\((.*?)\)/)[1];

            let queryParamsDict = {};
            getDefaultConstraints(queryParamsDict);
            queryParamsDict["genre"] = genreId;
            sessionStorage.setItem("fromSearchOrBrowse", "true");
            return `<a href="movie-list.html${getQueryString(queryParamsDict)}">${g.split('(')[0]}</a>`;
        }).slice(0,3).join(", ");
        sessionStorage.removeItem("genre");

        let starsList = movie.stars.split(',').map(s => {
            let starId = s.match(/\((.*?)\)/)[1];
            return `<a href="single-star.html?id=${starId}">${s.split('(')[0]}</a>`;
        }).slice(0, 3).join(", ");

        let color = getColorForRating(parseFloat(movie.rating));

        return `
            <div class="movie-card">
                <h3 class="movie-title">
                    <a href="single-movie.html?id=${movie.id}">${movie.title}</a>
                </h3>
                <div><strong>Year:</strong> ${movie.year}</div>
                <div><strong>Director:</strong> ${movie.director}</div>
                <div><strong>Genres:</strong> ${genres}</div>
                <div><strong>Stars:</strong> ${starsList}</div>
                <br/>
                <div style="color: white; background-color: ${color}; padding: 5px 10px; border-radius: 5px; width: 200px; height: 37.6px; align-content: center; margin: 0 auto;">
                    <strong>Rating:</strong> ${movie.rating}
                </div>
                <br/>
                <button class="btn btn-primary add-to-cart" data-title="${movie.title}">Add to Cart</button>
            </div>
        `;
    }).join("");

    movieListElement.append(movieCards);
    paginationControls();
}

function paginationControls()
{
    totalPages = Math.ceil(totalMovies / moviesPerPage);
    let totalPagesSpan = jQuery("#totalPages");
    totalPagesSpan.text(`Page ${currentPage} of ${totalPages}`);
    let prevPage = jQuery("#prevPage");
    let nextPage = jQuery("#nextPage");
    prevPage.prop("disabled", currentPage === 1);
    nextPage.prop("disabled", currentPage === totalPages);
}

function prevPageClicked()
{
    if (currentPage > 1)
    {
        currentPage--;
        sessionStorage.setItem("fromSearchOrBrowse", "false");
        sessionStorage.setItem("page", currentPage);
        fetchMovies();
    }
}

function nextPageClicked()
{
    if (currentPage < totalPages)
    {
        currentPage++;
        sessionStorage.setItem("fromSearchOrBrowse", "false");
        sessionStorage.setItem("page", currentPage);
        fetchMovies();
    }
}

function getQueryString(queryParamsDict)
{
    let queryParams = [];
    for (const [key, value] of Object.entries(queryParamsDict)) {
        if (value !== null && value !== sessionStorage.getItem(key)) {
            sessionStorage.setItem(key, value.toString());
        }
        if (value !== null) {
            queryParams.push(`${key}=${encodeURIComponent(value.toString())}`);
        }
    }
    return queryParams.length ? `?${queryParams.join("&")}` : "";
}

function getQueryParams(queryParamsDict)
{
    let fromSearchOrBrowse= sessionStorage.getItem("fromSearchOrBrowse");
    if (fromSearchOrBrowse === "false")
    {
        queryParamsDict["title"] = sessionStorage.getItem("title");
        queryParamsDict["year"] = sessionStorage.getItem("year");
        queryParamsDict["director"] = sessionStorage.getItem("director");
        queryParamsDict["star"] = sessionStorage.getItem("star");
        queryParamsDict["genre"] = sessionStorage.getItem("genre");
        queryParamsDict["sortBy"] = sessionStorage.getItem("sortBy");
        queryParamsDict["page"] = sessionStorage.getItem("page");
        queryParamsDict["moviesPerPage"] = sessionStorage.getItem("moviesPerPage");
    }
    else
    {
        let params = new URLSearchParams(window.location.search);
        let title = params.get("title");
        let year = params.get("year");
        let director = params.get("director");
        let star = params.get("star");
        let genre = params.get("genre");
        if (title && title.length !== 0)
        {
            queryParamsDict["title"] = title;
        }
        if (year && year.length !== 0)
        {
            queryParamsDict["year"] = year;
        }
        if (director && director.length !== 0)
        {
            queryParamsDict["director"] = director;
        }
        if (star && star.length !== 0)
        {
            queryParamsDict["star"] = star;
        }
        if (genre && genre.length !== 0)
        {
            queryParamsDict["genre"] = genre;
        }
        queryParamsDict["sortBy"] = params.get("sortBy");
        queryParamsDict["page"] = params.get("page");
        queryParamsDict["moviesPerPage"] = params.get("moviesPerPage");
    }
}

function getColorForRating(rating)
{
    let hue = Math.round((rating / 10) * 120);
    return `hsl(${hue}, 100%, 40%)`;
}


// jQuery(document).on("click", ".add-to-cart", function()
// {
//     let title = jQuery(this).data("title");
//     showModal(title);
// });
//
// function showModal(title)
// {
//     jQuery("#quantityModal").data("movieTitle", title);
//     jQuery("#quantityModal").modal("show");
// }
//
// jQuery("#submitQuantity").on("click", function()
// {
//     let title = jQuery("#quantityModal").data("movieTitle");
//     let quantity = jQuery("#quantityInput").val();
//
//     if (quantity < 1) {
//         alert("Quantity must be at least 1");
//     } else {
//         addToCart(title, quantity);
//     }
// });
//
// function addToCart(title, quantity)
// {
//     $.ajax({
//         url: "api/index",
//         method: "POST",
//         data:
//             {
//                 item: title,
//                 quantity: quantity,
//                 action: "add"
//             },
//         success: () => {
//             alert(`Added ${quantity} of ${title} to cart!`);
//             jQuery("#quantityModal").modal("hide");
//         },
//         error: (xhr, status, error) => {
//             console.error("Error adding movie to cart:", error);
//         }
//     });
// }