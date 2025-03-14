function handleLogout(resultData)
{
    if (resultData.status && resultData.status === "success")
    {
        sessionStorage.clear();
        let url = window.location.origin + "/fabflix/_dashboard/";
        window.location.replace(url);
    }
    else
    {
        console.error("Logout failed: Unexpected response", resultData)
    }
}

function handleError(resultData)
{
    console.error("Logout failed:", resultData);
}

function logout()
{
    let url = window.location.origin + "/fabflix/api/logout"; // uses main shared logout servlet
    $.ajax({
        url: url,
        method: 'POST',
        success: handleLogout,
        error: handleError
    });
}