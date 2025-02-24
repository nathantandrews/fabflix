let titleElem = $("#title")

titleElem.autocomplete({
    // documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
        handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
        console.log(suggestion)
        handleSelectSuggestion(suggestion)
    },
    // set delay time
    deferRequestBy: 300,
    // there are some other parameters that you might want to use to satisfy all the requirements
    minChars: 3,

});

titleElem.keypress(function(event) {
    // keyCode 13 is the enter key
    if (event.keyCode === 13) {
        // pass the value of the input box to the handler function
        searchMovies()
    }
})

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

function handleLookup(query, doneCallback) {
    console.log("autocomplete initiated")

    let autoCompleteCache = getMapFromSession("autocomplete-cache");
    let results = autoCompleteCache.get(query);
    if (results == null)
    {
        console.log("sending AJAX request to backend Java Servlet")
        // sending the HTTP GET request to the Java Servlet endpoint movie-suggestion
        // with the query data
        jQuery.ajax({
            method: "GET",
            // generate the request url from the query.
            // escape the query string to avoid errors caused by special characters
            url: "/fabflix/api/movie-suggestion?query=" + escape(query),
            success: function(data) {
                // pass the data, query, and doneCallback function into the success handler
                console.log("autocomplete results from the database!")
                results = JSON.parse(data);
                handleLookupSuccess(results, query, doneCallback)
            },
            error: function(errorData) {
                console.log("lookup ajax error")
                console.log(errorData)
            }
        })
    }
    else
    {
        console.log("autocomplete results in the cache!");
        handleLookupSuccess(results, query, doneCallback);
    }
}


/*
 * This function is used to handle the ajax success callback function.
 * It is called by our own code upon the success of the AJAX request
 *
 * data is the JSON data string you get from your Java Servlet
 *
 */
function handleLookupSuccess(jsonData, query, doneCallback) {
    console.log("lookup successful: " + query + " = " + JSON.stringify(jsonData));

    let autoCompleteCache = getMapFromSession("autocomplete-cache");
    let results = autoCompleteCache.get(query);
    if (results == null)
    {
        autoCompleteCache.set(query, jsonData);
        storeMapInSession("autocomplete-cache", autoCompleteCache);
    }
    // call the callback function provided by the autocomplete library
    // add "{suggestions: jsonData}" to satisfy the library response format according to
    //   the "Response Format" section in documentation
    doneCallback( { suggestions: jsonData } );
}


/*
 * This function is the select suggestion handler function.
 * When a suggestion is selected, this function is called by the library.
 *
 * You can redirect to the page you want using the suggestion data.
 */
function handleSelectSuggestion(suggestion)
{
    window.location.href = 'single-movie.html?id=' + suggestion["data"];
    console.log("you select " + suggestion["value"] + " with ID " + suggestion["data"]);
}

function storeMapInSession(key, map) {
    const mapString = JSON.stringify(Array.from(map.entries()));
    sessionStorage.setItem(key, mapString);
}

function getMapFromSession(key) {
    const mapString = sessionStorage.getItem(key);
    if (mapString) {
        return new Map(JSON.parse(mapString));
    }
    return new Map();
}