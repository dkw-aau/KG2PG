# Output File Format

Documentation of KG2PG output files and their structure.

## Output Directory Structure

Each run creates a **timestamped directory** to prevent conflicts:
- **Format**: `output/DatasetName_YYYY-MM-DD_HH-MM-SS_timestamp/`
- **Example**: `output/runningExample_2025-12-02_14-12-18_1764684738360/`

## Generated Files

### Core CSV Files

- **`PG_NODES_WD_LABELS.csv`** - Node labels and identifiers
- **`PG_NODES_LITERALS.csv`** - Node literal properties  
- **`PG_RELATIONS.csv`** - Relationships/edges between nodes
- **`PG_PREFIX_MAP.csv`** - Namespace prefix mappings

### Additional Files

- **`PG_NODES_PROPS_JSON.json`** - Node properties in JSON format
- **`PG_SCHEMA.txt`** - Property graph schema definition
- **`*_RUNTIME_LOGS.csv`** - Performance and runtime statistics

## File Format Examples

### PG_SCHEMA.txt

Property graph schema in human-readable format:

```json
// Node Types
(courseType: Course { id: 7, iri: "http://x.y/Course",  name : STRING })
(universityType: University { id: 2, iri: "http://x.y/University",  OPTIONAL name : STRING ARRAY {1, 100} })
(personType: Person { id: 10, iri: "http://x.y/Person",  OPTIONAL age : INTEGER,  name : STRING })

// Edge Types
CREATE EDGE TYPE (:UniversityType)-[addressType: address { iri: "http://x.y/address" }]->(:addressType | :stringType)
CREATE EDGE TYPE (:departmentType)-[subOrgOfType: subOrgOf { iri: "http://x.y/subOrgOf" } ]->(:universityType)

// Cardinalities of Edges
FOR (u: University) COUNT 0..1 OF T WITHIN (u)-[:address]->(T: {Address | String})
FOR (d: Department) COUNT 1..1 OF u WITHIN (d)-[:subOrgOf]->(u: University)
```

### PG_NODES_LITERALS.csv

CSV file containing literal nodes with their values:

| id:ID | object_value | object_type | type | object_iri | :LABEL |
| --- | --- | --- | --- | --- | --- |
| 0 | STANFORD UNIVERSITY USA | http://www.w3.org/2001/XMLSchema#string | STRING | http://x.y/Stanford | string;Node;KG2PG |
| 1 | Massachusetts Institute of Technology | http://www.w3.org/2001/XMLSchema#string | STRING | http://x.y/MIT | string;Node;KG2PG |
| 2 | 1995-01-01 | http://www.w3.org/2001/XMLSchema#date | DATE | http://x.y/Bob | date;Node;KG2PG |

### PG_NODES_PROPS_JSON.json

JSON file containing key-value properties of nodes:

```json
[
  {
    "iri": "http://x.y/MIT",
    "properties": {
      "ns2_name": "\"Massachusetts Institute of Technology\""
    }
  },
  {
    "iri": "http://x.y/Stanford",
    "properties": {
      "ns2_name": "\"Stanford University\"",
      "ns2_country": "\"USA\""
    }
  }
]
```

### PG_NODES_WD_LABELS.csv

CSV file containing nodes with labels only:

| iri:ID | :LABEL |
| --- | --- |
| http://x.y/MIT | University;Node |
| http://x.y/Bob | UnderGraduateStudent;Person;Student;Node |
| http://x.y/alice | Person;Lecturer;Professor;Faculty;Node |

### PG_RELATIONS.csv

CSV file containing [Node] -(relationship) → (node) data:

| :START_ID | property | :END_ID | :TYPE |
| --- | --- | --- | --- |
| http://x.y/Bob | http://x.y/takesCourse | http://x.y/CompSci202 | ns2_takesCourse |
| http://x.y/Bob | http://x.y/studiesAt | http://x.y/MIT | ns2_studiesAt |
| http://x.y/alice | http://x.y/worksFor | http://x.y/CompSciDept | ns2_worksFor |

### PG_PREFIX_MAP.csv

CSV file showing prefix to namespace mapping:

| NAMESPACE | PREFIX |
| --- | --- |
| http://dbpedia.org/ontology/ | ns0 |
| http://www.w3.org/2000/01/rdf-schema# | ns1 |
| http://x.y/ | ns2 |

## Runtime Logs

`*_RUNTIME_LOGS.csv` contains performance metrics for each transformation step, useful for benchmarking and optimization.
