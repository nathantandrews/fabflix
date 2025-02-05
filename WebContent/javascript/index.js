/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */


/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleResult(resultData) {
    console.log("handleResult: populating movie list from resultData");

    let movieListElement = jQuery("#movie_list");

    resultData.forEach((movie) => {

        //Early exit if more than 20 movies

        // .split(',').slice(0, 3).join(', ')

        //JS magic with .map (foreach loop essentially)
        // let stars = movie["movie_stars"]
        //     .split(',')
        //     .slice(0, 3)
        //     .map(s => {
        //         const re = new RegExp("\\((.*?)\\)");
        //         let id = re.exec(s)?.[1];
        //         return `<a href="single-star.html?id=${id}">${s.split('(')[0]}</a>`;
        //     })
        //     .join(', ');

        let stars = "";
        const movie_stars_ids = movie["movie_stars_ids"].split(", ");
        const movie_stars_names = movie["movie_stars_names"].split(", ");
        for (let i = 0; i < movie_stars_ids.length && i < 3; ++i)
        {
            if (i > 0)
            {
                stars += ', &nbsp;';
            }
            stars += '<a href=' + movie_stars_ids[i] + '"single-star.html?id=">'
                + movie_stars_names[i] +     // display star_name for the link text
                '</a>'
        }
        let color = getColorForRating(parseFloat(movie["movie_rating"]));

        // This will create one of the "cards" for each movie, the cards is the white box that contains the title href, director etc
        let movieCard = `
            <div class="movie-card">
                <h3 class="movie-title">
                    <a href="html/single-movie.html?id=${movie['movie_id']}">${movie['movie_title']}</a>
                </h3>
                <div><strong>Year:</strong> ${movie["movie_year"]}</div>
                <div><strong>Director:</strong> ${movie["movie_director"]}</div>
                <div><strong>Genres:</strong> ${movie["movie_genres"]}</div>
                <div><strong>Stars:</strong> ${stars}</div>
                <div style="color: white; background-color: ${color}; padding: 5px 10px; border-radius: 5px;">
                    <strong>Rating:</strong> ${movie["movie_rating"]}
                </div>
            </div>
        `;

        movieListElement.append(movieCard);
    });
}
function handleError(resultData) {
    console.log(resultData)
}
// Fetch movie data from the API
jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "/Fabflix_war/api/movie-list",
    success: handleResult,
    error: handleError
});

/**
 * Function to determine the background color for the rating badge.
 * @param rating
 * @returns {string} Hex color code
 */
function getColorForRating(rating) {
    const red = Math.min(255, Math.max(0, Math.floor((1 - rating / 10) * 255)));
    const green = Math.min(255, Math.max(0, Math.floor((rating / 10) * 255)));
    return `rgb(${red}, ${green}, 0)`;
}
