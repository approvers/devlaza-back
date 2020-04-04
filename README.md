# Devlaza API

## Overview
An API for devlaza.

## How to boot server
First, You need to execute the command shown below to boot the API server.
```bash
docker-compose up -d
```

Then, set mail auth info.
```bash
export MAILADDRESS=${Your mail account address.}
export PASSWORD=${Your mail account password.}
```

Finally, execute this command on the project root directory.

```bash
./gradlew bootRun
```

## Commands
### Create new user

POST `http://localhost:8080/users/new`  
#### query
- name: String
- password: String
- showId: String
- mailAddress: String

You will receive mail from `${MAILADDRESS}` to check if the email is valid or not.

### Login
POST `http://localhost:8080/users/login`  
#### query
- address: String
- password: String

If you succeed in authentication, you will get a login token.

### Get user info
GET `http://localhost:8080/users/<id>`  
\<id> is user id.

### Create Project
POST `http://localhost:8080/projects/` <= Don't forget last `/`!!!
#### query
- name: String
- token: String
- introduction: String
- sites: String
- tags: String

If you want to specify multiple sites and tags, they need to be separated by `+`.

Format of sites is `title,url`

### Get all projects
GET `http://localhost:8080/projects/`

### Get Project Info
GET `http://localhost:8080/projects/<id>`
\<id> is project id.

### Search project with some parameters
GET `http://localhost:8080/projects/contidion`  
query
- keyword: String
- user: String
- tags: String
- sort: String(asc|desc|popular)
- recruiting: Int(1: open 0: close 2: both)
- searchStartDate: String(LocalDate)
- searchEndDate: String(LocalDate)

### Join to project
PATCH `http://localhost:8080/projects/join/<id>`  
\<id> is project id.  
query
- token: String

### Leave from project
DELETE `http://localhost:8080/projects/leave/<id>`  
\<id> is project id.  
query
- token:String

### Delete project
DELETE `http://localhost:8080/projects/<id>`  
\<id> is project id.
query
- token:String

### GetUserInfoWithToken
POST `http://localhost:8080/auth/`  
query
- token:String