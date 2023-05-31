package cs.schemaTranslation.pgSchema;

import kotlin.Pair;

import java.util.*;

public class PgSchema {

    Map<Integer, Set<Integer>> nodesToEdges;
    Map<Pair<Integer, Integer>, Set<Integer>> nodeEdgeTarget;
    Set<Integer> pgEdges;

    public PgSchema() {
        nodesToEdges = new HashMap<>();
        nodeEdgeTarget = new HashMap<>();
    }

    public void addNode(PgNode pgNode) {
        nodesToEdges.put(pgNode.getId(), new HashSet<>());
    }

    public void addSourceEdge(PgNode sourcePgNode, PgEdge pgEdge) {
        nodesToEdges.get(sourcePgNode.getId()).add(pgEdge.getId());
    }

    public void addTargetEdge(PgNode sourcePgNode, PgEdge pgEdge, PgNode targetPgNode) {
        Pair<Integer, Integer> pair = new Pair<>(sourcePgNode.getId(), pgEdge.getId());
        if (nodeEdgeTarget.get(pair) != null) {
            nodeEdgeTarget.get(pair).add(targetPgNode.getId());
        } else {
            nodeEdgeTarget.put(pair, new HashSet<>());
            nodeEdgeTarget.get(pair).add(targetPgNode.getId());
        }
    }

    public Map<Integer, Set<Integer>> getNodesToEdges() {
        return nodesToEdges;
    }

    public Map<Pair<Integer, Integer>, Set<Integer>> getNodeEdgeTarget() {
        return nodeEdgeTarget;
    }

    public void postProcessPgSchema() {
        pgEdges = new HashSet<Integer>();
        for (Set<Integer> set : nodesToEdges.values()) {
            pgEdges.addAll(set);
        }
    }

    public Set<Integer> getPgEdges() {
        return pgEdges;
    }
}
