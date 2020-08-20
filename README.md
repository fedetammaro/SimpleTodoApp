![Java CI](https://github.com/Sfullez/SimpleTodoApp/workflows/Java%20CI/badge.svg)
[![Coverage Status](https://coveralls.io/repos/github/Sfullez/SimpleTodoApp/badge.svg?branch=master)](https://coveralls.io/github/Sfullez/SimpleTodoApp?branch=master)\
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Sfullez_SimpleTodoApp&metric=alert_status)](https://sonarcloud.io/dashboard?id=Sfullez_SimpleTodoApp)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sfullez_SimpleTodoApp&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=Sfullez_SimpleTodoApp)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=Sfullez_SimpleTodoApp&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=Sfullez_SimpleTodoApp)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=Sfullez_SimpleTodoApp&metric=security_rating)](https://sonarcloud.io/dashboard?id=Sfullez_SimpleTodoApp)\
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=Sfullez_SimpleTodoApp&metric=sqale_index)](https://sonarcloud.io/dashboard?id=Sfullez_SimpleTodoApp)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Sfullez_SimpleTodoApp&metric=bugs)](https://sonarcloud.io/dashboard?id=Sfullez_SimpleTodoApp)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=Sfullez_SimpleTodoApp&metric=code_smells)](https://sonarcloud.io/dashboard?id=Sfullez_SimpleTodoApp)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=Sfullez_SimpleTodoApp&metric=duplicated_lines_density)](https://sonarcloud.io/dashboard?id=Sfullez_SimpleTodoApp)

Simple Todo Application developed using TDD and the tools/techniques seen at the Advanced Techniques and Tools for Software Development course at Università degli Studi di Firenze, using Maven and GitHub Actions.

## Prerequisites
- Java 8
- Maven
- A GitHub repository for Continuous Integration (if you want to execute remotely tests, code coverage with Coveralls and code quality analysis with SonarCloud)
- Docker (for the MongoDB container if you want to run the application as standalone)

### How to setup a MongoDB docker container with a replica set
Since a MongoDB database with transactions support is required to try the application, here are the steps to setup a working MongoDB instance with its replica set, implying a Docker installation is already present:

 1. `docker pull mongo` to download the latest MongoDB docker image
 2. `docker network create mongo-network` to create a Docker network named "mongo-network" for MongoDB instances to communicate
 3. `docker run -p 27017:27017 --name mongo-primary --net mongo-network mongo mongod --replSet todoapp-replica-set` to create a MongoDB docker container on the created network; finally, it invokes the Mongo daemon to add the instance to a replica set called "todoapp-replica-set"
 4. `docker run -p 27018:27017 --name mongo-secondary --net mongo-network mongo mongod --replSet todoapp-replica-set` to create another MongoDB docker container for the secondary instance for the replica set

Then, we have to setup the replica set so that the two instances act as primary and secondary nodes. To do this, we connect to the Mongo shell of the primary node with `docker exec -it mongo-primary mongo` and we create the following configuration:

 1. `db = (new Mongo('localhost:27017')).getDB('todoapp')` to create the database that will be used by the application
 2. `configuration = {"_id": "todoapp-replica-set", "members": [{"_id": 0, "host": "mongo-primary:27017"}, {"_id": 1, "host": "mongo-secondary:27017"}]}` creates a configuration with the two instances, from the set network, as nodes of the replica set
 3. `rs.initiate(configuration)` to initiate the replica set with the given configuration

Now the replica set has been initialized and configured and the application can be started. The replica set URl to pass to the application will be `mongodb://localhost:27017`.

## Build with Maven
- `mvn clean verify` to simply run all tests
- `mvn clean verify org.pitest:pitest-maven:mutationCoverage` to run all tests and perform mutation testing

Add `-Pjacoco` to also generate the code coverage report made by Jacoco during the build process.
Note that the replica set setup is not needed for Maven builds, since all tests use Testcontainers for the MongoDB database instance.

### Run the application
Once the application has been packaged with `mvn clean package`, the .jar contaning backend and frontend can be found in `/simpletodoapp-gui/target`. Use `java -jar <path_to_jar> [options]` to run the application.
| Option name | Description |
|-|-|
| `--mongo-url` | URL of the replica set, by default `mongodb://localhost:27017`; obtainable as `mongodb://localhost:[primary_node_port_number]` |
| `--db-name` | Database name, as specified during the replica set setup phase, by default `todoapp` |
| `--db-tasksCollection` | Name of the tasks collection in the database, by default `tasks` |
| `--db-tagsCollection` | Name of the tags collection in the database, by default `tags` |

## Continuous integration
The .yml workflow file to perform continuous integration on the project with GitHub actions is provided in the repository; if you want to also check the Coveralls and Sonarcloud status, some environment variables need to be modified inside the .yml workflow file:

 - `SONAR_PROJECT`: given by Sonarcloud during the initialization phase
 - `SONAR_ORGANIZATION`: given by Sonarcloud during the initialization phase
 - `SONAR_URL`: given by Sonarcloud during the initialization phase
 - `SONAR_TOKEN`: given by Sonarcloud during the initialization phase; this needs to be set as a GitHub Secret
 - `GITHUB_TOKEN`: set by default by GitHub as secret
 - `COVERALLS_TOKEN`: given by Coveralls during the initialization phase; this needs to be set as a GitHub Secret

To setup a token/key as secret in GitHub, simply go to the repository **Settings** and then select **Secrets**; alternatively, if code quality and coverage analyses are not needed, simply remove the `sonar:sonar coveralls:report` goals from the Maven build inside the .yml workflow file.