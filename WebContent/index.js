/**
 * Handles the data returned by the API, reads the JSON object, and populates the data into HTML elements.
 * @param resultData jsonObject
 */
function handleResult(resultData) {
    console.log("handleResult: populating movie list from resultData");

    let movieListElement = jQuery("#movie_list");

    resultData.forEach((movie, index) => {

        //Early exit if more than 20 movies
        if (index >= 20) return;

        let genres = movie["movie_genre"].split(',').slice(0, 3).join(', ');

        //JS magic with .map (foreach loop essentially)
        let stars = movie["movie_stars"]
            .split(',')
            .slice(0, 3)
            .map(s => {
                const re = new RegExp("\\((.*?)\\)");
                let id = re.exec(s)?.[1];
                return `<a href="single-star.html?id=${id}">${s.split('(')[0]}</a>`;
            })
            .join(', ');

        let color = getColorForRating(parseFloat(movie["movie_rating"]));

        // This will create one of the "cards" for each movie, the cards is the white box that contains the title href, director etc
        let movieCard = `
            <div class="movie-card">
                <h3 class="movie-title">
                    <a href="single-movie.html?id=${movie['movie_id']}">${movie['movie_title']}</a>
                </h3>
                <div><strong>Year:</strong> ${movie["movie_year"]}</div>
                <div><strong>Director:</strong> ${movie["movie_director"]}</div>
                <div><strong>Genres:</strong> ${genres}</div>
                <div><strong>Stars:</strong> ${stars}</div>
                <div style="color: white; background-color: ${color}; padding: 5px 10px; border-radius: 5px;">
                    <strong>Rating:</strong> ${movie["movie_rating"]}
                </div>
            </div>
        `;

        movieListElement.append(movieCard);
    });
}

// Fetch movie data from the API
jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/movie-list",
    success: handleResult,
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
