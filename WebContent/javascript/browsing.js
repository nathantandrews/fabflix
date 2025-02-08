let switcher = jQuery("#switcher-btn");
switcher.click(() => createBrowseOptions(switcher));
let titleMenu = jQuery("#title-submenu");

function handleResult(resultData, type)
{
    let movieList = "<ul class='list-group'>";
    resultData.forEach(movie => {
        if (type === "genre")
        {
        movieList += `<li class='list-group-item'>
                            <a href='movie-list.html?genre=${movie.genreId}' class='text-decoration-none' style='color: #007bff;'>${movie.genreName}</a>
                        </li>`;
        }
        else if (type === "title")
        {
        movieList += `<li class='list-group-item'>
                    <a href='single-movie.html?id=${movie.id}' class='text-decoration-none' style='color: #007bff;'>${movie.title}</a>
                </li>`;
        }
    });
    movieList += "</ul>";
    jQuery("#movie-container").html(movieList);
}
function handleError(resultData)
{
    console.log("Error: " + resultData)
}

function handleGenreResult(resultData)
{
    resultData.forEach( (genre) =>
    {
        titleMenu.append(`<a onclick="prepareSearch(genre.id, 'g')">${genre.name}&nbsp</a>`);
    })
}

function prepareSearch(filter, type)
{
    let url = "";
    switch (type)
    {
        case 'c': url = `api/search?title=${filter}`; break;
        case 'g': url = `api/search?genre=${filter}`; break;
    }
    window.location.href = url;
}

function createBrowseOptions(btn)
{
    titleMenu.empty();
    if (btn.innerText === "Browse by Genre")
    {
        jQuery.ajax
        ({
            url: "api/browsing",
            method: "GET",
            success: (resultData) => handleGenreResult(resultData),
            error: (resultData) => handleError(resultData)
        });
    }
    else if (btn.innerText === "Browse by Character")
    {
        const characters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ*";
        for (let char of characters)
        {
            titleMenu.append(`<a onclick="prepareSearch('${char}', 'c')">${char}</a>`);
        }
    }
    // toggle the button
    let btnText = btn.innerText;
    btn.innerText = (btnText === 'Browse by Genre') ? 'Browse by Character' : 'Browse by Genre';
}

createBrowseOptions(switcher);
    // document.addEventListener('DOMContentLoaded', () =>
    // {
    //     const checkoutButton = `
    //         <div class="checkout">
    //             <button onclick="window.location.href='shopping-cart.html'" class="btn btn-primary">Checkout</button>
    //         </div>
    //     `;
    //     document.body.insertAdjacentHTML('afterbegin', checkoutButton);
    // });
