package cs.schemaTranslation.pgSchema;

import kotlin.Pair;

import java.util.*;

public class PgSchema {

    Map<Integer, Set<Integer>> nodesToEdges;
    Map<Pair<Integer, Integer>, Integer> nodeEdgeTarget;

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
        nodeEdgeTarget.put(new Pair<>(sourcePgNode.getId(), pgEdge.getId()), targetPgNode.getId());
    }

    public Map<Integer, Set<Integer>> getNodesToEdges() {
        return nodesToEdges;
    }

    public Map<Pair<Integer, Integer>, Integer> getNodeEdgeTarget() {
        return nodeEdgeTarget;
    }
}
