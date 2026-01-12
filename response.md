Dear Reviewer,

Thank you for your detailed feedback and for the time spent investigating the reproduction of our results. We sincerely appreciate your patience regarding the script behavior and the clarity of the results.

We have addressed your concerns as follows:

## 1. Correspondence of Results and Table 5
Regarding the numbers you obtained:

Bio2RDF: The discrepancy you noticed (e.g., the 5.8M vs. the 26M+16M+16M reported in Table 5) was primarily due to a configuration flag in Main.java called isParsimonious. This flag was incorrectly set to false in the repository, but it must be true to produce the parsimonious model results reported in the paper.

Correction: With the flag now set to true, the Bio2RDF counts match the paper exactly. We have updated the REPRODUCIBILITY.md file with a detailed mapping that explains exactly which output files correspond to which metrics in Table 5 to facilitate verification.

DBpedia 2020: We have identified a discrepancy in this specific dataset. It appears the version currently hosted in our storage bucket is slightly different from the one used during the final evaluation of the paper. We have documented the current statistics for this version in the README and are working to restore the original 2020 version. However, DBpedia 2022 and Bio2RDF now match the paper’s reported results perfectly.

## 2. Improvements to Scripting and Progress Monitoring
We apologize for the confusion caused by the background job handling. The behavior you described—where scripts exited while jobs were still running—was indeed misleading.

Updated Logic: We have overhauled the execution scripts (run_bio2rdf.sh, etc.). The scripts now properly block and wait for the Docker containers to complete their tasks before exiting.

Progress Visibility: We have implemented real-time status updates within the terminal. The script now prints the elapsed time and current container status every minute, ensuring the user knows the process is still active. Example given below

New Verification Tool: We have added a Python script (scripts/count_pg_stats.py) that computes the final statistics directly from the generated CSV files. This allows for quick verification of node and edge counts without requiring a full Neo4j import.

```
=========================================
⚠️  IMPORTANT: Container is running in background
⚠️  This script will WAIT until processing completes
⚠️  Do NOT terminate this script prematurely
=========================================

Started at: Mon Jan 12 13:07:58 UTC 2026

⏳ [13:08:00] Elapsed: 0 min - Container is actively processing...
⏳ [13:09:01] Elapsed: 1 min - Container is actively processing...
⏳ [13:10:03] Elapsed: 2 min - Container is actively processing...
⏳ [13:11:04] Elapsed: 3 min - Container is actively processing...
⏳ [13:12:06] Elapsed: 4 min - Container is actively processing...
⏳ [13:13:07] Elapsed: 5 min - Container is actively processing...
   📋 Recent activity:
      13:08:12.288 [main] INFO ROOT - SHACL shapes read successfully, parsing SHACL shapes...
      13:08:12.440 [main] INFO ROOT - SHACL shapes parsed successfully, writing PG-Schema to file...
      13:08:12.605 [main] INFO ROOT - Phase 1: Graph Data Translation - Extracting entities data from RDF file.
⏳ [13:14:09] Elapsed: 6 min - Container is actively processing...
⏳ [13:15:11] Elapsed: 7 min - Container is actively processing...
⏳ [13:16:12] Elapsed: 8 min - Container is actively processing...
⏳ [13:17:14] Elapsed: 9 min - Container is actively processing...

```

## 3. Documentation Updates
We have updated the REPRODUCIBILITY.md to:

- Explicitly map the generated CSV files (e.g., PG_NODES_WD_LABELS.csv) to the metrics used in the paper.
- Provide examples of expected outputs to guide the user.

We believe these changes significantly improve the usability of the tool and the clarity of the experimental results. Please let us know if any further clarification is required.