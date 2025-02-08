let currentPage = 1;
let moviesPerPage = 10; // Default value
let movieListElement = jQuery("#movie_list");
let moviesPerPageSelect = jQuery("#moviesPerPageSelect");
let paginationElement = jQuery("#pagination");

moviesPerPageSelect.val(moviesPerPage);
moviesPerPageSelect.on("change", function ()
{
    moviesPerPage = parseInt(jQuery(this).val());
    currentPage = 1;
    fetchMovies();
});

function fetchMovies()
{
    let params = getQueryParams();
    params.moviesPerPage = moviesPerPage;
    params.page = currentPage;

    $.ajax({
        url: "/api/movie-list",
        method: "GET",
        data: params,
        success: (response) => {
            console.log(response);
            handleResult(response.movies, response.totalMovies);
        },
        error: (xhr, status, error) => {
            console.error("Error fetching movies:", error);
        }
    });
}

function handleResult(resultData, totalMovies)
{
    movieListElement.empty();

    if (resultData.length === 0)
    {
        movieListElement.append("<p>No movies found.</p>");
        return;
    }
    console.log(resultData);
    resultData.forEach((movie) =>
    {
        let genres = movie.genres.split(',').map(s =>
        {
            const re = new RegExp("\\((.*?)\\)");
            let genreId = re.exec(s)[1];
            return `<a href="movie-list.html?genre=${genreId}">${s.split('(')[0]}</a>`;
        });

        let starsList = movie.stars.split(',').map(s =>
        {
            const re = new RegExp("\\((.*?)\\)");
            let starId = re.exec(s)[1];
            return `<a href="single-star.html?id=${starId}">${s.split('(')[0]}</a>`;
        });

        let stars = starsList.slice(0, 3).join(", ");
        let color = getColorForRating(parseFloat(movie.rating));

        let movieCard = `
            <div class="movie-card">
                <h3 class="movie-title">
                    <a href="single-movie.html?id=${movie.id}">${movie.title}</a>
                </h3>
                <div><strong>Year:</strong> ${movie.year}</div>
                <div><strong>Director:</strong> ${movie.director}</div>
                <div><strong>Genres:</strong> ${genres.join(", ")}</div>
                <div><strong>Stars:</strong> ${stars}</div>
                <div style="color: white; background-color: ${color}; padding: 5px 10px; border-radius: 5px;">
                    <strong>Rating:</strong> ${movie.rating}
                </div>
                <button class="btn btn-primary add-to-cart" data-title="${movie.title}">Add to Cart</button>
            </div>
        `;
        movieListElement.append(movieCard);
    });

    paginationControls(totalMovies);
}

function paginationControls(totalMovies)
{
    paginationElement.empty();

    let totalPages = Math.ceil(totalMovies / moviesPerPage);
    if (totalPages <= 1) return;

    let prevDisabled = currentPage === 1 ? "disabled" : "";
    let nextDisabled = currentPage === totalPages ? "disabled" : "";

    paginationElement.append(`
        <button id="prevPage" class="btn btn-secondary" ${prevDisabled}>Previous</button>
        <span> Page ${currentPage} of ${totalPages} </span>
        <button id="nextPage" class="btn btn-secondary" ${nextDisabled}>Next</button>
    `);

    jQuery("#prevPage").on("click", function ()
    {
        if (currentPage > 1) {
            currentPage--;
            fetchMovies();
        }
    });

    jQuery("#nextPage").on("click", function ()
    {
        if (currentPage < totalPages) {
            currentPage++;
            fetchMovies();
        }
    });
}

function getQueryParams()
{
    let params = new URLSearchParams(window.location.search);
    return {
        title: params.get("title") || "",
        year: params.get("year") || "",
        director: params.get("director") || "",
        star: params.get("star") || "",
        genre: params.get("genre") || ""
    };
}


function getColorForRating(rating)
{
    const red = Math.min(255, Math.max(0, Math.floor((1 - rating / 10) * 255)));
    const green = Math.min(255, Math.max(0, Math.floor((rating / 10) * 255)));
    return `rgb(${red}, ${green}, 0)`;
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

document.addEventListener("DOMContentLoaded", () =>
{
    sessionStorage.setItem("lastMovieListURL", window.location.href);
});


fetchMovies();