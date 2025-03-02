## CS 122B Project 4
- # General
    - #### Team#:
        - #### wedontattendlecture
    - #### Names:
        - #### Nathan
    - #### Project 5 Video Demo Link:
        
    - #### Instruction of deployment:
        
    - #### Collaborations and Work Distribution:
        Nathan - Everything

- # Connection Pooling
    - #### Include the filename/path of all code/configuration files in GitHub of using JDBC Connection Pooling.
        - #### webapp/src/main/java/dev/wdal/dashboard/auth/DashboardLoginServlet.java
        - #### webapp/src/main/java/dev/wdal/dashboard/AddMovieServlet.java
        - #### webapp/src/main/java/dev/wdal/dashboard/AddStarServlet.java
        - #### webapp/src/main/java/dev/wdal/dashboard/DatabaseMetadataServlet.java
        - #### webapp/src/main/java/dev/wdal/main/auth/LoginServlet.java
        - #### webapp/src/main/java/dev/wdal/main/BrowsingServlet.java
        - #### webapp/src/main/java/dev/wdal/main/CartServlet.java
        - #### webapp/src/main/java/dev/wdal/main/MovieListServlet.java
        - #### webapp/src/main/java/dev/wdal/main/MovieSuggestionServlet.java
        - #### webapp/src/main/java/dev/wdal/main/PaymentServlet.java
        - #### webapp/src/main/java/dev/wdal/main/SingleMovieServlet.java
        - #### webapp/src/main/java/dev/wdal/main/SingleStarServlet.java
        - #### webapp/src/main/WebContent/META-INF/context.xml
    - #### Explain how Connection Pooling is utilized in the Fabflix code.
        - #### We use a context-defined datasource that uses the pooling factory.
    - #### Explain how Connection Pooling works with two backend SQL.
        - #### Each webapp instance has two datasources, each datasource has its own pool.

- # Master/Slave
    - #### Include the filename/path of all code/configuration files in GitHub of routing queries to Master/Slave SQL.
        - #### webapp/src/main/java/dev/wdal/dashboard/auth/DashboardLoginServlet.java
        - #### webapp/src/main/java/dev/wdal/dashboard/AddMovieServlet.java
        - #### webapp/src/main/java/dev/wdal/dashboard/AddStarServlet.java
        - #### webapp/src/main/java/dev/wdal/dashboard/DatabaseMetadataServlet.java
        - #### webapp/src/main/java/dev/wdal/main/auth/LoginServlet.java
        - #### webapp/src/main/java/dev/wdal/main/BrowsingServlet.java
        - #### webapp/src/main/java/dev/wdal/main/CartServlet.java
        - #### webapp/src/main/java/dev/wdal/main/MovieListServlet.java
        - #### webapp/src/main/java/dev/wdal/main/MovieSuggestionServlet.java
        - #### webapp/src/main/java/dev/wdal/main/PaymentServlet.java
        - #### webapp/src/main/java/dev/wdal/main/SingleMovieServlet.java
        - #### webapp/src/main/java/dev/wdal/main/SingleStarServlet.java
        - #### webapp/src/main/WebContent/META-INF/context.xml
    - #### How read/write requests were routed to Master/Slave SQL?
        - #### If the load balancer selects the master instance, all read and write only SQL statements are routed to the master.
        - #### If the load balancer selects the slave instance, all read only SQL statments are routed to the slave, while all the write only SQL statments are routed to the master.