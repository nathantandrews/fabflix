function searchMovies()
{
    let queryParamsDict = {};

    if (!getFormResults(queryParamsDict)) { return; }
    getDefaultConstraints(queryParamsDict);
    let queryParamsSet = new Set(Object.keys(queryParamsDict));
    let queryParamsList = getSearchKeys().filter(item => !queryParamsSet.has(item));
    queryParamsList.forEach((elem) => {
        sessionStorage.removeItem(elem);
    });


    let queryString = getQueryString(queryParamsDict);
    console.log("Query String:" + queryString);
    if (queryString)
    {
        sessionStorage.setItem("fromSearchOrBrowse", "true");
        window.location.href = `movie-list.html${queryString}`;
    }
}

function getFormResults(queryParamsDict)
{
    const title = document.getElementById("title").value.trim();
    const year = document.getElementById("year").value.trim();
    const director = document.getElementById("director").value.trim();
    const star = document.getElementById("star").value.trim();
    if (!title && !year && !director && !star)
    {
        console.log("show error message");
        $("#search_error_message").text("Please input a title, year, director, or star");
        return false;
    }
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
    return true;
}
