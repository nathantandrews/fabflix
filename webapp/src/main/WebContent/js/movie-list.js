let totalPages = 0;

let constraintSource = sessionStorage.getItem("lastMovieListURL");

if (constraintSource == null)
{
    constraintSource = "query";
}

let constraints = {};

function setDataConstraints(params, constraints)
{
    let dataParams=["title", "year", "director", "star", "genre", "keywords"];
    for (let i = 0; i < dataParams.length; i++)
    {
        let p = params.get(dataParams[i]);
        if (p != null)
        {
            p = p.trim();
            if (p.length != 0)
            {
                constraints[dataParams[i]] = p;
            }
        }
    }
}

function setDisplayConstraints(params, constraints)
{
    let displayParams=["page", "moviesPerPage", "sortBy", "action"];
    let defaultDisplayParams={"page":"1", "moviesPerPage":"10", "sortBy":"rating-desc-title-asc", "action":"movies"};
    for (let i = 0; i < displayParams.length; i++)
    {
        constraints[displayParams[i]] = params.get(displayParams[i]);
        if (constraints[displayParams[i]] == null)
        {
            constraints[displayParams[i]] = defaultDisplayParams[displayParams[i]];
            console.log(displayParams[i] + " updated to default: " + constraints[displayParams[i]]);
        }
    }
}

function getConstraints(constraints)
{
    if (constraintSource == "query")
    {
        let params = new URLSearchParams(window.location.search);
        setDataConstraints(params, constraints);
        setDisplayConstraints(params, constraints);
    }
    else
    {
        let params = new URLSearchParams(constraintSource.substring(constraintSource.lastIndexOf("?")));
        setDataConstraints(params, constraints);
        setDisplayConstraints(params, constraints);
    }
}

getConstraints(constraints);
// console.log("initial page constraints = " + JSON.stringify(constraints));

let sortOptions = $("#sort-options");
sortOptions.val(constraints["sortBy"]);
sortOptions.on("change", function (event)
{
    if (event.originalEvent !== undefined)
    {
        console.log("User triggered sortBy change");
        constraints["sortBy"] = $(this).val();
        constraints["page"] = "1";
        console.log("currentPage updated to default: 1");
        fetchMovies();
    }
    else
    {
        console.log("Programmatic sortBy change detected");
    }
});

console.log("Select showing moviesPerPage: " + constraints["moviesPerPage"]);
let moviesPerPageSelect = $("#moviesPerPageSelect");
moviesPerPageSelect.val(parseInt(constraints["moviesPerPage"]));
moviesPerPageSelect.on("change", function (event)
{
    if (event.originalEvent !== undefined)
    {
        console.log("User triggered moviesPerPage change");
        constraints["moviesPerPage"] = $(this).val().toString();
        constraints["page"] = "1";
        console.log("currentPage updated to default: 1");
        fetchMovies();
    }
    else
    {
        console.log("Programmatic moviesPerPage change detected");
    }
});

fetchMovies();

function fetchMovies()
{
    let lastURL = `movie-list.html${getQueryString(constraints)}`;
    // console.log("saving lastURL = " + lastURL);
    sessionStorage.setItem("lastMovieListURL", lastURL);
    let url = window.location.origin + "/fabflix/api/" + constraints["action"];
    $.ajax({
        url: url,
        method: "GET",
        data: constraints,
        success: (response) => {
            displayMovies(response["movieCount"], response["movies"]);
        },
        error: (xhr, status, error) => {
            console.error("Error fetching movies:", error);
        }
    });
}

function displayMovies(totalMovies, movies)
{
    let movieListElement = $("#movie_list");

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
            queryParamsDict["action"] = "browse";
            queryParamsDict["genre"] = genreId;
            return `<a onclick="sessionStorage.removeItem('lastMovieListURL');" href="movie-list.html${getQueryString(queryParamsDict)}">${g.split('(')[0]}</a>`;
        }).slice(0,3).join(", ");

        let starsList = movie.stars.split(',').map(s => {
            let starId = s.match(/\((.*?)\)/)[1];
            return `<a href="single-star.html?id=${starId}">${s.split('(')[0]}</a>`;
        }).slice(0, 3).join(", ");

        let color = getColorForRating(parseFloat(movie.rating));
        let relevance = Math.round(parseFloat(movie.relevance) * 100);

        return `
            <div class="movie-card">
                <h3 class="movie-title">
                    <a href="single-movie.html?id=${movie.id}">${movie.title}</a>
                </h3>
                <div>
                    <strong>Relevance:</strong> ${relevance}%
                </div>
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
    paginationControls(totalMovies);
}

function paginationControls(totalMovies)
{
    console.log("totalMovies = " + totalMovies + "; moviesPerPage = " + parseInt(constraints["moviesPerPage"]));
    totalPages = Math.ceil(totalMovies / parseInt(constraints["moviesPerPage"]));
    let totalPagesSpan = $("#totalPages");
    let currentPage = parseInt(constraints["page"]);
    totalPagesSpan.text(`Page ${currentPage} of ${totalPages}`);
    let prevPage = $("#prevPage");
    let nextPage = $("#nextPage");
    prevPage.prop("disabled", currentPage === 1);
    nextPage.prop("disabled", currentPage === totalPages);
}

function prevPageClicked()
{
    let currentPage = parseInt(constraints["page"]);
    if (currentPage > 1)
    {
        currentPage--;
        constraints["page"] = currentPage.toString();
        fetchMovies();
    }
}

function nextPageClicked()
{
    let currentPage = parseInt(constraints["page"]);
    if (currentPage < totalPages)
    {
        currentPage++;
        constraints["page"] = currentPage.toString();
        fetchMovies();
    }
}

function getColorForRating(rating)
{
    let hue = Math.round((rating / 10) * 120);
    return `hsl(${hue}, 100%, 40%)`;
}
