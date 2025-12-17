Hello Reviewers,

Thank you for your efforts to reproduce the results. Due to unforeseen issues, you were unable to reproduce them. I apologize for the inconvenience.

I have rerun all the experiments on December 16, 2025. Here is a guide you can follow. 

What's new? I have made the repository accessible via my server at http://130.225.39.130:8000, where you can view and download data and results. It will be taken down once the review is complete. 


Note: Please note that it is a small server, which is why I was unable to run the DBpedia2022 dataset experiment completely. However, I have made runtime statistics available from the previous server. 


### Have you placed the data and shape files correctly?

I have successfully run experiments on DBpedia2020 and Bio2RDF.
Scripts and configuration files are working as expected. If you have Docker installed, you only need to ensure that you have downloaded and placed the data and shape files in the same structure I have used:

Main directory: http://130.225.39.130:8000/data/

DBpedia 2020:
- Shapes file: http://130.225.39.130:8000/data/dbpedia/dbpedia_2020_QSE_FULL_SHACL.ttl
- Graph file: http://130.225.39.130:8000/data/dbpedia/dbpedia_ml.nt

You can use the above links to download these data and shape files if you have trouble downloading from the original sources mentioned in the README. 


Similarly, you will find shape and graph data files for DBpedia2022 at http://130.225.39.130:8000/data/dbpedia2022/ and for Bio2RDF at http://130.225.39.130:8000/data/bio2rdf/


## How to run the scripts?

If you have placed data in the appropriate directories and the data file names match the expected names, you should be able to run the following scripts:

- `/KG2PG/scripts/run_bio2rdf.sh`
- `/KG2PG/scripts/run_dbp2020.sh`
- `/KG2PG/scripts/run_dbp2022.sh`

Each script will create a Docker image and container. You do not need to worry about the script output; it should exit after starting the container. You can run `docker ps -a` and `docker logs kg2pg_container_bio2rdf` or `docker logs kg2pg_container_dbpedia2020` to verify that it is running and processing the data correctly. 

When completed, you will see a newly created directory called `output` (note: it is `output`, not `Output`). Inside it, there will be subdirectories with timestamps.
If you explore these results, you will be able to see the transformed property graphs with runtime statistics.
You can see examples on my server at `/home/ubuntu/git/KG2PG/output/bio2rdf` and `/home/ubuntu/git/KG2PG/output/DBpedia2020` 


## How to interpret results and which tables to compare?

### Runtime and memory consumption:
You can view the results in `*_RUNTIME_LOGS.csv` files (from the output directory). Compare the MinuteTotal column. 

You have to compare it with [Table 4](/images/table_4.png) of the paper.

| Dataset | Method | Second | Minute | SecondTotal | MinuteTotal | MaxCard | DatasetPath |
|---------|--------|--------|--------|-------------|-------------|---------|-------------|
| bio2rdf | entityExtraction() | 1228 | 20 | 1228 | 20 | | data/bio2rdf/bio2rdf_ct.nt |
| bio2rdf | propertiesToPgKeysAndEdges() | 1456 | 24 | 2684 | 44 | | data/bio2rdf/bio2rdf_ct.nt |
| bio2rdf | entityDataToCsvAndJson() | 20 | 0 | 2704 | 44 | | data/bio2rdf/bio2rdf_ct.nt |
| DBpedia2020Graph | entityExtraction() | 342 | 5 | 342 | 5 | | data/dbpedia/dbpedia_ml.nt |
| DBpedia2020Graph | propertiesToPgKeysAndEdges() | 97 | 1 | 439 | 6 | | data/dbpedia/dbpedia_ml.nt |
| DBpedia2020Graph | entityDataToCsvAndJson() | 8 | 0 | 447 | 6 | | data/dbpedia/dbpedia_ml.nt |
| DBpedia2022 | entityExtraction() | 384 | 6 | 384 | 6 | | data/dbpedia2022/dbpedia_latest.nt |
| DBpedia2022 | propertiesToPgKeysAndEdges() | 6804 | 113 | 7188 | 119 | | data/dbpedia2022/dbpedia_latest.nt |
| DBpedia2022 | entityDataToCsvAndJson() | 705 | 11 | 7893 | 130 | | data/dbpedia2022/dbpedia_latest.nt |



### Reproducing other results

If you want to reproduce the statistics and results presented in the tables: 

**[Table 2](/images/table_2.png):** Load the datasets in GraphDB or any other KG triplestore and run statistical queries. These are dataset statistics. 

**[Table 3](/images/table_3.png):** Load the shape files in GraphDB or any other KG triplestore and run shape statistical queries. 

**[Table 4](/images/table_4.png):** See the runtime statistics explained above.

**[Table 5, 6, & 7](/images/table_5.png)** [](/images/table_6_7.png):
Load the generated property graphs into Neo4j and follow the instructions at [NEO4J_IMPORT.md](https://github.com/dkw-aau/KG2PG/blob/master/NEO4J_IMPORT.md) to run Cypher queries and view results and statistics.


### Disclaimer

Please note the following limitations:

- **Single reproduction script:** There is no single script that reproduces all the tables presented in the paper. Each table requires different inputs and processes as outlined above.

- **Server access:** We cannot provide access to Neo4j or GraphDB servers. All authors have moved on from the University since the paper's acceptance two years ago.

- **Reproducibility statement:** If the above instructions satisfy your reproducibility requirements, we are pleased to have assisted. If additional resources are needed, please refer to the documentation and scripts provided in this repository. 