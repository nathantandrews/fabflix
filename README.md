## CS 122B Project 1
DEMO YOUTUBE LINK: https://youtu.be/gAr7JOLEzjY


Prepared Statements and JDBC Connection Pooling Filenames:
- src/dev/wdal/dashboard/auth/DashboardLoginServlet.java
- src/dev/wdal/dashboard/AddMovieServlet.java
- src/dev/wdal/dashboard/AddStarServlet.java
- src/dev/wdal/dashboard/DatabaseMetadataServlet.java
- src/dev/wdal/main/auth/LoginServlet.java
- src/dev/wdal/main/BrowsingServlet.java
- src/dev/wdal/main/CartServlet.java
- src/dev/wdal/main/MovieListServlet.java
- src/dev/wdal/main/MovieSuggestionServlet.java
- src/dev/wdal/main/PaymentServlet.java
- src/dev/wdal/main/SingleMovieServlet.java
- src/dev/wdal/main/SingleStarServlet.java
  
Only PreparedStatements:
- src/dev/wdal/importer/AbstractManager.java and its derived classes

Parsing Time Optimization Strategies:
- Database Cache for quicker checking of duplicates
- LOAD DATA statement with .csv files
- ended up taking around 30 seconds compared to 15-20 minutes

Inconsistency Reports:
- genres_errors.log
- movies_errors.log
- stars_errors.log
- stars_in_movies_errors.log

All Inconsistency Reports are shown in the demo, and are placed at the root of the repository when parsing.

Contributions:

    Everything - Nathan
