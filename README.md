"# spring-data-sample-security"

This is the third installment of the Spring Data Rest sample series. A security feature is added in this installment. To access those end-points, you need to log in. Two users are setup. One is joe as the username and secret as the password. And the other is jane as the username and secret as the password. The first user has a role of user and the second one has a role of admin. For a GET request, you need to sign in as a user while you need to sign in as an admin for a POST, PUT adn PATCH request.

All end-points of this application can be found on the Swagger page http://localhost:8080/swagger-ui.html