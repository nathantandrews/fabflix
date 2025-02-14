let editorElement = $("#editor");

function showDBMetaData()
{
    editorElement.empty();
    let url = window.location.origin + "/fabflix/api/metadata";
    $.ajax({
        url: url,
        method: "GET",
        success: (resultData) => {
            displayMetadata(resultData);
        },
        error: (xhr, status, error) => {
            console.error("Error fetching database metadata: ", error);
        }
    });
}

function displayMetadata(resultData)
{
    // console.log(typeof(resultData));
    let metadataElement = "";
    resultData.forEach( (table) => {
        metadataElement += `<h2>${table["tableName"]}</h2>
                            <br>
                            <table>
                            <tr>
                                <th>Column</th>
                                <th>Type</th>                            
                            </tr>`;
        table.columns.forEach((column) => {
            metadataElement += `<tr>
                                <td>${column["columnName"]}</td>
                                <td>${column["dataType"]}</td>
                                </tr>`;
        });
        metadataElement += `</table>`
        });
    editorElement.append(metadataElement);
}

function showAddStar()
{
    editorElement.empty();
    let url = window.location.origin + "/fabflix/pages/add-star"
    console.log("fetching " + url);
    $.get(url)
        .done((data) => {
            editorElement.append(data);
            $('#add-star-form').submit(submitAddStarForm);
        })
        .fail((error) => {
            console.error("Error loading add-star-element:", error);
        });
}

function submitAddStarForm(formSubmitEvent)
{
    formSubmitEvent.preventDefault();
    console.log("submitAddStarForm running");
    let newURL = window.location.origin + "/fabflix/api/add-star";
    $.ajax({
        url: newURL,
        method: "POST",
        data: $(this).serialize(),
        success: handleAddStarResult,
        error: handleError
    });
}

function handleAddStarResult(resultDataJson)
{
    // let resultData = JSON.parse(resultDataJson);
    let resultData = resultDataJson;
    console.log("handle add-star response");
    console.log(resultData);
    console.log(resultData[0]["status"]);
    console.log(resultData[0]["message"]);
    $("#status-message").text(resultData[0]["message"]);
}

function handleError(resultDataJson)
{
    // let resultData = JSON.parse(resultDataJson);
    let resultData = resultDataJson;

    console.log(resultData["status"]);
    console.log(resultData["message"])
    $("#status-message").text(resultData["message"]);
}

function showAddMovie()
{
    editorElement.empty();
    let url = window.location.origin + "/fabflix/pages/add-movie"
    console.log("fetching " + url);
    $.get(url)
        .done((data) => {
            editorElement.append(data);
            $('#add-movie-form').submit(submitAddMovieForm);
        })
        .fail((error) => {
            console.error("Error loading add-movie-element:", error);
        });
}

function submitAddMovieForm(formSubmitEvent)
{
    formSubmitEvent.preventDefault();
    console.log("submitAddMovieForm running");
    let newURL = window.location.origin + "/fabflix/api/add-movie";
    $.ajax({
        url: newURL,
        method: "POST",
        data: $(this).serialize(),
        success: handleAddMovieResult,
        error: handleError
    });
}

function handleAddMovieResult(resultDataJson)
{
    let resultData = JSON.parse(resultDataJson);
    console.log("handle add-movie response");
    console.log(resultData);
    console.log(resultData["status"]);
    console.log(resultData["message"]);
    $("#status-message").text(resultData["message"]);
}

function createNavbar()
{
    $(document).ready(() => {
        let navbar = $("<div>", { id: "navbar", class: "navbar" });
        $("body").prepend(navbar);

        // Fetch and insert navbar content
        let url = window.location.origin + "/fabflix/pages/employee-navbar"
        console.log("fetching " + url);
        $.get(url)
            .done((data) => {
                navbar.html(data);
            })
            .fail((error) => {
                console.error("Error loading navbar:", error);
            });
    });
}

createNavbar();

showDBMetaData();