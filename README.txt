# Tecnologie Informatiche per il Web (TIW) Course @Polimi (AY 2020-2021)

## Images Catalog project

### Introduction
In this project we implemented a website for the management of a hierarchical catalog of images.

### Implementation
The website has been developed in two versions:
- HTML pure,
- RIA (Rich Internet Applications).

### Documentation
There are two different sets of slides (both in Italian), one for the [HTML pure version](https://github.com/leoguerra8/TIWProject/tree/master/Documentazione/HTML.pdf) and the other for th [RIA version](https://github.com/leoguerra8/TIWProject/tree/master/Documentazione/RIA.pdf) of the project, respectively, with:
- data and requests analysis,
- database design and local database schema,
- application design,
- events/actions and controller/event handler tables (only for the RIA version),
- components description,
- events diagrams,
- utils packages descriptions. 

### Executing the project
The latest version of the project is available at [this GitHub Page](https://github.com/leoguerra8/TIWProject).

To execute the project, you need, first, to clone this repository, then, to open it (the use of Eclipse is suggested), but before running one of the two versions of the project with a working Tomcat (v9.0) server, you need to:
- create a new db schema (e.g. with MySQL Workbench), with the name "db_catalog" and import in the new schema the [provided db dump](https://github.com/leoguerra8/TIWProject/blob/master/dbDump.sql),
- change the web.xml file in the folder /WebContent/WEB-INF, replacing dbUser and dbPassword with your own MySQL parameters.

### Authors
- [Leonardo Guerra](https://github.com/leoguerra8)
- [Gaia Locchi](https://github.com/gaialocchi)