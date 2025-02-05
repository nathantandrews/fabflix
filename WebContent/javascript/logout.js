function handleLogout(resultData)
{
    if (resultData.status && resultData.status === "success")
    {
        sessionStorage.removeItem("user");
        window.location.href = "login.html";
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

// document.addEventListener("DOMContentLoaded", () => {
//     const buttonContainer = document.createElement("div");
//     buttonContainer.classList.add("d-flex", "gap-2", "mt-3");
//
//     function createBackButton(text, storageKey) {
//         const lastURL = localStorage.getItem(storageKey);
//         if (lastURL) {
//             const button = document.createElement("button");
//             button.textContent = text;
//             button.classList.add("btn", "btn-primary", "mr-2");
//
//             button.onclick = () => {
//                 window.location.href = lastURL;
//             };
//
//             buttonContainer.appendChild(button);
//         }
//     }
//
//     createBackButton("Back to Movie List", "lastMovieListURL");
//     createBackButton("Back to Single Star", "lastSingleStar");
//     createBackButton("Back to Single Movie", "lastSingleMovie");
//
//     if (buttonContainer.children.length > 0) {
//         const body = document.querySelector("body");
//         body.insertBefore(buttonContainer, body.firstChild);
//     }
// });