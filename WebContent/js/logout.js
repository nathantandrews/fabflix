function handleLogout(resultData)
{
    if (resultData.status && resultData.status === "success")
    {
        sessionStorage.removeItem("user");
        window.location.replace(window.location.origin + "/Fabflix/index.html");
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
    $.ajax({
        url: window.location.origin + "/Fabflix//api/logout",
        method: 'POST',
        success: handleLogout,
        error: handleError
    });
}