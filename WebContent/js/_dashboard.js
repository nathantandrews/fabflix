let login_form = $("#login_form");
sessionStorage.clear();

function handleLoginResult(resultDataJson)
{

    let resultData = JSON.parse(resultDataJson);

    console.log("handle login response");
    console.log(resultData);
    console.log(resultData["status"]);

    // If login succeeds, it will redirect the user to editor.html
    if (resultData["status"] === "success")
    {
        let url = window.location.origin + "/fabflix/pages/editor";
        console.log(url);
        window.location.replace(url);
    }
    else
    {
        console.log("show error message");
        console.log(resultData["message"]);
        $("#login_error_message").text(resultData["message"]);
    }
}

function handleError(resultData)
{
    console.log("Error: " + resultData)
}

function submitLoginForm(formSubmitEvent)
{
    formSubmitEvent.preventDefault();
    let newURL = window.location.origin + "/fabflix/api/_dashboard";
    $.ajax({
        url: newURL,
        method: "POST",
        data: login_form.serialize(),
        success: handleLoginResult,
        error: handleError
    });
}

// Bind the submit action of the form to a handler function
login_form.submit(submitLoginForm);