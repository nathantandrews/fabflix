function handleLogout(resultData)
{
    if (resultData.status && resultData.status === "success")
    {
        sessionStorage.removeItem("user");
        window.location.replace("/index.html");
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
    jQuery.ajax({
        url: "../api/logout",
        method: 'POST',
        success: handleLogout,
        error: handleError
    });
}