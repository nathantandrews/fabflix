let login_form = $("#login_form");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataJson jsonObject
 */
function handleLoginResult(resultDataJson)
{

    let resultData = JSON.parse(resultDataJson);

    console.log("handle login response");
    console.log(resultData);
    console.log(resultData["status"]);

    // If login succeeds, it will redirect the user to movie-list.html
    if (resultData["status"] === "success")
    {
        window.location.replace("html/main-page.html");
    }
    else
    {
        // If login fails, the web page will display
        // error messages on <div> with id "login_error_message"
        console.log("show error message");
        console.log(resultData["message"]);
        $("#login_error_message").text(resultData["message"]);
    }
}

function handleError(resultData)
{
    console.log(resultData)
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitLoginForm(formSubmitEvent) 
{
    console.log("submit login form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.ajax({
        url: "api/login",
        method: "POST",
        // Serialize the login form to the data sent by POST request
        data: login_form.serialize(),
        success: handleLoginResult,
        error: handleError
    });
}

// Bind the submit action of the form to a handler function
login_form.submit(submitLoginForm);