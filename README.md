# S3PG Codebase

S3PG is a Standardized SHACL Shapes-based PG transformation approach. This repository contains the source code and instructions on how to run the code. 

### Instructions to transform RDF Graph to a Property Graph 

Start by cloning the code using the following command:

```bash
git clone https://github.com/dkw-aau/KG2PG.git
```

We provide a JAR file to help user easily transform RDF data model to Property Graph data model given:

1. the input knowledge graph is in `.nt` format,
2. SHACL shapes in `.ttl` format, and
3. the config file contains correct parameters.

#### Set Config Params:
We already provide default parameters in the [config.properties](https://github.com/dkw-aau/KG2PG/blob/main/config/dbpedia.properties) file,
you only need to update the following parameters to get started:

      dataset_path=/dir_path/knowledge_graph.nt
      resources_path=/dir_path/KG2PG/src/main/resources
      output_file_path=/dir_path/KG2PG/Output/

Please replace the `dir_path` with respect to your directory.
You can specify values of support and range (as pruning thresholds) in the config file as pairs.


#### Run Jar file:
The jar file is located in [jar](https://github.com/dkw-aau/KG2PG/tree/main/jar) directory. Please execute the following command to run the jar:

```
java -jar -Xmx16g  build/libs/KG2PG.jar config.properties &> output.logs
```
You can change the value for Xmx16g according to your machine's specification. It specifies the maximum memory usage by the JVM machine to run this jar.


**Note:** KG2PG requires Java to be installed on your system to run its Jar. You can install it by following [these](https://sdkman.io/install) steps to install sdkman and execute the following commands to install the specified version of Java and Gradle.

        sdk list java
        sdk install java 17.0.2-open
        sdk use java 17.0.2-open
        sdk install gradle 7.4-rc-1


#### Output:
KG2PG will output 

---
### Reproducibility
If you want to reproduce the results of the paper, please read ???