let switchBtn = jQuery("#switcher-btn");
let switched = false;
switchBtn.append(`<button class="btn btn-primary" onClick="createBrowseOptions()">Browse by Genre</button>`);

let titleMenu = jQuery("#title-submenu");
console.log("before creating browse options");
createBrowseOptions();
console.log("after creating browse options");

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
        table += `<td><a onclick="searchForMovies('${genre.id}', 'g')">${genre.name}</a></td>`;
        if ((index + 1) % itemsPerRow === 0) {
            table += `</tr><tr>`;
        }
    });

    table += `</tr></tbody></table>`;
    titleMenu.append(table);
}

function searchForMovies(filter, type)
{
    let queryParams = [];
    switch (type)
    {
        case 'c':
            sessionStorage.setItem("title", filter);
            queryParams.push(`title=${encodeURIComponent(filter)}`);
            break;
        case 'g':
            sessionStorage.setItem("genre", filter);
            queryParams.push(`genre=${encodeURIComponent(filter)}`);
            break;
        default:
            console.warn("Invalid search type:", type);
            break;
    }
    const page = 1;
    const sortBy = "rating-desc-title-asc";
    const moviesPerPage = 10;
    queryParams.push(`moviesperpage=${encodeURIComponent(moviesPerPage)}`);
    queryParams.push(`sortby=${encodeURIComponent(sortBy)}`);
    queryParams.push(`page=${encodeURIComponent(page)}`);
    const queryString = queryParams.length > 0 ? `?${queryParams.join("&")}` : "";
    window.location.href = `movie-list.html${queryString}`;
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
            table += `<td><a onclick="searchForMovies('${characters[i]}', 'c')">${characters[i]}</a></td>`;
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
        jQuery.ajax
        ({
            url: "/api/browsing",
            method: "GET",
            success: (resultData) => handleResult(resultData),
            error: (resultData) => handleError(resultData)
        });
    }
    switched = !switched;
}

// document.addEventListener('DOMContentLoaded', () =>
// {
//     const checkoutButton = `
//         <div class="checkout">
//             <button onclick="window.location.href='shopping-cart.html'" class="btn btn-primary">Checkout</button>
//         </div>
//     `;
//     document.body.insertAdjacentHTML('afterbegin', checkoutButton);
// });
