package com.u.dag.graph;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.u.dag.node.Node;
import java.util.List;

/**
 * Created by saguilera on 6/10/17.
 */

public interface Graph {

    /**
     * Add a node to the graph
     * @param node
     */
    void add(@NonNull Node node);

    /**
     * Connect from a node to another one
     * @param from start node
     * @param to end node
     */
    void connect(@NonNull Node from, @NonNull Node to);

    /**
     * Get the root node. The entry point of the graph
     * @return root node or null if no nodes exist
     */
    @Nullable Node getRoot();

    /**
     * Get all incoming edges (connections) of a node
     * @param node to analyze
     * @return List of nodes
     */
    @Nullable List<Node> getIncomingEdges(@NonNull Node node);

    /**
     * Get all outgoing edges (connections) of a node
     * @param node to analyze
     * @return List of nodes
     */
    @Nullable List<Node> getOutgoingEdges(@NonNull Node node);

    /**
     * Return all nodes sorted from start to end.
     * EG:
     * A -> B -> D -> E
     *  \_> C
     *
     * sort = [A, B, C, D, E]
     *
     * @return list of nodes sorted
     */
    @Nullable List<Node> getAllNodesSorted();

    /**
     * Clear the graph
     */
    void clear();

    /**
     * If the graph contains the node
     * @param node to analyze
     * @return true if is present, false otherwise
     */
    boolean contains(@NonNull Node node);

}
