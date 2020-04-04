#Devlaza API

## Overview
This API for devlaza.  

## How to boot server
First, you have to execute to boot server this command.  
```bash
docker-compose up -d
```

Next, set mail auth info.
```bash
export MAILADDRESS=${Your mail account address.}
export PASSWORD=${Your mail account password.}
```

Final, you execute this command on project root directory.

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

Then you can receive mail from `${MAILADDRESS}`  

### Login
POST `http://localhost:8080/users/login`  
#### query
- address: String
- password: String

If you authentication succeeded, you can get login token.

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

If you wanna set some sites and tags, you must divide it `+`.

Sites format is `title,url`

### Get Project Info
GET `http://localhost:8080/project/`

