package cs.schemaTranslation.pgSchema;

import kotlin.Pair;
import org.eclipse.rdf4j.query.algebra.In;

import java.util.*;

public class PgSchema {

    Map<Integer, List<Integer>> nodesToEdges; // nodeId -> edgeIds
    Map<Pair<Integer, Integer>, Set<Integer>> nodeEdgeTarget; // <nodeId, edgeId> -> targetNodeIds
    Map<Pair<Integer, Integer>, Pair<Integer, Integer>> nodeEdgeCardinality; // <nodeId, edgeId> -> <minCount, maxCount>
    Map<Pair<Integer, Integer>, Boolean> pgNodeEdgeBooleanMap; // <nodeId, edgeId> -> isLiteral -- this does not fail when there are multiple edges with different ids


    public PgSchema() {
        nodesToEdges = new HashMap<>();
        nodeEdgeTarget = new HashMap<>();
        nodeEdgeCardinality = new HashMap<>();
        pgNodeEdgeBooleanMap = new HashMap<>();
    }

    public void addNode(PgNode pgNode) {
        nodesToEdges.put(pgNode.getId(), new ArrayList<>());
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

    public Map<Integer, List<Integer>> getNodesToEdges() {
        return nodesToEdges;
    }

    public Map<Pair<Integer, Integer>, Set<Integer>> getNodeEdgeTarget() {
        return nodeEdgeTarget;
    }

    public Map<Pair<Integer, Integer>, Boolean> getPgNodeEdgeBooleanMap() {
        return pgNodeEdgeBooleanMap;
    }

    public Map<Pair<Integer, Integer>, Pair<Integer, Integer>> getNodeEdgeCardinalityMap() {
        return nodeEdgeCardinality;
    }
}
/*

class NodeEdgeData {
    Set<Integer> targetNodeIds;
    Pair<Integer, Integer> cardinality;

    public Set<Integer> getTargetNodeIds() {
        return targetNodeIds;
    }

    public void setTargetNodeIds(Set<Integer> targetNodeIds) {
        this.targetNodeIds = targetNodeIds;
    }

    public Pair<Integer, Integer> getCardinality() {
        return cardinality;
    }

    public void setCardinality(Pair<Integer, Integer> cardinality) {
        this.cardinality = cardinality;
    }
}*/
