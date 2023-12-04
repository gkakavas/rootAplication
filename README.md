# API Documentation

## Introduction

Welcome to documentation for this HR management system API. This document provides information on how to 
interact with my API, the available endpoints, request and response formats, authentication, and authorization.

This API is a simple REST API for an HR Management System that provides operations about:
## Authentication:
  - Authenticate:
    * ### Authorities-Roles:
    Any user can be authenticated. No roles or authorities are required.
    * ### Endpoint:
    `POST /auth/authenticate`
    * ### Request Headers:
    Don't are required
    * ### Request Body:
    ```
    {
     "username": (a valid email) "test@email.com"
     "password": "TestPassword123"
    }
    ```
    * ### Response Headers:
    None
    
    * ### Response Body
      ```
      {
       "token" : (JWT) "Bearer ey..."
      }
      ```
  - Register:
    * ### Authorities-Roles:
    No role or authority required. This API endpoint is available only for IP addresses defined in the production profile as a list property:
    **Example for yml configuration**  
    ```
    allowed-ips:
    ips:
    - 0:0:0:0:0:0:0:1
    ```
    * ### Endpoint:
    `POST /auth/authenticate`
    * ### Request Headers:
    Don't are required
    * ### Request Body:
    ```
    {
    "username": (a valid email) "test@email.com"
    "password": "TestPassword123"
    }
    ```
    * ### Response Headers:
    None
    * ### Response Body
    ```
      {
      "token" : (JWT) "Bearer ey..."
      }
      ```
## Groups:
  - Create a group
  - Read a group
  - Read all groups
  - Update a group
  - Delete a group
## Users:
  - Create a user
  - Read connected user
  - Read a user
  - Read all users 
  - Update a user
  - Delete a user 
  - Patch a user 
  - Read connected user's events
  - Change password for connected user
## Events:
  - Create an event 
  - Create an event by group
  - Read an event 
  - Read all events
  - Update an event
  - Delete an event
  - Patch an event details
  - Add users into an existing event 
  - Remove users from an existing event
## Files:
  - Upload a file
  - Download a timesheet
  - Download an evaluation
  - Read all timesheets
  - Read all evaluations
  - Delete a file
  - Approve an evaluation
  - 
## Leaves:
  - Create a leave
  - Read a leave
  - Read all leaves
  - Update a leave
  - Delete a leave
  - Approve a leave
##
All API requests should be made to the following base URL: