let switchBtn = $("#switcher-btn");
let titleMenu = $("#title-submenu");


let switched = false;
switchBtn.append(`<button class="btn btn-primary" onClick="createBrowseOptions()">Browse by Genre</button>`);

createBrowseOptions();

function handleError(resultData)
{
    console.log("Error: " + resultData)
}

function handleResult(resultData)
{
    // console.log(resultData);
    resultData = JSON.parse(resultData);
    // Create a table structure
    let table = `<table class="table table-striped"><tbody><tr>`;
    let itemsPerRow = 5;
    resultData.forEach((genre, index) => {
        table += `<td><a onclick="searchMovies('${genre.id}', 'g')">${genre.name}</a></td>`;
        if ((index + 1) % itemsPerRow === 0) {
            table += `</tr><tr>`;
        }
    });

    table += `</tr></tbody></table>`;
    titleMenu.append(table);
}

function searchMovies(filter, type)
{
    let queryParamsDict = {};
    switch (type)
    {
        case 'c':
            queryParamsDict["title"] = filter;
            break;
        case 'g':
            queryParamsDict["genre"] = filter;
            break;
        default:
            console.warn("Invalid search type:", type);
            break;
    }
    getDefaultConstraints(queryParamsDict);
    let queryParamsSet = new Set(Object.keys(queryParamsDict));
    let queryParamsList = getSearchKeys().filter(item => !queryParamsSet.has(item));
    queryParamsList.forEach((elem) => {
        sessionStorage.removeItem(elem);
    });

    let queryString = getQueryString(queryParamsDict);
    console.log("Query String:" + queryString);
    if (queryString !== "")
    {
        sessionStorage.setItem("fromSearchOrBrowse", "true");
        window.location.href = `movie-list.html${queryString}`;
    }
    else
    {
        console.error("Empty Query String")
    }
}

function createBrowseOptions()
{
    titleMenu.empty();
    switchBtn.empty();
    if (!switched)
    {
        switchBtn.append(`<button class="btn btn-primary" onClick="createBrowseOptions()">Browse by Genre</button>`);
        const characters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ*";
        let table = `<table class="table table-striped"><tbody><tr>`;
        const itemsPerRow = 10;
        for (let i = 0; i < characters.length; ++i)
        {
            table += `<td><a onclick="searchMovies('${characters[i]}', 'c')">${characters[i]}</a></td>`;
            if ((i + 1) % itemsPerRow === 0) {
                table += `</tr><tr>`;
            }
        }
        table += `</tr></tbody></table>`;
        titleMenu.append(table);
    }
    else
    {
        switchBtn.append(`<button class="btn btn-primary" onClick="createBrowseOptions()">Browse by Title</button>`);
        $.ajax
        ({
            url: "/api/browsing",
            method: "GET",
            success: (resultData) => handleResult(resultData),
            error: (resultData) => handleError(resultData)
        });
    }
    switched = !switched;
}

