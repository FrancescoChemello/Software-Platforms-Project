# PLATFORM FOR RETRIEVING ONLINE ARTICLES - SOFTWARE PLATFORMS PROJECT #

## Overview ##
The project's objective is to build a platform for obtaining articles from online newspapers, storing them, making them searchable, and extracting representations of themes discussed in a set of articles returned as results for a given query.

This project was developed as part of the [Software Platforms](https://stem.elearning.unipd.it/course/view.php?id=8355) course.

*Computers and Networks Security* is a course of the [Master Degree in Computer Engineering](https://degrees.dei.unipd.it/master-degrees/computer-engineering/) of the  [Department of Information Engineering](https://www.dei.unipd.it/en/), [University of Padua](https://www.unipd.it/en/), Italy.

## Dependencies ##

Before running the project, make sure you have the following installed:

- **Java 21** (OpenJDK 21 or compatible)
- **Maven** (for building the project)
- **Docker** (for running the services in containers; Docker Compose is included in recent Docker versions)

### Installation on Debian/Ubuntu systems ###

Install **Java 21**, **Maven**, and **Docker** with:

```
sudo apt update
sudo apt install openjdk-21-jdk maven docker.io
```

Check the installed versions:

```
java -version
mvn -version
docker --version
```

> **Note:**  
> - For other Linux distributions or operating systems, refer to the official documentation for [Java](https://adoptium.net/), [Maven](https://maven.apache.org/install.html), and [Docker](https://docs.docker.com/get-docker/).


## Repository Structure ##

The repository is organized as follows:

```
Software-Platforms-Project/
│
├── client-service/                # Microservice for managing client requests
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/it/unipd/dei/softplat/client/
│   │   │   │   ├── controller/    # REST controllers (e.g., ClientController.java)
│   │   │   │   ├── dto/           # Data Transfer Objects (e.g., MessageDTO.java)
│   │   │   │   ├── model/         # Domain models (e.g., QueryResult.java, QueryTopic.java)
│   │   │   │   └── service/       # Business logic (e.g., ClientService.java)
│   │   │   └── resources/
│   │   │       ├── application.properties.example
│   │   │       └── log4j2-spring.xml
│   │   └── test/java/it/unipd/dei/softplat/client/
│   │       └── ClientTest.java
│   └── pom.xml
│
├── monitoring-service/            # Microservice for monitoring topics
│   └── ...
│
├── datamanager-service/           # Microservice for managing data from the monitoring service
│   └── ...
│
├── mallet-service/                # Microservice for topic extraction (Mallet)
│   └── ...
│
├── mongodb-service/               # Microservice for MongoDB integration
│   └── ...
│
├── elasticsearch-service/         # Microservice for Elasticsearch integration
│   └── ...
│
├── common-http/                   # Module for common HTTP utilities
│   └── ...
│
├── docker-compose.yml             # Docker orchestration file
├── README.md
└── ...
```

**Note:**  
Every microservice follows the same basic structure (`controller/`, `dto/`, `model/`, `service/`, `resources/`, `test/`), making the project easy to maintain and scale.

## How to run the program ##

### Compilation (Build) ###

First, build all Java services using **Maven** (required only the first time or after code changes):

```
mvn clean package spring-boot:repackage
```

Then, build all Docker images and start the containers:

```
docker compose build --no-cache && docker compose up -d
```

### Execution (Usage) ###

To interact with the platform, attach to the `client-service` container (the main entry point for user interaction):

```
docker attach client-service
```

You can also view the logs of any service with:

```
docker compose logs <service-name>
```

To stop all services and remove the containers:

```
docker compose down
```

## How it works (Description) ##

This platform allows expert users (e.g., journalists or sociologists) to monitor, store, search, and analyze articles from online newspapers, focusing on specific issues (e.g., "Artificial Intelligence", "Climate Change").

### Main components

- **Client Service:** User interface for submitting queries and viewing results.
- **Monitoring Service:** Periodically retrieves articles from online newspapers based on user-defined issues.
- **Data Manager Service:** Stores and manages articles in the database.
- **Mallet Service:** Performs topic extraction and theme analysis on article sets.
- **MongoDB/Elasticsearch Services:** Provide storage and search capabilities.

### Typical workflow

1. **Start monitoring:**  
   The user submits an "issue query" (e.g., `"artificial intelligence"`) to start monitoring articles from a specific newspaper (e.g., The Guardian).
2. **Article collection:**  
   The platform periodically fetches and stores relevant articles in the database.
3. **Search and analysis:**  
   The user submits a new query (e.g., `"ChatGPT"`) to extract all articles matching that query among those relevant to the issue.
4. **Theme extraction:**  
   The platform analyzes the resulting subcorpus and returns a representation of the main themes discussed.

This workflow helps users study how specific topics are discussed in the media over time.

### License ###

All the contents of this repository are shared using the [Creative Commons Attribution-ShareAlike 4.0 International License](http://creativecommons.org/licenses/by-sa/4.0/).

![CC logo](https://i.creativecommons.org/l/by-sa/4.0/88x31.png)