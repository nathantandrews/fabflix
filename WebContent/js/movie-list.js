let movieListElement = $("#movie_list");
let moviesPerPageSelect = $("#moviesPerPageSelect");
let sortOptions = $("#sort-options");

let totalMovies = 0;
let totalPages = 0;

// Grab session constraints
let currentPage = sessionStorage.getItem("page");
console.log("currentPage updated from session: " + currentPage);
if (currentPage == null)
{
    currentPage = 1;
}
currentPage = parseInt(currentPage);
console.log("currentPage parsed as int: " + currentPage);
let moviesPerPage = sessionStorage.getItem("moviesPerPage");
console.log("moviesPerPage updated from session: " + moviesPerPage);
if (moviesPerPage == null)
{
    moviesPerPage = 10;
    console.log("moviesPerPage updated to default: " + moviesPerPage);
}
moviesPerPage = parseInt(moviesPerPage);
console.log("moviesPerPage parsed as int: " + moviesPerPage);

let sortBy = sessionStorage.getItem("sortBy");
console.log("sortBy updated from session: " + sortBy);
if (sortBy == null)
{
    sortBy = "rating-desc-title-asc";
    console.log("sortBy updated to default: " + sortBy);
}

sortOptions.val(sortBy);
sortOptions.on("change", function (event)
{
    if (event.originalEvent !== undefined)
    {
        sessionStorage.setItem("fromSearchOrBrowse", "false");
        console.log("User triggered sort change");
        sessionStorage.setItem("sortBy", $(this).val());
        console.log("sortBy added to session: " + $(this).val());
        currentPage = 1;
        console.log("currentPage updated to default: 1");
        sessionStorage.setItem("page", "1");
        console.log("page added to session (set back to default): 1");
        fetchMovies();
    }
    else
    {
        console.log("Programmatic change detected");
    }
});

console.log("Select showing moviesPerPage: " + moviesPerPage);
moviesPerPageSelect.val(moviesPerPage);
moviesPerPageSelect.on("change", function (event)
{
    if (event.originalEvent !== undefined)
    {
        sessionStorage.setItem("fromSearchOrBrowse", "false");
        console.log("User triggered sort change");
        moviesPerPage = parseInt($(this).val());
        console.log("moviesPerPage parsed to int: " + moviesPerPage);
        sessionStorage.setItem("moviesPerPage", moviesPerPage);
        console.log("moviesPerPage added to session: " + moviesPerPage);
        currentPage = 1;
        console.log("currentPage updated to default: 1");
        sessionStorage.setItem("page", "1");
        console.log("page added to session (set back to default): 1");
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
    let lastURL = `movie-list.html${getQueryString(queryParamsDict)}`;
    sessionStorage.setItem("lastMovieListURL", lastURL);
    let url = window.location.origin + "/fabflix/api/movie-list"
    $.ajax({
        url: url,
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
            return `<a onclick="sessionStorage.setItem('fromSearchOrBrowse', 'true');" href="movie-list.html${getQueryString(queryParamsDict)}">${g.split('(')[0]}</a>`;
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
                <div>
                    <strong>Year:</strong> ${movie.year}
                </div>
                <div>
                    <strong>Director:</strong> ${movie.director}
                </div>
                <div>
                <strong>Genres:</strong> ${genres}
                </div>
                <div>
                <strong>Stars:</strong> ${starsList}
                </div>
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
    let totalPagesSpan = $("#totalPages");
    totalPagesSpan.text(`Page ${currentPage} of ${totalPages}`);
    let prevPage = $("#prevPage");
    let nextPage = $("#nextPage");
    prevPage.prop("disabled", currentPage === 1);
    nextPage.prop("disabled", currentPage === totalPages);
}

function prevPageClicked()
{
    if (currentPage > 1)
    {
        currentPage--;
        sessionStorage.setItem("fromSearchOrBrowse", "false");
        console.log("fromSearchOrBrowse added to session: false");
        sessionStorage.setItem("page", currentPage);
        console.log("page added to session: " + currentPage);
        fetchMovies();
    }
}

function nextPageClicked()
{
    if (currentPage < totalPages)
    {
        currentPage++;
        sessionStorage.setItem("fromSearchOrBrowse", "false");
        console.log("fromSearchOrBrowse added to session: false");
        sessionStorage.setItem("page", currentPage);
        console.log("page added to session: " + currentPage);
        fetchMovies();
    }
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
