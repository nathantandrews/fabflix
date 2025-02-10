/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */

/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {
    console.log("handleResult: populating movie info from resultData");

    let movieInfoElement = jQuery("#movie_info");
    console.log("Received result:", JSON.stringify(resultData, null, 2));

    let movieTitleSection = `
        <div class="section">
            <div class="section-title">Title</div>
            <div class="section-content">${resultData[0]["movie_title"]}</div>
        </div>
        <div class="section">
            <div class="section-title">Release Date</div>
            <div class="section-content">${resultData[0]["movie_year"]}</div>
        </div>
    `;
    movieInfoElement.append(movieTitleSection);

    let directorSection = `
        <div class="section">
            <div class="section-title">Director</div>
            <div class="section-content">${resultData[0]["movie_director"]}</div>
        </div>
    `;
    movieInfoElement.append(directorSection);

    let genres = resultData[0]["movie_genre"].split(',').map(g => {
        let genreId = g.match(/\((.*?)\)/)[1];
        let queryParamsDict = {};
        getDefaultConstraints(queryParamsDict);
        queryParamsDict["genre"] = genreId;
        sessionStorage.setItem("fromSearchOrBrowse", "true");
        return `<a href="movie-list.html${getQueryString(queryParamsDict)}">${g.split('(')[0]}</a>`;
    }).join(", ");
    sessionStorage.removeItem("genre");

    let genresSection = `
        <div class="section">
            <div class="section-title">Genres</div>
            <div class="section-content">${genres}</div>
        </div>
    `;
    movieInfoElement.append(genresSection);

    //.map is like a foreach. Adding all the stars
    let stars = resultData[0]["movie_stars"].split(',').map(s => {
        const re = new RegExp("\\((.*?)\\)");
        let id = re.exec(s)[1];
        return `<a href="single-star.html?id=${id}">${s.split('(')[0]}</a>`;
    }).join('<br>'); // Basically a "newline" character. Makes it so that there's a space between each line
    let starsSection = `
        <div class="section">
            <div class="section-title">Stars</div>
            <div class="section-content">${stars}</div>
        </div>
    `;
    movieInfoElement.append(starsSection);

    let rating = parseFloat(resultData[0]["movie_rating"]);
    let color = getColorForRating(rating);
    let ratingSection = `
        <div class="section">
            <div class="section-title">Rating</div>
            <div class="section-content" style="color: white; background-color: ${color}; padding: 7px 12px; border-radius: 7px;">
                ${rating}
            </div>
        </div>
    `;
    movieInfoElement.append(ratingSection);
}


// This makes the background color for rating. If it's 0, it'll be red, if its 10 it'll be green
function getColorForRating(rating)
{
    let hue = Math.round((rating / 10) * 120); // 0 (red) to 120 (green)
    return `hsl(${hue}, 100%, 40%)`;
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */
// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "/api/single-movie?id=" + getParameterByName('id'), // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});

