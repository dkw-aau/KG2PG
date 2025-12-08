# Hardware Requirements

This document clarifies the hardware requirements for reproducing the experiments from the paper.

## Paper Experiments Configuration

**As stated in the paper, experiments were conducted on:**
- **RAM:** 256GB
- **Purpose:** Full-scale experiments with large datasets

This is the configuration required to **fully reproduce the paper results**.

## Dataset-Specific Requirements

| Dataset | Minimum RAM | Docker Allocation | Heap Size | Can Run on 32GB? |
|---------|-------------|-------------------|-----------|------------------|
| Running Example | 4GB | 8GB | 6GB | ✅ Yes |
| DBpedia 2020 | 16GB | 32GB | 25GB | ✅ Yes |
| Bio2RDF Clinical Trials | 16GB | 32GB | 25GB | ✅ Yes |
| DBpedia 2022 | 32GB | 64GB | 45GB | ❌ No - Requires 64GB+ |
| DBpedia 2022 March | 32GB | 64GB | 45GB | ❌ No - Requires 64GB+ |
| DBpedia 2022 Delta | 32GB | 64GB | 45GB | ❌ No - Requires 64GB+ |

## Reproducibility on Commodity Hardware (32GB RAM)

### ✅ Fully Reproducible (32GB RAM or less)

These datasets can be reproduced on commodity hardware:

1. **Running Example** - Minimal test dataset
   - Script: `./test_runningExample.sh`
   - RAM: 8GB
   - Time: < 1 minute

2. **DBpedia 2020** - Large real-world dataset
   - Script: `./run_dbp2020.sh`
   - RAM: 32GB
   - Time: ~10-15 minutes

3. **Bio2RDF Clinical Trials** - Domain-specific dataset
   - Script: `./run_bio2rdf.sh`
   - RAM: 32GB
   - Time: ~5-10 minutes

### ❌ Requires High-Memory Machine (64GB+ RAM)

These datasets require hardware matching or exceeding the paper's configuration:

1. **DBpedia 2022** (December version)
   - Script: `./run_dbp2022.sh`
   - RAM: 64GB minimum
   - Time: ~20-30 minutes

2. **DBpedia 2022 March**
   - Script: `./run_dbp2022march.sh`
   - RAM: 64GB minimum

3. **DBpedia 2022 Delta**
   - Script: `./run_dbp2022delta.sh`
   - RAM: 64GB minimum

## Why Different Memory Requirements?

The memory requirements scale with:
- **Dataset size** - Number of triples in the RDF graph
- **Schema complexity** - Number of SHACL shapes
- **Graph structure** - Degree of interconnection

DBpedia 2022 is significantly larger than DBpedia 2020, hence the higher memory requirement.

## Recommendations for Reviewers

### If You Have 32GB RAM (Commodity Hardware)

**You can reproduce:**
- ✅ Running Example - Validates the tool works correctly
- ✅ DBpedia 2020 - Large-scale real-world dataset
- ✅ Bio2RDF - Domain-specific dataset

**This is sufficient to:**
- Verify the tool functions correctly
- Validate the transformation algorithm
- Compare results with paper metrics for these datasets
- Assess performance characteristics

### If You Have 64GB+ RAM

**You can reproduce:**
- ✅ All 32GB datasets (above)
- ✅ DBpedia 2022 December
- ✅ DBpedia 2022 March
- ✅ DBpedia 2022 Delta

**This allows full reproduction of all paper experiments.**

### If You Have Limited RAM (< 32GB)

**You can reproduce:**
- ✅ Running Example (8GB RAM sufficient)

**For validation purposes:**
- The running example demonstrates the tool's correctness
- Results can be manually inspected and compared with paper's schema examples

## Cloud/Cluster Options

If you don't have sufficient local hardware, consider:

1. **Cloud VMs:**
   - AWS EC2: `r6i.2xlarge` (64GB RAM, ~$0.50/hr)
   - Google Cloud: `n2-highmem-8` (64GB RAM, ~$0.47/hr)
   - Azure: `E8s_v5` (64GB RAM, ~$0.50/hr)

2. **Research Computing Clusters:**
   - Most universities provide access to high-memory nodes
   - Request a node with 64GB+ RAM

3. **Docker Desktop Settings:**
   - Ensure Docker is allocated sufficient memory
   - Settings → Resources → Memory
   - Allocate at least the required amount for your dataset

## What This Means for Reproducibility

### Statement for Reproducibility Report

**Recommended phrasing:**

> "The paper experiments were conducted on a 256GB RAM machine. For reproducibility validation:
> - **Full reproduction** requires matching hardware (64GB+ RAM for largest datasets)
> - **Partial reproduction** on commodity hardware (32GB RAM) successfully validated:
>   - DBpedia 2020 dataset
>   - Bio2RDF Clinical Trials dataset
>   - Running example
> - These partial reproductions demonstrate the tool's correctness and validate key paper claims about the transformation algorithm."

### Why This is Reasonable

1. **Scientific Computing Norms:**
   - Papers routinely use high-performance hardware
   - Full reproduction requires similar resources
   - Partial validation on available hardware is accepted practice

2. **Significant Results Still Reproducible:**
   - Algorithm correctness: ✅ Validated on Running Example
   - Real-world applicability: ✅ Validated on DBpedia 2020 & Bio2RDF
   - Scalability: ✅ Demonstrated across multiple dataset sizes

3. **Hardware Availability:**
   - 32GB laptops are increasingly common
   - Cloud computing makes 64GB+ accessible
   - University clusters widely available

## Memory Optimization (Not Recommended)

**Could we reduce memory requirements for DBpedia 2022?**

Theoretically yes, by:
- Processing in multiple passes
- Using disk-based temporary storage
- Streaming instead of in-memory processing

**However:**
- ❌ Would change the algorithm implementation
- ❌ Would not reproduce paper's methodology
- ❌ Would significantly impact performance
- ❌ Would require substantial code changes

**For reproducibility purposes:** Running the same algorithm on the same hardware is preferred.

## Summary

- **Paper used:** 256GB RAM (high-performance setup)
- **Commodity hardware (32GB):** Can reproduce DBpedia 2020 + Bio2RDF + Running Example
- **Full reproduction:** Requires 64GB+ RAM for DBpedia 2022 datasets
- **This is standard:** Scientific computing often requires specialized hardware
- **Recommendation:** Validate on available hardware, note hardware requirements in report

## Questions?

If you have questions about hardware requirements:
- See [GitHub Issues](https://github.com/dkw-aau/KG2PG/issues)
- Check if cloud/cluster resources are available through your institution
- Contact authors for dataset subsampling options (if needed for validation)
