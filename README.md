Requirements

Here I provide information on issues that are done in this submit

Basic requirements (If these aren’t met the assignment will not pass):
1, A user needs to be able to add a new service with URL and a name   (DONE)
2, Added services have to be kept when the server is restarted        (DONE)
3, Display the name, url, creation time and status for each service   (DONE)
4, Provide a README in english with instructions on how to run the application (DONE)



Extra requirements (No prioritisation on these, pick the ones that you find interesting):

1, We want full create/update/delete functionality for services                                                 (NOT FULLY DONE)
  + I only have time to implement create and delete function.  However, update is quite similar 
  where I can make a new endpoint called update and update SQL query
  
2, The results from the poller are automatically shown to the user (no need to reload the page to see results)  (DONE)
  + I use a function to keep fetching services and fill in the status information every 2 seconds

3, We want to have informative and nice looking animations on add/remove services                               (DONE)
  + I just use the simple trick that change the color from back to blue or red when the information 
  about status is fetched

4, The service properly handles concurrent writes                                                               (NOT IMPLEMENT)

5, Protect the poller from misbehaving services (for example answering really slowly)                           (NOT IMPLEMENT)

6, URL Validation ("sdgf" is probably not a valid service)                                                      (DONE)

7, Multi user support. Users should not see the services added by another user                                  (NOT IMPLEMENT)

## Tech stack

- In this project, I use Java 8 and vertx for handling running server and io.vertx.ext.jdbc for SQL database which is simple
enough in one framework vertx. For the front end part, I just use html and basic javascript.
the project is built with gradle. Note that the gradle that works with Java 8.

## How to run

You can start the server by running the start class in the file Start.java in src from IntelliJ IDEA

Access in browser:

http://localhost:8080

## How to use the service

1. Click add service button to be able to create a new service 
2. Enter url and name, then click save button to create new service
3. Click delete button to delete a service
4. Entering an invalid url will prompt an alert when we click Save button
