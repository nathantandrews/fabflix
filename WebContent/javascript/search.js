document.getElementById("searchForm").addEventListener("submit", function(event) {
    event.preventDefault();

    const title = document.getElementById("title").value.trim();
    const year = document.getElementById("year").value.trim();
    const director = document.getElementById("director").value.trim();
    const star = document.getElementById("star").value.trim();

    let queryParams = [];
    if (title)
    {
        sessionStorage.setItem("title", title)
        queryParams.push(`title=${encodeURIComponent(title)}`);
    }
    if (year)
    {
        sessionStorage.setItem("year", year)
        queryParams.push(`year=${encodeURIComponent(year)}`);
    }
    if (director)
    {
        sessionStorage.setItem("director", director)
        queryParams.push(`director=${encodeURIComponent(director)}`);
    }
    if (star)
    {
        sessionStorage.setItem("star", star)
        queryParams.push(`star=${encodeURIComponent(star)}`);
    }
    const queryString = queryParams.length > 0 ? `?${queryParams.join("&")}` : "";
    window.location.href = `movie-list.html${queryString}`;
});