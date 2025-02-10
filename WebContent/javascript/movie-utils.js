function getQueryString(queryParamsDict)
{
    let queryParams = [];
    for (const [key, value] of Object.entries(queryParamsDict))
    {
        if (value !== null && value !== sessionStorage.getItem(key))
        {
            sessionStorage.setItem(key, value.toString());
        }
        if (value !== null) {
            queryParams.push(`${key}=${encodeURIComponent(value.toString())}`);
        }
    }
    return queryParams.length ? `?${queryParams.join("&")}` : "";
}

function getDefaultConstraints(queryParamsDict)
{
    const sortBy = "rating-desc-title-asc";
    const moviesPerPage = 10;
    const page = 1;
    queryParamsDict["sortBy"] = sortBy;
    queryParamsDict["moviesPerPage"] = moviesPerPage;
    queryParamsDict["page"] = page;
}

function getSearchKeys()
{
    return [
        "title",
        "year",
        "director",
        "star",
        "genre"
    ];
}