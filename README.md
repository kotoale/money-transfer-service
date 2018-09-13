# revolut-test-task

## Requirements
Design and implement a RESTful API (including data model and the backing implementation) for money
transfers between accounts.

Explicit requirements:
1. keep it simple and to the point (e.g. no need to implement any authentication, assume the APi is
invoked by another internal system/service)
2. use whatever frameworks/libraries you like (except Spring, sorry!) but don't forget about the
requirement #1
3. the datastore should run in-memory for the sake of this test
4. the final result should be executable as a standalone program (should not require a pre-installed
container/server)
5. demonstrate with tests that the API works as expected

## Solution
REST Service is based on Dropwizard framework with embedded Jetty container and Jersey framework for building REST
Google Guice is used for DI and Hibernate - for persistence.

### REST API description
- GET     /accounts?id={id}
- GET     /accounts/list 
- POST    /accounts/create -d '{"initAmount": null}'
- PUT     /accounts/deposit -d '{"id": "3", "amount": "0.1"}'
- PUT     /accounts/withdraw -d '{"id": "3", "amount": "0.1"}'   
- PUT     /accounts/transfer -d '{"fromId": "1","toId": "3", "amount": "0.2"}'
- DELETE  /accounts/delete?id={id}

#### How to run

