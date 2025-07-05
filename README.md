# KG2PG: Knowledge Graph to Property Graph Transformation

Transform RDF graph data models to Property Graph (PG) data models using the S3PG algorithm.

## 🚀 Quick Start with JAR Release

**New: Download and run KG2PG without building!**

### Option 1: Download Pre-built JAR
```bash
# Download the latest JAR release
wget https://github.com/dkw-aau/KG2PG/releases/latest/download/kg2pg-v1.0.0.jar

# Run with embedded sample data (creates timestamped output)
java -jar kg2pg-v1.0.0.jar
```

### Option 2: Use with Your Own Data
```bash
# Create data directory and add your files
mkdir -p data
# Copy your RDF files (.nt, .ttl) to data/
# Copy your SHACL shapes to data/

# Run with your data
java -jar kg2pg-v1.0.0.jar
```

### What You Get
- ✅ **Timestamped Output**: Each run creates a unique directory like `output/YourDataset_2025-07-05_15-25-09_1720188645/`
- ✅ **No Conflicts**: Previous results are never overwritten
- ✅ **Zero Configuration**: Works out of the box with embedded sample data
- ✅ **Cross-Platform**: Same JAR works on Windows, macOS, and Linux
- ✅ **Professional Results**: CSV and JSON format property graph outputs

📖 **Detailed Usage**: See [JAR_USAGE.md](JAR_USAGE.md) for comprehensive instructions.

---

## 🐳 Docker Support (Multi-Architecture)

```bash
# Build for both Intel and ARM64 (Apple Silicon)
docker buildx build --platform linux/amd64,linux/arm64 -t kg2pg .

# Run with Docker
docker run --rm -v $(pwd)/data:/app/data -v $(pwd)/output:/app/output kg2pg
```

---

## 🛠️ Building from Source

### Quick Build for JAR Release
```bash
# Build executable JAR with embedded resources
./build-jar.sh

# Test the JAR
./test-jar.sh

# Prepare for release (comprehensive testing)
./prepare-release.sh
```

### Traditional Gradle Build
```bash
# Build without JAR packaging
./gradlew clean build
```

### Getting the Source Code

Codebase to transform RDF graph data models to Property Graph (PG) data models is available
at [https://github.com/dkw-aau/KG2PG/](https://github.com/dkw-aau/KG2PG/).

Start by cloning the code using the following command:

```bash
git clone https://github.com/dkw-aau/KG2PG.git
cd KG2PG
```

---

## 📊 Data Requirements

Before using the S3PG transformation algorithm, you have several options for data:

### Option 1: Use Embedded Sample Data (Recommended for Testing)
The JAR comes with embedded sample data - no additional downloads needed!

### Option 2: Use Your Own Data
- **RDF Dataset**: Your knowledge graph in RDF format (.nt, .ttl, .rdf)
- **SHACL Shapes**: Shape constraints for your dataset (.ttl)

### Option 3: Use Pre-configured Datasets

### 📁 Large Datasets

To begin, download the required datasets, i.e., DBpedia and Bio2RDF Clinical Trials Dataset

#### 1. DBpedia

We downloaded two versions of DBpedia.
The first version is from 2020 and the second version is from 2022.
The folder [dbpedia](https://github.com/dkw-aau/s3pg/tree/main/dbpedia) contains the files and scripts used to download
each of the dataset.

For convenience, we have made the datasets used for experiments available online. 
You can download the DBpedia-2020 from [this](https://bitbucket.org/kashifrabbani/s3pg-dbpedia2020) link and DBpedia-2022 from [this](https://bitbucket.org/kashifrabbani/s3pg-dbpedia2022) link.

#### 2. Bio2RDF Clinical Trials Dataset

We downloaded this dataset from the official link: https://download.bio2rdf.org/#/current/clinicaltrials/

For convenience, we have made the datasets used for experiments available online.
You can download the Bio2rdf dataset from [this](https://bitbucket.org/kashifrabbani/s3pg-bio2rdf-ct) link.


### 🔍 SHACL Shapes Generation

Utilize QSE (Quality Shapes Extractor) to extract SHACL shapes from your datasets.
[QSE](https://github.com/dkw-aau/qse) GitHub repository contains the codebase and instructions to extract SHACL shapes
from a given dataset.

You can download the SHACL shapes for all of the above datasets using this link: [S3PG-SHACL-SHAPES](https://bitbucket.org/kashifrabbani/s3pg-shacl)

---

## 🔄 Transforming KGs to PGs using S3PG

### JAR-Based Transformation (Recommended)

#### Simple Transformation
```bash
# Download and run with embedded data
java -jar kg2pg.jar

# Or with your own data
mkdir -p data
# Add your .nt/.ttl files to data/
java -jar kg2pg.jar
```

#### Custom Configuration
```bash
# Create config.properties with your settings
echo "dataset_name=MyDataset" > config.properties
echo "dataset_path=data/my-graph.nt" >> config.properties
echo "shapes_path=data/my-shapes.ttl" >> config.properties
echo "output_file_path=output/" >> config.properties

java -jar kg2pg.jar
```

### Docker-Based Transformation
```bash
# Build the image
docker build -t kg2pg .

# Run with mounted data
docker run --rm \
  -v $(pwd)/data:/app/data \
  -v $(pwd)/output:/app/output \
  kg2pg
```

### Legacy Shell Script Method

Once you have downloaded the datasets (RDF knowledge graphs) and SHACL shapes for them, you can use the traditional
shell script approach to transform them into property graphs.

#### System Requirements

**For JAR Usage:**
- Java 17 or higher
- Minimum 4GB RAM (16GB recommended for large datasets)
- 1GB free disk space

**For Building from Source:**
The experiments run on a _single machine_. To reproduce the experiments the software used are *a GNU/Linux
distribution (with git, bash, make, and wget)*, Docker, and Java  *version 17 or higher*
having a machine with 256 GB (minimum required 16GB) and CPU with 16 cores (minimum required 1 core).

We have prepared shell scripts and configuration files for each dataset to make the process of running experiments as
much easy as possible.

#### Configuration Parameters

**For JAR usage:** Configuration is optional - works with embedded defaults.

**For custom configuration:** Create a `config.properties` file:
```properties
dataset_name=MyDataset
shapes_path=data/my-shapes.ttl
dataset_path=data/my-graph.nt
output_file_path=output/
expected_number_classes=50
expected_number_of_lines=1000
is_wikidata=false
```

**For shell script usage:** Please update the configuration file for each dataset available in
the [config](https://github.com/dkw-aau/KG2PG/tree/master/config) directory, i.e., `dbpedia2020`, `dbpedia2022`,
and `bio2rdf` to set the correct paths for your machine.

#### Shell Scripts (Legacy Method)

Assuming that you are in the project's directory, you have updated the configuration file(s), and docker is installed on
your machine, move into [scripts](https://github.com/dkw-aau/KG2PG/tree/master/scripts) directory using the
command ``` cd scripts ``` and then execute one of the following shell scripts files:
``` ./run_bio2rdf.sh ``` ,
``` ./run_dbp2020.sh ``` ,
``` ./run_dbp2022.sh ```

You will see logs and the output will be stored in the path of the output directory specified in the config file.

*Note: You may have to execute ```chmod +rwx ``` for each script to solve the permissions issue.*

---

## 📁 Output

### Timestamped Output Directories
Each run creates a unique timestamped directory to prevent conflicts:
- **Format**: `output/DatasetName_YYYY-MM-DD_HH-MM-SS_timestamp/`
- **Example**: `output/GraphNpm_2025-07-05_15-25-09_1720188645/`

### Output Files
S3PG outputs Property Graph data in multiple formats:

**CSV Files:**
- `PG_NODES_WD_LABELS.csv` - Node labels and identifiers
- `PG_NODES_LITERALS.csv` - Node literal properties  
- `PG_RELATIONS.csv` - Relationships/edges between nodes
- `PG_PREFIX_MAP.csv` - Namespace prefix mappings

**JSON Files:**
- `PG_NODES_PROPS_JSON.json` - Node properties in JSON format

**Schema Files:**
- `PG_SCHEMA.txt` - Property graph schema definition

**Runtime Files:**
- `GraphNpm_RUNTIME_LOGS.csv` - Performance and runtime statistics

The output contains the following files. We have showed the content of each file with example used in the paper.
<details>
<summary>`PG_SCHEMA.txt`  A text file which contains PG-Schema for the graph (in same syntax as in published paper)</summary>

    ```json
    // Node Types
    (courseType: Course { id: 7, iri: "http://x.y/Course",  name : STRING })
    (undergraduatestudentType: UnderGraduateStudent { id: 4, iri: "http://x.y/UnderGraduateStudent" })
    (professorType: Professor { id: 12, iri: "http://x.y/Professor" })
    (addressType: Address { id: 20, iri: "http://x.y/Address",  zip : STRING,  street : STRING,  state : STRING,  country : STRING,  city : STRING })
    (gYearType: gYear { id: 33, iri: "http://www.w3.org/2001/XMLSchema#gYear" })
    (universityType: University { id: 2, iri: "http://x.y/University",  OPTIONAL name : STRING ARRAY {1, 100} })
    (personType: Person { id: 10, iri: "http://x.y/Person",  OPTIONAL age : INTEGER,  name : STRING })
    (studentType: Student { id: 14, iri: "http://x.y/Student" })
    (undergradcourseType: UnderGradCourse { id: 8, iri: "http://x.y/UnderGradCourse" })
    (graduatecourseType: GraduateCourse { id: 18, iri: "http://x.y/GraduateCourse" })
    (graduatestudentType: GraduateStudent { id: 27, iri: "http://x.y/GraduateStudent" })
    (departmentType: Department { id: 0, iri: "http://x.y/Department",  name : STRING })
    (stringType: string { id: 6, iri: "http://www.w3.org/2001/XMLSchema#string" })
    (dateType: date { id: 32, iri: "http://www.w3.org/2001/XMLSchema#date" })
    (lecturerType: Lecturer { id: 11, iri: "http://x.y/Lecturer" })
    (countryType: Country { id: 25, iri: "http://x.y/Country",  isoCode : STRING,  name : STRING })
    
     // Edge Types
    CREATE EDGE TYPE (:UniversityType)-[addressType: address { iri: "http://x.y/address" }]->(:addressType | :stringType)
    CREATE EDGE TYPE (:departmentType)-[subOrgOfType: subOrgOf { iri: "http://x.y/subOrgOf" } ]->(:universityType)
    CREATE EDGE TYPE (:UnderGraduateStudentType)-[takesCourseType: takesCourse { iri: "http://x.y/takesCourse" }]->(:stringType | :courseType | :undergradcourseType)
    CREATE EDGE TYPE (:studentType)-[studiesAtType: studiesAt { iri: "http://x.y/studiesAt" } ]->(:universityType)
    CREATE EDGE TYPE (:graduatecourseType)-[offeredByType: offeredBy { iri: "http://x.y/offeredBy" } ]->(:departmentType)
    CREATE EDGE TYPE (:addressType)-[countryType: country { iri: "http://x.y/country" } ]->(:countryType)
    CREATE EDGE TYPE (:professorType)-[worksForType: worksFor { iri: "http://x.y/worksFor" } ]->(:departmentType)
    CREATE EDGE TYPE (:UnderGraduateStudentType)-[advisedByType: advisedBy { iri: "http://x.y/advisedBy" }]->(:personType | :lecturerType | :professorType | :facultyType)
    CREATE EDGE TYPE (:lecturerType)-[worksForType: worksFor { iri: "http://x.y/worksFor" } ]->(:departmentType)
    CREATE EDGE TYPE (:ProfessorType)-[teacherOfType: teacherOf { iri: "http://x.y/teacherOf" }]->(:graduatecourseType | :courseType | :undergradcourseType)
    CREATE EDGE TYPE (:LecturerType)-[teacherOfType: teacherOf { iri: "http://x.y/teacherOf" }]->(:courseType | :undergradcourseType)
    CREATE EDGE TYPE (:GraduateStudentType)-[takesCourseType: takesCourse { iri: "http://x.y/takesCourse" }]->(:graduatecourseType | :courseType | :undergradcourseType)
    CREATE EDGE TYPE (:undergradcourseType)-[offeredByType: offeredBy { iri: "http://x.y/offeredBy" } ]->(:departmentType)
    CREATE EDGE TYPE (:GraduateStudentType)-[advisedByType: advisedBy { iri: "http://x.y/advisedBy" }]->(:personType | :professorType | :facultyType)
    CREATE EDGE TYPE (:professorType)-[docDegreeFromType: docDegreeFrom { iri: "http://x.y/docDegreeFrom" } ]->(:universityType)
    CREATE EDGE TYPE (:PersonType)-[dobType: dob { iri: "http://x.y/dob" }]->(:dateType | :gyearType | :stringType)
    
     // Cardinalities of Edges
    FOR (u: University) COUNT 0..1 OF T WITHIN (u)-[:address]->(T: {Address | String})
    FOR (d: Department) COUNT 1..1 OF u WITHIN (d)-[:subOrgOf]->(u: University)
    FOR (u: UnderGraduateStudent) COUNT 0.. OF T WITHIN (u)-[:takesCourse]->(T: {String | Course | UnderGradCourse})
    FOR (s: Student) COUNT 1..1 OF u WITHIN (s)-[:studiesAt]->(u: University)
    FOR (g: GraduateCourse) COUNT 1..1 OF d WITHIN (g)-[:offeredBy]->(d: Department)
    FOR (a: Address) COUNT 1..1 OF c WITHIN (a)-[:country]->(c: Country)
    FOR (p: Professor) COUNT 1..1 OF d WITHIN (p)-[:worksFor]->(d: Department)
    FOR (u: UnderGraduateStudent) COUNT 0.. OF T WITHIN (u)-[:advisedBy]->(T: {Person | Lecturer | Professor | Faculty})
    FOR (l: Lecturer) COUNT 1..1 OF d WITHIN (l)-[:worksFor]->(d: Department)
    FOR (p: Professor) COUNT 0.. OF T WITHIN (p)-[:teacherOf]->(T: {GraduateCourse | Course | UnderGradCourse})
    FOR (l: Lecturer) COUNT 0.. OF T WITHIN (l)-[:teacherOf]->(T: {Course | UnderGradCourse})
    FOR (g: GraduateStudent) COUNT 0.. OF T WITHIN (g)-[:takesCourse]->(T: {GraduateCourse | Course | UnderGradCourse})
    FOR (u: UnderGradCourse) COUNT 1..1 OF d WITHIN (u)-[:offeredBy]->(d: Department)
    FOR (g: GraduateStudent) COUNT 0.. OF T WITHIN (g)-[:advisedBy]->(T: {Person | Professor | Faculty})
    FOR (p: Professor) COUNT 1..1 OF u WITHIN (p)-[:docDegreeFrom]->(u: University)
    FOR (p: Person) COUNT 0..3 OF T WITHIN (p)-[:dob]->(T: {Date | GYear | String})
    ```

</details>


<details>
<summary>`PG_NODES_LITERALS.csv`  A CSV file containing literal nodes with their values </summary>

    | id:ID | object_value | object_type | type | object_iri | :LABEL |
    | --- | --- | --- | --- | --- | --- |
    | 0 | \\ | http://www.w3.org/2001/XMLSchema#string | STRING | http://x.y/UsaCountry | string;Node;KG2PG |
    | 1 | po\ns | http://www.w3.org/2001/XMLSchema#string | STRING | http://x.y/UsaCountry | string;Node;KG2PG |
    | 2 | *C. polyandra "sensu Miq., nonRoxb." | http://www.w3.org/2001/XMLSchema#string | STRING | http://x.y/MIT | string;Node;KG2PG |
    | 3 | This is a "quote" inside the description. | http://www.w3.org/2001/XMLSchema#string | STRING | http://x.y/Math101 | string;Node;KG2PG |
    | 4 | STANFORD UNIVERSITY USA | http://www.w3.org/2001/XMLSchema#string | STRING | http://x.y/Stanford | string;Node;KG2PG |
    | 5 | Massachusetts Institute of Technology | http://www.w3.org/2001/XMLSchema#string | STRING | http://x.y/MIT | string;Node;KG2PG |
    | 6 | http://dbpedia.org/resource/Steins | IRI | STRING | http://x.y/MIT | IRI;Node;KG2PG |
    | 7 | Web Engineering | http://www.w3.org/2001/XMLSchema#string | STRING | http://x.y/Bob | string;Node;KG2PG |
    | 8 | 1995-01-01 | http://www.w3.org/2001/XMLSchema#date | DATE | http://x.y/Bob | date;Node;KG2PG |
    | 9 | 1994 | http://www.w3.org/2001/XMLSchema#gYear | INT | http://x.y/John | gYear;Node;KG2PG |
    | 10 | 05-05 | http://www.w3.org/2001/XMLSchema#string | STRING | http://x.y/John | string;Node;KG2PG |
    | 11 | 450 Serra Mall, Stanford, CA 94305 | http://www.w3.org/2001/XMLSchema#string | STRING | http://x.y/Stanford | string;Node;KG2PG |
    |  |  |  |  |  |  |

</details>

<details>
<summary>
`PG_NODES_PROPS_JSON.json`  A JSON file containing key value properties of nodes </summary>

    ```json
    [
      {
        "iri": "http://x.y/Marry",
        "properties": {
          "ns2_name": "\"Marry Donaldson\""
        }
      },
      {
        "iri": "http://x.y/Bob",
        "properties": {
          "ns2_name": "\"Bob\""
        }
      },
      {
        "iri": "http://x.y/MIT",
        "properties": {
          "ns2_name": "\"Massachusetts Institute of Technology\""
        }
      },
      {
        "iri": "http://x.y/CompSciDept",
        "properties": {
          "ns2_name": "\"Department of Computer Science\""
        }
      },
      {
        "iri": "http://x.y/CompSci202",
        "properties": {
          "ns2_name": "\"Computer Science 202\""
        }
      },
      {
        "iri": "http://x.y/Math101",
        "properties": {
          "ns2_name": "\"Math 101\""
        }
      },
      {
        "iri": "http://x.y/UsaCountry",
        "properties": {
          "ns2_name": "\"United States of America\"",
          "ns2_isoCode": "\"US\""
        }
      },
      {
        "iri": "http://x.y/alice",
        "properties": {
          "ns2_name": "\"Alice Smith\""
        }
      },
      {
        "iri": "http://x.y/Stanford",
        "properties": {
          "ns2_name": "\"Stanford University\"",
          "ns2_country": "\"USA\""
        }
      },
      {
        "iri": "http://x.y/MitsAddress",
        "properties": {
          "ns2_state": "\"MA\"",
          "ns2_city": "\"\"\"Chuck Versus the Intersect\"\"\"",
          "ns2_zip": "\"02139-12\"",
          "ns2_street": "\"77 Massachusetts Avenue\""
        }
      },
      {
        "iri": "http://x.y/John",
        "properties": {
          "ns2_name": "\"John\"",
          "ns2_age": "20-2"
        }
      },
      {
        "iri": "http://x.y/MathDept",
        "properties": {
          "ns2_name": "\"Department of Mathematics\""
        }
      }
    ]
    ```

</details>
<details>
<summary>
`PG_NODES_WD_LABELS.csv`  A CSV file containing nodes with labels only (no properties, properties associated with these nodes are in the json file )</summary>

    | iri:ID | :LABEL |
    | --- | --- |
    | http://x.y/Marry | Person;Professor;Faculty;Node |
    | http://x.y/Bob | UnderGraduateStudent;Person;Student;Node |
    | http://x.y/MIT | University;Node |
    | http://x.y/CompSciDept | Department;Node |
    | http://x.y/CompSci202 | Course;UnderGradCourse;Node |
    | http://x.y/Math101 | GraduateCourse;Course;Node |
    | http://x.y/UsaCountry | Country;Node |
    | http://x.y/alice | Person;Lecturer;Professor;Faculty;Node |
    | http://x.y/Stanford | University;Node |
    | http://x.y/MitsAddress | Address;Node |
    | http://x.y/John | Person;GraduateStudent;Student;Node |
    | http://x.y/MathDept | Department;Node |
</details>
<details>
<summary>
`PG_RELATIONS.csv`  A CSV file containing [Node] -(relationship) → (node) data</summary>


    | :START_ID | property | :END_ID | :TYPE |
    | --- | --- | --- | --- |
    | http://x.y/UsaCountry | http://dbpedia.org/ontology/wrong | 0 | ns0_wrong |
    | http://x.y/UsaCountry | http://www.w3.org/2000/01/rdf-schema#label | 1 | ns1_label |
    | http://x.y/MIT | http://dbpedia.org/ontology/synonym | 2 | ns0_synonym |
    | http://x.y/Math101 | http://www.w3.org/2000/01/rdf-schema#label | 3 | ns1_label |
    | http://x.y/Stanford | http://www.w3.org/2000/01/rdf-schema#label | 4 | ns1_label |
    | http://x.y/MIT | http://www.w3.org/2000/01/rdf-schema#label | 5 | ns1_label |
    | http://x.y/MIT | http://www.w3.org/2000/01/rdf-schema#seeAlso | 6 | ns1_seeAlso |
    | http://x.y/Bob | http://x.y/takesCourse | http://x.y/CompSci202 | ns2_takesCourse |
    | http://x.y/Bob | http://x.y/takesCourse | 7 | ns2_takesCourse |
    | http://x.y/John | http://x.y/takesCourse | http://x.y/Math101 | ns2_takesCourse |
    | http://x.y/John | http://x.y/takesCourse | http://x.y/CompSci202 | ns2_takesCourse |
    | http://x.y/Bob | http://x.y/advisedBy | http://x.y/alice | ns2_advisedBy |
    | http://x.y/John | http://x.y/advisedBy | http://x.y/Marry | ns2_advisedBy |
    | http://x.y/Bob | http://x.y/studiesAt | http://x.y/MIT | ns2_studiesAt |
    | http://x.y/John | http://x.y/studiesAt | http://x.y/MIT | ns2_studiesAt |
    | http://x.y/alice | http://x.y/worksFor | http://x.y/CompSciDept | ns2_worksFor |
    | http://x.y/Marry | http://x.y/worksFor | http://x.y/MathDept | ns2_worksFor |
    | http://x.y/alice | http://x.y/docDegreeFrom | http://x.y/MIT | ns2_docDegreeFrom |
    | http://x.y/Marry | http://x.y/docDegreeFrom | http://x.y/Stanford | ns2_docDegreeFrom |
    | http://x.y/alice | http://x.y/teacherOf | http://x.y/CompSci202 | ns2_teacherOf |
    | http://x.y/Marry | http://x.y/teacherOf | http://x.y/Math101 | ns2_teacherOf |
    | http://x.y/Bob | http://x.y/dob | 8 | ns2_dob |
    | http://x.y/John | http://x.y/dob | 9 | ns2_dob |
    | http://x.y/John | http://x.y/dob | 10 | ns2_dob |
    | http://x.y/MathDept | http://x.y/subOrgOf | http://x.y/MIT | ns2_subOrgOf |
    | http://x.y/CompSciDept | http://x.y/subOrgOf | http://x.y/MIT | ns2_subOrgOf |
    | http://x.y/Stanford | http://x.y/address | 11 | ns2_address |
    | http://x.y/MIT | http://x.y/address | http://x.y/MitsAddress | ns2_address |
    | http://x.y/MitsAddress | http://x.y/country | http://x.y/UsaCountry | ns2_country |
    | http://x.y/Math101 | http://x.y/offeredBy | http://x.y/MathDept | ns2_offeredBy |
    | http://x.y/CompSci202 | http://x.y/offeredBy | http://x.y/CompSciDept | ns2_offeredBy |
    |  |  |  |  |

</details>
<details>
<summary>
`PG_PREFIX_MAP.csv`  A CSV file which shows a prefix map, i.e., prefix to namespace </summary>

    | NAMESPACE | PREFIX |
    | --- | --- |
    | http://dbpedia.org/ontology/ | ns0 |
    | http://www.w3.org/2000/01/rdf-schema# | ns1 |
    | http://x.y/ | ns2 |
    |  |  |

- `Graph_RUNTIME_LOGS.csv`  Logs of runtime for each step of transformation

</details>

## 4. Loading transformed Graphs into Neo4j

Use neo4j admin import to load the files into Neo4j.


<details>
<summary>Getting started with Neo4j using Docker</summary>

- Start Neo4J instance using Docker

  Prerequisite:

    ```bash
    cd home/ubuntu/neo4j/
    mkdir data_enterprise
    mkdir logs
    mkdir conf
    mkdir plugins
    mkdir import
    
    cd plugins
    wget https://github.com/neo4j/apoc/releases/download/5.11.0/apoc-5.11.0-core.jar
    wget https://github.com/neo4j-labs/neosemantics/releases/download/5.7.0.0/neosemantics-5.7.0.0.jar
    
    chmod +x run_neo4j.sh
    ./run_neo4j.sh
    ```

    ```bash
    #!/bin/bash
    
    # Define your container name (change as needed)
    CONTAINER_NAME=neo4j
    
    # Define your Neo4j image version
    NEO4J_VERSION=5.11.0-enterprise
    
    # Define the paths to host directories
    DATA_DIR=/srv/data/iq26og/gdb/neo4j/data
    LOGS_DIR=/srv/data/iq26og/gdb/neo4j/logs
    CONF_DIR=/srv/data/iq26og/gdb/neo4j/conf
    PLUGINS_DIR=/srv/data/iq26og/gdb/neo4j/plugins
    IMPORT_DIR=/srv/data/iq26og/gdb/neo4j/import
    
    # Run the Neo4j container
    docker run \
        --detach \
        --name $CONTAINER_NAME \
        --publish=7474:7474 --publish=7687:7687 \
        --volume=$DATA_DIR:/data \
        --volume=$LOGS_DIR:/logs \
        --volume=$CONF_DIR:/conf \
        --volume=$PLUGINS_DIR:/plugins \
        --volume=$IMPORT_DIR:/var/lib/neo4j/import \
        --env NEO4J_dbms_memory_pagecache_size=30G \
        --env NEO4J_server_memory_heap_initial__size=10G \
        --env NEO4J_server_memory_heap_max__size=120G \
        --env NEO4J_dbms.unmanaged_extension_classes=n10s.endpoint=/rdf \
        --env=NEO4J_ACCEPT_LICENSE_AGREEMENT=yes \
        --env=NEO4J_apoc_import_file_enabled=true \
        neo4j:$NEO4J_VERSION
    ```
</details>

<details>

<summary>Load Nodes and Relationships in Neo4j using Admin Import</summary>


    ```bash
    #info: name of container running neo4j database is neo4j
    
    # **Run the following command to import nodes and relationships:**
    
    docker exec --interactive --tty neo4j_dbp22_s3pg neo4j-admin database import full --delimiter="|" --array-delimiter=";" --nodes=import/dbpedia2022/PG_NODES_LITERALS.csv --nodes=import/dbpedia2022/PG_NODES_WD_LABELS.csv --relationships=import/dbpedia2022/PG_RELATIONS.csv  dbp22s3pg &> dbp22s3pg.log
    
    # Then run the following command, if it exits, remove it and run run_neo4j.sh shell script again (mentioned in previous step)
    
    docker restart neo4j_dbp22_s3pg
    
    # then run the following to enter into cypher-shell
    
    docker exec --interactive --tty neo4j_dbp22_s3pg cypher-shell
    
    # enter username and credentials
    
    # execute the following
    
    SHOW DATABASE;
    CREATE DATABASE dbp22s3pg;
    
    SHOW DATABASE;
    
    CTRL + D; # exit cypher shell;
    # Now visit http://a256-gc1-17.srv.aau.dk:7474/browser/ where your neo4j browser is running 
    ```
</details>


<details>
<summary> Load Key Value Properties of Nodes</summary>

    ```bash
    CREATE INDEX node_range_index_iri FOR (n:Node) ON (n.iri)
    
    CREATE TEXT INDEX node_text_index_iri FOR (n:Node) ON (n.iri)
    
    CALL apoc.load.json("file:///import/dbpedia2022/PG_NODES_PROPS_JSON.json") 
    YIELD value
    MATCH (n:Node) USING INDEX n:Node(iri)
    WHERE n.iri = value.iri
    SET n += value.properties;
    ```
</details>

<details>
<summary>Execute the following script to load key value props in batches.</summary>
 
  ```bash
  #!/bin/bash
  
  # Set the directory containing JSON files
  json_directory="splitted_json/"
  
  # Set the Cypher query template
  query_template='CALL apoc.load.json("file:///import/dbpedia2022/splitted_json/%s") YIELD value MATCH (n:Node) USING INDEX n:Node(iri) WHERE n.iri = value.iri SET n += value.properties;'
  
  # Set the Neo4j credentials and database
  neo4j_user="neo4j"
  neo4j_password="12345678"
  neo4j_database="dbpedia2022kg2pg12oct"
  
  # Log file
  log_file="apoc_query_logs.txt"
  
  # Remove existing log file
  rm -f "$log_file"
  
  # Initialize a variable to track total execution time
  total_execution_time=0
  
  # Loop through JSON files and execute the query for each file
  for json_file in "${json_directory}"split_file_*.json; do
      file_name=$(basename "$json_file")
      query=$(printf "$query_template" "$file_name")
      
      # Print the query before execution
      echo "$query"
      
      start_time=$(date +%s)
      docker exec --interactive --tty neo4j_db cypher-shell -u "$neo4j_user" -p "$neo4j_password" -d "$neo4j_database" "$query" >> "$log_file" 2>&1
      end_time=$(date +%s)
      execution_time=$((end_time - start_time))
      echo "Query executed on $json_file in $execution_time seconds." >> "$log_file"
      
      # Add the execution time to the total execution time
      total_execution_time=$((total_execution_time + execution_time))
  done
  
  # Calculate total execution time in minutes
  total_execution_time_minutes=$((total_execution_time / 60))
  
  echo "All queries executed and logged in $log_file."
  echo "Total execution time for all queries: $total_execution_time seconds ($total_execution_time_minutes minutes)."
  ```
</details>

## 5. Running Queries

Queries are available in the [resources](https://github.com/dkw-aau/KG2PG/tree/master/src/main/resources) directory.

Once you have loaded PG into Neo4j. Next step is to run queries, use the benchmark() method in the Main file to run the queries.

---

## 🆚 Comparison: JAR vs Traditional Build

| Feature | JAR Release | Traditional Build |
|---------|-------------|------------------|
| **Setup Time** | < 1 minute | ~10-15 minutes |
| **Prerequisites** | Java 17+ only | Java + Gradle + Dependencies |
| **Download Size** | 161MB (single file) | Full repository clone |
| **Configuration** | Optional (embedded defaults) | Required (config files) |
| **Data Setup** | Optional (embedded samples) | Required (download datasets) |
| **Output Organization** | Automatic timestamped dirs | Manual organization |
| **Cross-Platform** | ✅ Same file everywhere | ❓ Platform-specific builds |
| **Docker Support** | ✅ Multi-architecture | ✅ Single architecture |
| **Best For** | Quick testing, Production use | Development, Customization |

---

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/dkw-aau/KG2PG/issues)
- **Discussions**: [GitHub Discussions](https://github.com/dkw-aau/KG2PG/discussions)
- **Documentation**: [JAR_USAGE.md](JAR_USAGE.md)

---

## 🏆 Citation

If you use KG2PG in your research, please cite our work:

```bibtex
@article{kg2pg2024,
  title={KG2PG: Knowledge Graph to Property Graph Transformation using S3PG},
  author={Your Authors},
  journal={Your Journal},
  year={2024}
}
```

---

*Made with ❤️ for the Knowledge Graph community*