[![Community badge: Incubating](https://img.shields.io/badge/Lifecycle-Incubating-blue)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#incubating-)
[![Community extension badge](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)

# Zeebe Cherry Office PDF Workers
this project is a collection of connectors/workers to manipulate documents.

# Start the framework
The project use the Cherry framework. See the documentation on [Cherry Framework](https://github.com/camunda-community-hub/zeebe-cherry-framework)

## Specify the connection to Zeebe
Update src/main/resources/application.properties and set up information to connect to your Zeebe engine (local or in the cloud)

## Start the application

The collection cqn be start using the start.sh/.bat file

## Access the dashboard
Then access the server on localhost:9081. 

## design your process
You can download from the application the element-template files to design your process


# Manage files (or documents)

Zeebe does not store files as it.
The Cherry project offers different approaches to manipulating files across workers.
For example, OfficeToPdf needs an MS office or an Open Office document as input and will produce a PDF document as a result.

How to give this document? How to store the result?

The Cherry project introduces the StorageDefinition concept. This information explains how to access files (same concept as a JDBC URL).
Then OfficeToPdf required a sourceStorageDefinition, and a destinationStorageDefinition.
LoadFileFromDisk required a storageDefinition, and produced as output a "fileLoaded".
Note: the storage definition is the way to access where files are stored, not the file itself.

Existing storage definitions are:
* **JSON**: files are stored as JSON, as a process variable. This is simple, but if your file is large, not very efficient. The file is encoded in base 64, which implies a 20 to 40% overload, and the file is stored in the C8 engine, which may cause some overlap.

Example with LoadFileFromDisk:
````
storageDefinition: JSON
````
fileLoaded contains a JSON information with the file
````
{"name": "...", "mimeType": "application/txt", value="..."}
````

* **FOLDER:<path>**. File is store on the folder, with a unique name to avoid any collision.

Example with LoadFileFromDisk:
````
storageDefinition: FOLDER:/c8/fileprocess
````
fileLoaded contains
````
"contractMay_554343435533.docx"
````
Note: the folder is accessible by workers. If you run a multiple Vercors application on different hosts, the folder must be visible by all applications.

* **TEMPFOLDER**, the temporary folder on the host, is used to store the file, with a unique name to avoid any collision

Example with LoadFileFromDisk:
````
storageDefinition: TEMPFOLDER
````
fileLoaded contains
````
"contractMay_554343435533.docx"
````
This file is visible in the temporary folder on the host

Note: the temporary folder is accessible only on one host, and each host has a different temporary folder. This implies your workers run only on the same host, not in a cluster.

# Tutorial
This tutorial load a file from a disk, and produce the PDF. It save them the result on a repository on the disk.
The TEMPORARY FOLDER is used for the storage.

## update the Zeebe connection
Edit src/main/resources/application.properties and set up the connection to your Zeebe client.
Note: using the Saas service, go to Client, create a new Client and click on "Spring Boot" button to doawnload properties



## upload the template-file in the modeler
Start the application.

Access the dashboard, and click on "Workers and Connectors".
![DowloadElementTemplate](src/main/resources/static/img/Architecture.png?raw=true)

# Build the Docker image
docker build -t zeebe-cherry-officepdf:1.0.0 .