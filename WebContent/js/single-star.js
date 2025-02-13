/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    target = target.replace(/[\[\]]/g, "\\$&");

    // Use regex to find parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Decode and return the parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API and populates HTML elements
 * @param resultData jsonObject
 */
function handleResult(resultData) {
    console.log("handleResult: Populating star and movie details");

    let starInfoElement = $("#star_info");
    starInfoElement.append(`
        <div class="section-title">Star Name</div>
        <div class="section-content">${resultData[0]["star_name"]}</div>
        <div class="section-title">Date of Birth</div>
        <div class="section-content">${resultData[0]["star_dob"] || 'N/A'}</div>
    `);

    let movieTableBodyElement = $("#movie_table_body");

    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = `
            <tr>
                <td><a href='single-movie.html?id=${resultData[i]["movie_id"]}'>${resultData[i]["movie_title"]}</a></td>
                <td>${resultData[i]["movie_year"]}</td>
                <td>${resultData[i]["movie_director"]}</td>
            </tr>
        `;
        movieTableBodyElement.append(rowHTML);
    }
}

let starId = getParameterByName('id');
$.ajax({
    dataType: "json",
    method: "GET",
    url: window.location.origin + "/Fabflix/api/single-star?id=" + starId,
    success: (resultData) => handleResult(resultData),
});

