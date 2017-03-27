## Spring Data Sample With Security

This is the third installment of the Spring Data Rest sample series. A security feature is added in this installment. To access those end-points, you need to log in. Two users are setup. One is joe as the username and secret as the password. And the other is jane as the username and secret as the password. The first user has a role of user and the second one has a role of admin. For a GET request, you need to sign in as a user while you need to sign in as an admin for a POST, PUT and PATCH request.

Spring Data Sample series:

 * [First installment](https://github.com/vw98075/spring-data-sample): a single entity
 * [Second installment](https://github.com/vw98075/spring-data-sample2): multiple entities and they are related
 * Third installment: secure API access with in-memory user setup
 * Fourth installment: secure API access with DB user setup
 * Fifth installment: secure API access with JWT, a sessionless approach

All end-points of this application can be found on the Swagger page http://localhost:8080/swagger-ui.html