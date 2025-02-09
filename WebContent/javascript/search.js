function getFormResults(queryDict)
{
    const title = document.getElementById("title").value.trim();
    const year = document.getElementById("year").value.trim();
    const director = document.getElementById("director").value.trim();
    const star = document.getElementById("star").value.trim();
    if (!title && !year && !director && !star)
    {
        console.log("show error message");
        jQuery("#search_error_message").text("Please input a title, year, director, or star");
        return false;
    }
    if (title && title.length !== 0)
    {
        queryDict["title"] = title;
    }
    if (year && year.length !== 0)
    {
        queryDict["year"] = year;
    }
    if (director && director.length !== 0)
    {
        queryDict["director"] = director;
    }
    if (star && director.length !== 0)
    {
        queryDict["star"] = star;
    }
    return true;
}

function getDefaultConstraints(queryDict)
{
    const page = 1;
    const sortBy = "rating-desc-title-asc";
    const moviesPerPage = 10;
    queryDict["page"] = page;
    queryDict["sortBy"] = sortBy;
    queryDict["moviesPerPage"] = moviesPerPage;
}

function searchMovies()
{
    let queryDict = {};

    if (!getFormResults(queryDict)) { return; }
    getDefaultConstraints(queryDict);

    let queryString = getQueryString(queryDict);
    console.log("Query String:" + queryString);
    if (queryString)
    {
        window.location.href = `movie-list.html${queryString}`;
    }
}

function getQueryString(queryParamsDict)
{
    let queryParams = [];
    for (const [key, value] of Object.entries(queryParamsDict))
    {
        sessionStorage.setItem(key, value.toString());
        queryParams.push(`${key}=${encodeURIComponent(value.toString())}`);
    }
    if (queryParams.length === 0)
    {
        return "";
    }
    return queryParams.length > 0 ? `?${queryParams.join("&")}` : "";
}