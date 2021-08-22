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
**_REST Service is based on Dropwizard framework with embedded Jetty container and Jersey framework for building REST_**

**_Google Guice is used for DI_**

**_Hibernate - for persistence_**

### REST API description
- GET     /accounts?id={id}
- GET     /accounts/list 
- POST    /accounts/create -d '{"amount": null}'
- PUT     /accounts/deposit -d '{"id": "3", "amount": "0.1"}'
- PUT     /accounts/withdraw -d '{"id": "3", "amount": "0.1"}'   
- PUT     /accounts/transfer -d '{"fromId": "1","toId": "3", "amount": "0.2"}'
- DELETE  /accounts/delete?id={id}

### How to run
_**Make the project from project directory**_
 
_(you need to have maven 3.x installed on your PC)_
```
mvn clean install
```
_**Start the application**_
```
java -jar target/rest-service-1.0-SNAPSHOT.jar server account-rest-service-app.yaml
```
### Now you can test in via curl
#### 1. List all accounts
**Request:**
```
curl -H "Content-Type: application/json"  http://localhost:8080/accounts/list
```
**Response:**
```
{"accounts":[{"id":1,"amount":"0.00000000"},{"id":2,"amount":"200.00000000"},{"id":3,"amount":"300.00400000"},{"id":4,"amount":"123.32100000"},{"id":5,"amount":"777.00740000"}]}
``` 
#### 2. Get account by id

**Request:**
```
curl -H "Content-Type: application/json" http://localhost:8080/accounts?id=1
```
**Response:**
```
{"account":{"id":1,"amount":"0.00000000"},"status":"READ"}
```
**Request:**
```
curl -H "Content-Type: application/json" http://localhost:8080/accounts?id=6
```
**Response:**
```
There's no account with id: 6
```
#### 3. Deposit money to account
**Request:**
```
curl -X PUT -H "Content-Type: application/json" -d '{"id": "3", "amount": "100.02"}' http://localhost:8080/accounts/deposit

```
**Response:**
```
{"account":{"id":3,"amount":"400.02400000"},"status":"UPDATED"}
```
**Request:**
```
curl -X PUT -H "Content-Type: application/json" -d '{"id": "0", "amount": "100.02"}' http://localhost:8080/accounts/deposit
```
**Response:**
```
There's no account with id: 0
```
**Request:**
```
curl -X PUT -H "Content-Type: application/json" -d '{"id": "0", "amount": "100.123456789"}' http://localhost:8080/accounts/deposit
```
**Response:**
```
{"errors":["amount numeric value out of bounds (<37 digits>.<8 digits> expected)"]}
```
**Request:**
```
curl -X PUT -H "Content-Type: application/json" -d '{"id": null, "amount": "100.12345678"}' http://localhost:8080/accounts/deposit

```
**Response:**
```
{"errors":["id may not be null"]}
```
**Request:**
```
curl -X PUT -H "Content-Type: application/json" -d '{"id": "3", "amount": "-100.12345678"}' http://localhost:8080/accounts/deposit

```
**Response:**
```
{"errors":["amount must be greater than 0.0"]}
```
**Request:**
```
curl -X PUT -H "Content-Type: application/json" -d '{"id": "3", "amount": "100.12345678"}' http://localhost:8080/accounts/deposit

```
**Response:**
```
{"account":{"id":3,"amount":"500.14745678"},"status":"UPDATED"}
```
#### 4. Withdraw money from account

**Request:**
```
curl -X PUT -H "Content-Type: application/json" -d '{"id": "3", "amount": "100.12345678"}' http://localhost:8080/accounts/withdraw
```
**Response:**
```
{"account":{"id":3,"amount":"500.14745678"},"status":"UPDATED"}
```
**Request:**
```
curl -X PUT -H "Content-Type: application/json" -d '{"id": "1", "amount": "100.12345678"}' http://localhost:8080/accounts/withdraw

```
**Response:**
```
Current amount (0.00000000) is less than amount to withdraw (100.12345678) for the account with id = 1
```
#### 5. Create account

For POST request with empty body account with default init amount will be created

see property initialMoneyAmount in configuration account-rest-service-app.yaml

if the property is missed default value is zero

**Request:** 
```
curl -X POST -H "Content-Type: application/json" http://localhost:8080/accounts/create
```
**Response:**
```
{"account":{"id":6,"amount":"100"},"status":"CREATED"}
```
You also can specify initial amount in the request:
```
curl -H "Content-Type: application/json" -d '{"amount": "123"}' http://localhost:8080/accounts/create 
```
**Response:**
```
{"account":{"id":7,"amount":"123"},"status":"CREATED"}
```
Null amount in the request also supported - then default value (described above) will be used
```
curl -H "Content-Type: application/json" -d '{"amount": null}' http://localhost:8080/accounts/create  
```
**Response:**
```
{"account":{"id":8,"amount":"100"},"status":"CREATED"}
```

#### 6. Transfer money from one account to another

It is prohibited to transfer money to the same account
```
curl -X PUT -H "Content-Type: application/json" -d '{"fromId": "1","toId": "1", "amount": "200"}' http://localhost:8080/accounts/transfer
```
**Response:**
```
ransfer money to the same account is forbidden
```
It is prohibited to transfer money to the same account
```
curl -X PUT -H "Content-Type: application/json" -d '{"fromId": "1","toId": "1", "amount": "200"}' http://localhost:8080/accounts/transfer
```
**Response:**
```
ransfer money to the same account is forbidden
```
**Request:**
```
curl -X PUT -H "Content-Type: application/json" -d '{"fromId": "1","toId": "2", "amount": "200"}' http://localhost:8080/accounts/transfer
```
**Response:**
```
Current amount (0.00000000) is less than amount to withdraw (200) for the account with id = 1
```
**Request:**
```
curl -X PUT -H "Content-Type: application/json" -d '{"fromId": "2","toId": "1", "amount": "200"}' http://localhost:8080/accounts/transfer
```
**Response:**
```
{"account":{"id":2,"amount":"0.00000000"},"status":"UPDATED"}
```
**Request:**
```
curl -X PUT -H "Content-Type: application/json" -d '{"fromId": "2","toId": "10", "amount": "200"}' http://localhost:8080/accounts/transfer
```
**Response:**
```
There's no account with id: 10
```
