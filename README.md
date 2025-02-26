## CS 122B Project 4
DEMO YOUTUBE LINK: https://youtu.be/gAr7JOLEzjY


Prepared Statements and JDBC Connection Pooling Filenames:
- webapp/src/main/java/dev/wdal/dashboard/auth/DashboardLoginServlet.java
- webapp/src/main/java/dev/wdal/dashboard/AddMovieServlet.java
- webapp/src/main/java/dev/wdal/dashboard/AddStarServlet.java
- webapp/src/main/java/dev/wdal/dashboard/DatabaseMetadataServlet.java
- webapp/src/main/java/dev/wdal/main/auth/LoginServlet.java
- webapp/src/main/java/dev/wdal/main/BrowsingServlet.java
- webapp/src/main/java/dev/wdal/main/CartServlet.java
- webapp/src/main/java/dev/wdal/main/MovieListServlet.java
- webapp/src/main/java/dev/wdal/main/MovieSuggestionServlet.java
- webapp/src/main/java/dev/wdal/main/PaymentServlet.java
- webapp/src/main/java/dev/wdal/main/SingleMovieServlet.java
- webapp/src/main/java/dev/wdal/main/SingleStarServlet.java
  
Only PreparedStatements:
- importer/src/main/java/dev/wdal/importer/AbstractManager.java and its derived classes

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
