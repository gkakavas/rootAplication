# API Documentation

## Introduction

Welcome to documentation for this HR management system API. This document provides information on how to
interact with my API, the available endpoints, request and response formats, authentication, and authorization.

This API is a simple REST API for an HR Management System that provides operations about:

## Authentication:
___
* Authenticate:
### Authorities-Roles:
Any user can be authenticated. No roles or authorities are required.
### Endpoint:
`POST /auth/authenticate`
### Request Headers:
Don't are required
### Request Body:
```
   {
       "username": (a valid email) "test@email.com"
       "password": "TestPassword123"
   }
   ```
### Response Headers:
None
### Response Body
```
   {
       "token" : (JWT) "Bearer ey..."
   }
   ```
---
* Register:
### Authorities-Roles:
No role or authority required. This API endpoint is available only for IP addresses defined in the production
profile as a list property:
**Example for yml configuration**
  ```
  allowed-ips:
    ips:
      - 0:0:0:0:0:0:0:1
  ```
### Endpoint:
`POST /auth/register`
### Request Headers:
Don't are required
### Request Body:
```
   {
     "firstname" : "test",
     "lastname" : "test",
     "email" : "test@test.com",
     "password" :"TestPassword123",
     "specialization": "BACKEND",
     "role": "ADMIN",
     "currentProject": "Test project"
   }
 ```
### Response Headers:
None
### Response Body
```
 {
     "token" : (JWT) "Bearer ey..."
 }
 ```
____________________________________________________________________________________
## Groups:
* Create a group
### Authorities-Roles:
Required `ADMIN` role or authority `group::create` to access this endpoint.
### Endpoint:
`POST /group/create`
### Request Headers:
`'Authorization' : (JWT) 'Bearer ey....'`
### Request Body:
```
   {
    "groupName":"Group",
    "idsSet":{
        "userIds":["dda820ae-db8b-4fac-a6b4-926bf20a7ba0"]
    }
   }
   ```
### Response Headers:
None
### Response Body
```
{
    "groupId": "54ea4d6d-caaa-413b-871a-9ddcddef4f4e",
    "groupName": "Group",
    "groupCreator": "group@creator.com",
    "groupCreationDate": [2023,12,4,15,42,17],
    "users": [
        {
            "userId": "dda820ae-db8b-4fac-a6b4-926bf20a7ba0",
            "firstname": "test",
            "lastname": "test",
            "email": "test@test",
            "specialization": "BACKEND",
            "currentProject": "Test project",
            "groupName": "Group",
            "createdBy": "group@creator.com",
            "registerDate": [2023,12,4,15,14,48],
            "lastLogin": null,
            "role": "ADMIN"
        }
    ]
}
```
---
* Read a group
### Authorities-Roles:
Required `ADMIN` role or authority `group::readOne` to access this endpoint.
### Endpoint:
`POST /group/{groupId}`: Request parameter groupId should be a valid UUIDv4
### Request Headers:
`'Authorization' : (JWT) 'Bearer ey....'`
### Request Body:
None
### Response Headers:
None
### Response Body
```
{
    "groupId": "19dd2267-8a90-47e5-9a2d-d9e767a109de",
    "groupName": "group1",
    "groupCreator": null,
    "groupCreationDate": [2023,12,1,12,1,6],
    "users": [
        {
            "userId": "e6e47100-2fe1-4413-89cf-2df47253a3af",
            "firstname": "firstname2",
            "lastname": "lastname2",
            "email": "firstname2@email.com",
            "specialization": "specialization2",
            "currentProject": "current_project2",
            "groupName": "group1",
            "createdBy": null,
            "registerDate": [2023,12,1,12,1,6],
            "lastLogin": null,
            "role": "ADMIN"
        },
        {
            "userId": "ca2d6dfd-8a5d-40f1-8b3b-b7b176ccdbf7",
            "firstname": "firstname1",
            "lastname": "lastname1",
            "email": "firstname1@email.com",
            "specialization": "specialization1",
            "currentProject": "current_project1",
            "groupName": "group1",
            "createdBy": null,
            "registerDate": [2023,12,1,12,1,6],
            "lastLogin": [2023,12,4,15,42,10],
            "role": "ADMIN"
        }
    ]
}
```
---
* Read all groups
### Authorities-Roles:
Required `ADMIN` role or authority `group::readAll` to access this endpoint.
### Endpoint:
`POST /group/all`
### Request Headers:
`'Authorization' : (JWT) 'Bearer ey....'`
### Request Body:
None
### Response Headers:
None
### Response Body
```
[
{
    "groupId": "19dd2267-8a90-47e5-9a2d-d9e767a109de",
    "groupName": "group1",
    "groupCreator": null,
    "groupCreationDate": [2023,12,1,12,1,6],
    "users": [<user1>,<user2>,....,<userN>]
},
{
    "groupId": "78pe8023-8a90-47e5-9a2d-d9e767a109de",
    "groupName": "group2",
    "groupCreator": null,
    "groupCreationDate": [2023,12,1,12,1,6],
    "users": [<user1>,<user2>,....,<userN>]
},
{
    "groupId": "57pk3428-8a90-47e5-9a2d-d9e767a109de",
    "groupName": "group3",
    "groupCreator": null,
    "groupCreationDate": [2023,12,1,12,1,6],
    "users": [<user1>,<user2>,....,<userN>]
}
]
```
---
* Update a group
### Authorities-Roles:
Required `ADMIN` role or authority `group::update` to access this endpoint.
### Endpoint:
`POST /group/update/{groupId}`: Request parameter groupId should be a valid UUIDv4
### Request Headers:
`'Authorization' : (JWT) 'Bearer ey....'`
### Request Body:
```
  {
    "groupName":"Group",
    "idsSet":{
        "userIds":["dda820ae-db8b-4fac-a6b4-926bf20a7ba0"]
    }
    }
```
### Response Headers:
None
### Response Body
```
{
    "groupId": "19dd2267-8a90-47e5-9a2d-d9e767a109de",
    "groupName": "Group",
    "groupCreator": null,
    "groupCreationDate": [2023,12,1,12,1,6],
    "users": [
        {
            "userId": "dda820ae-db8b-4fac-a6b4-926bf20a7ba0",
            "firstname": "test",
            "lastname": "test",
            "email": "test@test.com",
            "specialization": "BACKEND",
            "currentProject": null,
            "groupName": "Group",
            "createdBy": null,
            "registerDate": [2023,12,4,15,14,48],
            "lastLogin": null,
            "role": "ADMIN"
        }
    ]
}
```
---
* Delete a group
### Authorities-Roles:
Required `ADMIN` role or authority `group::update` to access this endpoint.
### Endpoint:
`POST /group/delete/{groupId}`: Request parameter groupId should be a valid UUIDv4.
### Request Headers:
`'Authorization' : (JWT) 'Bearer ey....'`
### Request Body:
None
### Response Headers:
None
### Response Body
Only a response status 204 No Content to indicate that resource successfully completed.
__________________________________________________________________________________________
## Users:
* Create a user
* Read connected user
* Read a user
* Read all users
* Update a user
* Delete a user
* Patch a user
* Read connected user's events
* Change password for connected user
___________________________________________________________________________________________
## Events:
* Create an event
* Create an event by group
* Read an event
* Read all events
* Update an event
* Delete an event
* Patch an event details
* Add users into an existing event
* Remove users from an existing event
__________________________________________________________________________________________
## Files:
* Upload a file
* Download a timesheet
* Download an evaluation
* Read all timesheets
* Read all evaluations
* Delete a file
* Approve an evaluation
___________________________________________________________________________________________
## Leaves:
* Create a leave
* Read a leave
* Read all leaves
* Update a leave
* Delete a leave
* Approve a leave
##
All API requests should be made to the following base URL: