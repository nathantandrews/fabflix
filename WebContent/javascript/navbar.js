function createNavbar()
{
    document.addEventListener('DOMContentLoaded', () =>
    {
        console.log("createNavbar running")
        const navbar = document.createElement('div');
        navbar.id = "nav-placeholder";
        navbar.classList.add("nav-placeholder");
        document.body.insertAdjacentElement('afterbegin', navbar);
        fetch("../html/navbar.html")
            .then(response => response.text())
            .then(data => {
                navbar.innerHTML = data;
            })
            .catch(error => console.error("Error loading navbar:", error));
    });
}
function backToList()
{
    window.location.href = sessionStorage.getItem("lastMovieListURL");
}

createNavbar();