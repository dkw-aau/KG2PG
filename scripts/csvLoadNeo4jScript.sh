docker exec --interactive --tty neo4j neo4j-admin database import full --delimiter="|" --array-delimiter=";" --nodes=import/PG_NODES_WD_PROP.csv --nodes=import/PG_NODES_LITERALS.csv --relationships=import/PG_RELATIONS.csv --ignore-extra-columns=true --skip-bad-relationships=true  studentgraph2


#IMPORT DONE in 2s 840ms.
#Imported:
#  23 nodes
#  30 relationships
#  100 properties




docker exec --interactive --tty neo4j neo4j-admin database import full --delimiter="|" --array-delimiter=";" --nodes=import/dbpedia/PG_NODES_LITERALS.csv --nodes=import/dbpedia/PG_NODES_WD_PROP.csv --relationships=import/dbpedia/PG_RELATIONS.csv --ignore-extra-columns=true --skip-bad-relationships=true  dbpediaTest &> dbpediaTest_loading.log



docker exec --interactive --tty neo4j neo4j-admin database import full --delimiter="|" --array-delimiter=";" --nodes=import/dbpedia/PG_NODES_LITERALS.csv --nodes=import/dbpedia/PG_NODES_WD_PROP.csv --ignore-extra-columns=true   dbpediaWithoutRel &> dbpediaWithoutRel_loading.log


docker exec --interactive --tty neo4j neo4j-admin database import incremental --stage=prepare --delimiter="|" --array-delimiter=";" --nodes=import/dbpedia/PG_NODES_LITERALS.csv --nodes=import/dbpedia/PG_NODES_WD_PROP.csv --relationships=import/dbpedia/PG_RELATIONS.csv --skip-bad-relationships=true dbpediaWithoutRel --force  &> dbpediaWithoutRel_loading.log




neo4j@system> STOP DATABASE db1 WAIT;
...
$ bin/neo4j-admin database import incremental --stage=all --nodes=N1=../../raw-data/incremental-import/b.csv db1

Missing required option: '--nodes=[<label>[:<label>]...=]<files>'