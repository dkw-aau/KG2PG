#!/usr/bin/env python3
"""
Script to count statistics from KG2PG CSV data files.
Counts:
- Number of nodes
- Number of edges (relations)
- Number of relation types
"""

import os
import sys
import csv
import time
from pathlib import Path
from collections import Counter


def print_usage():
    """Print usage instructions."""
    print("\nUsage: python3 count_pg_stats.py [OPTIONS] [DIRECTORY]")
    print("\nOptions:")
    print("  -v, --verbose    Show detailed relation types distribution")
    print("  -h, --help       Show this help message")
    print("\nArguments:")
    print("  DIRECTORY        Path to directory containing PG CSV files")
    print("                   (PG_NODES_LITERALS.csv, PG_NODES_WD_LABELS.csv, PG_RELATIONS.csv)")
    print("\nBehavior:")
    print("  - If DIRECTORY is provided: Searches that directory for PG files")
    print("  - If no DIRECTORY: Searches the default 'output' folder in project root")
    print("  - The STATISTICS.md report will be created in the same directory as the PG files")
    print("\nExamples:")
    print("  python3 count_pg_stats.py")
    print("  python3 count_pg_stats.py /path/to/output/folder")
    print("  python3 count_pg_stats.py --verbose /path/to/output/folder")
    print()


def print_progress(message, level="INFO"):
    """Print progress message with timestamp."""
    timestamp = time.strftime("%Y-%m-%d %H:%M:%S")
    print(f"[{timestamp}] {level}: {message}", flush=True)


def get_unique_node_ids(nodes_file):
    """Get unique node IDs from a CSV file (matching Neo4j deduplication)."""
    node_ids = set()
    try:
        with open(nodes_file, 'r', encoding='utf-8') as f:
            reader = csv.reader(f, delimiter='|')
            header = next(reader)  # Read header to find ID column
            
            # Find the index of the id:ID column
            id_index = 0
            for idx, col_name in enumerate(header):
                if col_name.strip() == 'id:ID':
                    id_index = idx
                    break
            
            row_count = 0
            skipped = 0
            for row in reader:
                row_count += 1
                if row_count % 500000 == 0:
                    print_progress(f"    Processed {row_count:,} rows from {os.path.basename(nodes_file)}...", "DEBUG")
                
                if id_index < len(row):
                    node_id = row[id_index].strip()
                    # Only add non-empty IDs (matching Neo4j behavior)
                    if node_id and node_id != '':
                        node_ids.add(node_id)
                    else:
                        skipped += 1
                else:
                    skipped += 1
        
        print_progress(f"  Total rows: {row_count:,}, Valid IDs: {len(node_ids):,}, Skipped: {skipped:,}")
        return node_ids
    except FileNotFoundError:
        return set()
    except Exception as e:
        print_progress(f"Error reading {nodes_file}: {e}", "ERROR")
        return set()


def count_nodes(output_dir):
    """Count the total number of unique valid nodes (like Neo4j does)."""
    print_progress(f"Reading node files from: {output_dir}")
    
    literals_file = os.path.join(output_dir, 'PG_NODES_LITERALS.csv')
    labels_file = os.path.join(output_dir, 'PG_NODES_WD_LABELS.csv')
    
    all_node_ids = set()
    literals_ids = set()
    labels_ids = set()
    
    # Get unique IDs from literals file
    if os.path.exists(literals_file):
        print_progress(f"  Reading: PG_NODES_LITERALS.csv")
        literals_ids = get_unique_node_ids(literals_file)
        all_node_ids.update(literals_ids)
        print_progress(f"  Valid nodes in PG_NODES_LITERALS.csv: {len(literals_ids):,}")
    else:
        print_progress(f"  PG_NODES_LITERALS.csv not found", "WARNING")
    
    # Get unique IDs from labels file
    if os.path.exists(labels_file):
        print_progress(f"  Reading: PG_NODES_WD_LABELS.csv")
        labels_ids = get_unique_node_ids(labels_file)
        all_node_ids.update(labels_ids)
        print_progress(f"  Valid nodes in PG_NODES_WD_LABELS.csv: {len(labels_ids):,}")
    else:
        print_progress(f"  PG_NODES_WD_LABELS.csv not found", "WARNING")
    
    total_unique_nodes = len(all_node_ids)
    print_progress(f"Total unique nodes in files: {total_unique_nodes:,}")
    return total_unique_nodes, all_node_ids


def count_edges_and_relation_types(relations_file, valid_node_ids):
    """Count the number of valid edges and unique relation types from PG_RELATIONS.csv file.
    Only counts edges where both START_ID and END_ID match valid node IDs (Neo4j import behavior)."""
    edge_count = 0
    valid_edges = 0
    invalid_edges = 0
    orphaned_nodes_start = set()
    orphaned_nodes_end = set()
    relation_types = Counter()
    
    try:
        print_progress(f"Reading relations file: {relations_file}")
        with open(relations_file, 'r', encoding='utf-8') as f:
            reader = csv.DictReader(f, delimiter='|')
            
            for row in reader:
                edge_count += 1
                
                # Get node IDs
                start_id = row.get(':START_ID', '').strip()
                end_id = row.get(':END_ID', '').strip()
                rel_type = row.get(':TYPE', '').strip()
                
                # Check if both nodes exist (matching Neo4j import behavior)
                start_exists = start_id in valid_node_ids
                end_exists = end_id in valid_node_ids
                
                if start_exists and end_exists and rel_type:
                    valid_edges += 1
                    relation_types[rel_type] += 1
                else:
                    invalid_edges += 1
                    if not start_exists:
                        orphaned_nodes_start.add(start_id)
                    if not end_exists:
                        orphaned_nodes_end.add(end_id)
                
                if edge_count % 100000 == 0:
                    print_progress(f"  Processed {edge_count:,} edges...", "DEBUG")
        
        print_progress(f"Total edges in file: {edge_count:,}")
        print_progress(f"Valid edges (both node IDs exist): {valid_edges:,}")
        print_progress(f"Invalid edges (orphaned node references): {invalid_edges:,}")
        
        # Calculate orphaned node counts
        total_orphaned = len(orphaned_nodes_start.union(orphaned_nodes_end))
        print_progress(f"Nodes referenced but not found in node files: {total_orphaned:,}")
        print_progress(f"Unique relation types: {len(relation_types)}")
        
        return valid_edges, len(relation_types), relation_types, total_orphaned
    except FileNotFoundError:
        print_progress(f"Relations file not found: {relations_file}", "WARNING")
        return 0, 0, Counter(), 0
    except Exception as e:
        print_progress(f"Error reading relations file: {e}", "ERROR")
        return 0, 0, Counter(), 0


def generate_markdown_report(output_dir, num_nodes, num_edges, num_relation_types, relation_types, elapsed_time, orphaned_nodes=0):
    """Generate and save a markdown report of the statistics."""
    report_file = os.path.join(output_dir, 'STATISTICS.md')
    
    try:
        with open(report_file, 'w', encoding='utf-8') as f:
            f.write("# Graph Statistics Report\n\n")
            f.write(f"**Generated:** {time.strftime('%Y-%m-%d %H:%M:%S')}\n\n")
            f.write(f"**Directory:** `{output_dir}`\n\n")
            
            f.write("## Summary Statistics\n\n")
            f.write("| Metric | Count |\n")
            f.write("|--------|-------|\n")
            f.write(f"| Nodes in CSV files | {num_nodes:,} |\n")
            f.write(f"| Orphaned node references | {orphaned_nodes:,} |\n")
            f.write(f"| Number of Edges | {num_edges:,} |\n")
            f.write(f"| Number of Relation Types | {num_relation_types} |\n")
            f.write(f"| Processing Time | {elapsed_time:.2f}s |\n\n")
            
            f.write("## Notes\n\n")
            f.write("- **Orphaned node references**: Node IDs referenced in relationships but not found in node CSV files.\n")
            f.write("- **Neo4j import**: The `neo4j-admin database import` command only creates relationships when both START_ID and END_ID match nodes in the node files.\n")
            f.write("- **Edge count**: Only counts edges where both node references are valid.\n\n")
            
            if relation_types:
                f.write("## Relation Types Distribution\n\n")
                f.write("| Relation Type | Count |\n")
                f.write("|---|---|\n")
                for rel_type, count in relation_types.most_common():
                    f.write(f"| `{rel_type}` | {count:,} |\n")
        
        print_progress(f"Report saved to: {report_file}")
        return report_file
    except Exception as e:
        print_progress(f"Error saving report to {report_file}: {e}", "ERROR")
        return None


def print_statistics(output_dir, verbose=False):
    """Print statistics for a given output directory."""
    start_time = time.time()
    print_progress(f"Processing directory: {os.path.basename(output_dir)}")
    
    relations_file = os.path.join(output_dir, 'PG_RELATIONS.csv')
    
    # Count nodes from both files
    num_nodes, valid_node_ids = count_nodes(output_dir)
    
    # Count edges and relation types (only valid edges with matching node references)
    num_edges, num_relation_types, relation_types, orphaned_nodes = count_edges_and_relation_types(relations_file, valid_node_ids)
    
    elapsed_time = time.time() - start_time
    
    # Calculate nodes actually used in relationships
    nodes_used_in_rels = num_nodes - orphaned_nodes if orphaned_nodes < num_nodes else 0
    
    # Print results to console
    print(f"\n{'='*60}")
    print(f"Statistics for: {output_dir}")
    print(f"{'='*60}")
    print(f"Number of Nodes (in files):     {num_nodes:>15,}")
    print(f"Nodes with invalid refs:        {orphaned_nodes:>15,}")
    print(f"Number of Edges:                {num_edges:>15,}")
    print(f"Number of Relation Types:       {num_relation_types:>15}")
    print(f"Processing Time:                {elapsed_time:>15.2f}s")
    print(f"{'='*60}")
    print(f"\nNote: Neo4j import only includes nodes/edges with valid ID")
    print(f"references. Discrepancies may come from orphaned node refs.")
    
    if verbose and relation_types:
        print("\nRelation Types Distribution:")
        print(f"{'Relation Type':<50} Count")
        print("-" * 60)
        for rel_type, count in relation_types.most_common():
            print(f"{rel_type:<50} {count:>10,}")
    
    # Generate and save markdown report
    generate_markdown_report(output_dir, num_nodes, num_edges, num_relation_types, relation_types, elapsed_time, orphaned_nodes)


def main():
    """Main function to process all output directories."""
    # Check for help flag
    if '--help' in sys.argv or '-h' in sys.argv:
        print_usage()
        sys.exit(0)
    
    # Get the script's directory and navigate to the project root
    script_dir = Path(__file__).resolve().parent
    project_root = script_dir.parent
    base_output_dir = project_root / 'output'
    base_output_dir = str(base_output_dir)
    verbose = '--verbose' in sys.argv or '-v' in sys.argv
    
    # Check if a specific directory is provided
    target_dir = None
    for arg in sys.argv[1:]:
        if not arg.startswith('-'):
            if os.path.isdir(arg):
                target_dir = arg
                break
            else:
                print_progress(f"Provided directory does not exist: {arg}", "ERROR")
                print("\nPlease provide a valid directory path containing PG CSV files:")
                print("  - PG_NODES_LITERALS.csv")
                print("  - PG_NODES_WD_LABELS.csv (optional)")
                print("  - PG_RELATIONS.csv")
                print("\nThe STATISTICS.md file will be created in the directory where these files are found.")
                print("\nRun 'python3 count_pg_stats.py --help' for usage information.")
                sys.exit(1)
    
    script_start = time.time()
    print_progress(f"Starting script execution")
    
    # Determine which directory to search in
    if target_dir:
        print_progress(f"Searching in target directory: {target_dir}")
        search_dir = target_dir
    else:
        if not os.path.exists(base_output_dir):
            print_progress(f"Default output directory not found: {base_output_dir}", "ERROR")
            print("\nYou have two options:")
            print("  1. Create the output directory and run KG2PG to generate PG files")
            print("  2. Provide a specific directory path as an argument:")
            print(f"     python3 {sys.argv[0]} /path/to/directory/with/pg/files")
            print("\nThe directory should contain:")
            print("  - PG_NODES_LITERALS.csv")
            print("  - PG_NODES_WD_LABELS.csv (optional)")
            print("  - PG_RELATIONS.csv")
            print("\nRun 'python3 count_pg_stats.py --help' for more information.")
            sys.exit(1)
        print_progress(f"Searching in base directory: {base_output_dir}")
        search_dir = base_output_dir
    
    # Find all graph output directories
    output_dirs = []
    
    print_progress(f"Looking for directories with PG_NODES_LITERALS.csv and PG_RELATIONS.csv")
    
    for root, dirs, files in os.walk(search_dir):
        if 'PG_NODES_LITERALS.csv' in files and 'PG_RELATIONS.csv' in files:
            output_dirs.append(root)
            print_progress(f"Found graph directory: {os.path.basename(root)}")
    
    if not output_dirs:
        print_progress(f"No directories with PG CSV files found in: {search_dir}", "ERROR")
        print("\nExpected files in a directory:")
        print("  - PG_NODES_LITERALS.csv")
        print("  - PG_NODES_WD_LABELS.csv (optional)")
        print("  - PG_RELATIONS.csv")
        print("\nPossible solutions:")
        print("  1. Run KG2PG to generate the PG CSV files first")
        print("  2. Check if you provided the correct directory path")
        print("  3. Verify the CSV files exist and have the correct names")
        print("\nRun 'python3 count_pg_stats.py --help' for usage information.")
        sys.exit(1)
    
    # Sort directories for consistent output
    output_dirs.sort()
    
    print_progress(f"Total directories found: {len(output_dirs)}\n")
    
    # Process each directory
    for idx, output_dir in enumerate(output_dirs, 1):
        print_progress(f"[{idx}/{len(output_dirs)}] Processing...")
        print_statistics(output_dir, verbose)
        print()  # Blank line between results
    
    total_time = time.time() - script_start
    print_progress(f"Script completed in {total_time:.2f}s")
    print_progress(f"Total directories processed: {len(output_dirs)}")


if __name__ == '__main__':
    main()
