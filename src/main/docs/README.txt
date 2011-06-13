dump2pg: Create database dumps from JDBC to PostgreSQL format
=============================================================

dump2pg is an application designed to be invoked by automation scripts
regarding migration of data from any DBMS into PostgreSQL in the most
efficient way.

The only requirement is that you have proper JDBC driver and connection
settings for the source database.

Through JDBC sessions, it does the following:
1. lists available tables in the database
2. creates database dump files in COPY command syntax ready to be used
   on PostgreSQL
   
Filtering of data is possible, which can be done by:
- catalog or database
- scheme: single or multiple (eg: "mdl%")
- table: single or multiple (eg: "Author%")
- selectively: by using a WHERE clause
- amount of rows (i.e., limit and offset)

It was tested on the following DBMS:
- Oracle (thin and OCI drivers)
- SQL Server (MS and JTDS drivers)

Please help us enhance it with coding, using or even giving new ideas! :D

http://dump2pg.googlecode.com