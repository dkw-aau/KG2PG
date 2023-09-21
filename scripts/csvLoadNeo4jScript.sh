######### Example Student Graph
docker exec --interactive --tty neo4j neo4j-admin database import full --delimiter="|" --array-delimiter=";" --nodes=import/PG_NODES_WD_PROP.csv --nodes=import/PG_NODES_LITERALS.csv --relationships=import/PG_RELATIONS.csv --ignore-extra-columns=true --skip-bad-relationships=true  studentgraph2


#IMPORT DONE in 2s 840ms.
#Imported:
#  23 nodes
#  30 relationships
#  100 properties

######### DBpedia = Importing only PG_NODES_WD_PROP

docker exec --interactive --tty neo4j neo4j-admin database import full --delimiter="|" --array-delimiter=";" --nodes=import/dbpedia/PG_NODES_WD_PROP.csv  --ignore-extra-columns=true  dbpediapgnodeswdprop &> dbpediapgnodeswdprop_loading.log


#IMPORT DONE in 55s 862ms.
#Imported:
#  5823566 nodes
#  0 relationships
#  22548566 properties
#Peak memory usage: 1.063GiB

######### DBpedia = Importing only PG_NODES_LITERALS
docker exec --interactive --tty neo4j neo4j-admin database import full --delimiter="|" --array-delimiter=";" --nodes=import/dbpedia/PG_NODES_LITERALS.csv  --ignore-extra-columns=true  dbpediapgnodesliteral &> dbpediapgnodesliteral_loading.log

#IMPORT DONE in 23s 927ms.
#Imported:
#  6385595 nodes
#  0 relationships
#  25542380 properties
#Peak memory usage: 1.072GiB
#

######### DBpedia = Importing everthing without skipping columns or bad relationships.
docker exec --interactive --tty neo4j neo4j-admin database import full --delimiter="|" --array-delimiter=";" --nodes=import/dbpedia/PG_NODES_LITERALS.csv --nodes=import/dbpedia/PG_NODES_WD_PROP.csv --relationships=import/dbpedia/PG_RELATIONS.csv  dbpediad21sept1207 &> dbpediad21sept1207_loading.log
#IMPORT DONE in 1m 38s 152ms.
#Imported:
#  12188538 nodes
#  18954099 relationships
#  66962553 properties
#Peak memory usage: 1.146GiB


############################################################################################################################################################################

docker exec --interactive --tty neo4j neo4j-admin database import full --delimiter="|" --array-delimiter=";" --nodes=import/dbpedia/PG_NODES_LITERALS.csv --nodes=import/dbpedia/PG_NODES_WD_PROP.csv --relationships=import/dbpedia/PG_RELATIONS.csv --ignore-extra-columns=true --skip-bad-relationships=true  dbpediad21sept &> dbpedia21sept_loading.log



docker exec --interactive --tty neo4j neo4j-admin database import full --delimiter="|" --array-delimiter=";" --nodes=import/dbpedia/PG_NODES_LITERALS.csv --nodes=import/dbpedia/PG_NODES_WD_PROP.csv --ignore-extra-columns=true   dbpediaWithoutRel &> dbpediaWithoutRel_loading.log


docker exec --interactive --tty neo4j neo4j-admin database import incremental --stage=prepare --delimiter="|" --array-delimiter=";" --nodes=import/dbpedia/PG_NODES_LITERALS.csv --nodes=import/dbpedia/PG_NODES_WD_PROP.csv --relationships=import/dbpedia/PG_RELATIONS.csv --skip-bad-relationships=true dbpediaWithoutRel --force  &> dbpediaWithoutRel_loading.log




neo4j@system> STOP DATABASE db1 WAIT;
...
$ bin/neo4j-admin database import incremental --stage=all --nodes=N1=../../raw-data/incremental-import/b.csv db1

Missing required option: '--nodes=[<label>[:<label>]...=]<files>'