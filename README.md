# KG2PG: Knowledge Graph to Property Graph Transformation

Transform RDF graph data models to Property Graph (PG) data models using the S3PG algorithm.

[![Latest Release](https://img.shields.io/github/v/release/dkw-aau/KG2PG)](https://github.com/dkw-aau/KG2PG/releases/latest)
[![JAR Download](https://img.shields.io/badge/Download-JAR%20v1.0.4-blue)](https://github.com/dkw-aau/KG2PG/releases/download/v1.0.4/kg2pg-v1.0.4.jar)

## 🚀 Quick Start

Test with included sample data:

```bash
git clone https://github.com/dkw-aau/KG2PG.git
cd KG2PG
docker build -t kg2pg:dockerImage .
cd scripts
./test_runningExample.sh
```

## 🔄 Reproducing Results on Datasets used in Paper

To reproduce results from the paper:

### Step 1: Download Datasets and Shapes

Download the datasets and SHACL shapes, then place them in the `data/` directory:

- **Datasets**: [DBpedia-2020](https://bitbucket.org/kashifrabbani/s3pg-dbpedia2020) | [DBpedia-2022](https://bitbucket.org/kashifrabbani/s3pg-dbpedia2022) | [Bio2RDF](https://bitbucket.org/kashifrabbani/s3pg-bio2rdf-ct)
- **SHACL Shapes**: [Download here](https://bitbucket.org/kashifrabbani/s3pg-shacl/src/master/)

### Step 2: Run with Docker

Configuration files (`config/*.properties`) and shell scripts (`scripts/*.sh`) are already provided. Simply run:

```bash
docker build -t kg2pg:dockerImage .
cd scripts
./run_bio2rdf.sh      # For Bio2RDF dataset
./run_dbp2020.sh      # For DBpedia 2020
./run_dbp2022.sh      # For DBpedia 2022
```

➡️ **See [DOCKER_USAGE.md](DOCKER_USAGE.md) for complete guide**

---

## 📖 Documentation

- **[Docker Usage Guide](DOCKER_USAGE.md)** - Complete Docker setup and usage
- **[JAR Usage Guide](JAR_USAGE.md)** - Standalone JAR instructions  
- **[Output Format](OUTPUT_FORMAT.md)** - Generated files and schemas
- **[Neo4j Import](NEO4J_IMPORT.md)** - Load data into Neo4j
- **[Build Instructions](BUILD_INSTRUCTIONS.md)** - Build from source
- **[Docker Testing](DOCKER_TESTING.md)** - Troubleshooting guide

---

## 📦 JAR (Alternative)

```bash
# Download and run
wget https://github.com/dkw-aau/KG2PG/releases/download/v1.0.4/kg2pg-v1.0.4.jar
java -jar kg2pg-v1.0.4.jar
```

➡️ **See [JAR_USAGE.md](JAR_USAGE.md) for complete guide**

---

## 🛠️ Build from Source

```bash
# Build Docker image
./build-docker.sh

# Build JAR
./build-jar.sh
```

➡️ **See [BUILD_INSTRUCTIONS.md](BUILD_INSTRUCTIONS.md) for complete guide**

---

## 📁 Output Files

Each run generates:
- `PG_NODES_WD_LABELS.csv` - Node labels
- `PG_NODES_LITERALS.csv` - Literal properties  
- `PG_RELATIONS.csv` - Relationships/edges
- `PG_PREFIX_MAP.csv` - Namespace mappings
- `PG_NODES_PROPS_JSON.json` - Key-value properties
- `PG_SCHEMA.txt` - Schema definition
- `*_RUNTIME_LOGS.csv` - Performance metrics

➡️ **See [OUTPUT_FORMAT.md](OUTPUT_FORMAT.md) for detailed format specs**

---

## 📥 Neo4j Import

```bash
# Quick import
docker exec neo4j neo4j-admin database import full \
  --delimiter="|" --array-delimiter=";" \
  --nodes=import/runningExample/PG_NODES_LITERALS.csv \
  --nodes=import/runningExample/PG_NODES_WD_LABELS.csv \
  --relationships=import/runningExample/PG_RELATIONS.csv \
  neo4j
```

➡️ **See [NEO4J_IMPORT.md](NEO4J_IMPORT.md) for complete setup**

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