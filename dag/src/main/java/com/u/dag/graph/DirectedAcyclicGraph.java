package com.u.dag.graph;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pools;
import android.support.v4.util.SimpleArrayMap;

import com.u.dag.node.Node;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * A class which represents a simple directed acyclic graph.
 * This class is internal of android and was copied with minor tweaks added.
 */
public final class DirectedAcyclicGraph implements Graph {
    private final Pools.Pool<ArrayList<Node>> mListPool = new Pools.SimplePool<>(10);
    private final SimpleArrayMap<Node, ArrayList<Node>> mGraph = new SimpleArrayMap<>();

    private final ArrayList<Node> mSortResult = new ArrayList<>();
    private final HashSet<Node> mSortTmpMarked = new HashSet<>();

    private boolean topologyChanged;

    @Override
    @Nullable
    public Node getRoot() {
        if (mGraph.isEmpty()) {
            return null;
        }
        List<Node> sortedNodes = getAllNodesSorted();
        return sortedNodes.get(sortedNodes.size() - 1);
    }

    /**
     * Add a node to the graph.
     *
     * <p>If the node already exists in the graph then this method is a no-op.</p>
     *
     * @param node the node to add
     */
    @Override
    public void add(@NonNull Node node) {
        if (!mGraph.containsKey(node)) {
            mGraph.put(node, null);
            topologyChanged = true;
        }
    }

    /**
     * Returns true if the node is already present in the graph, false otherwise.
     */
    @Override
    public boolean contains(@NonNull Node node) {
        return mGraph.containsKey(node);
    }

    /**
     * Add an edge to the graph.
     *
     * <p>Both the given nodes should already have been added to the graph through
     * {@link #add(Node)}.</p>
     *
     * @param from the parent node
     * @param to the node which has is an incoming edge to {@code node}
     */
    @Override
    public void connect(@NonNull Node from, @NonNull Node to) {
        if (!mGraph.containsKey(from) || !mGraph.containsKey(to)) {
            throw new IllegalArgumentException("All nodes must be present in the graph before"
                    + " being added as an edge");
        }

        ArrayList<Node> edges = mGraph.get(from);
        if (edges == null) {
            // If edges is null, we should try and get one from the pool and add it to the graph
            edges = getEmptyList();
            mGraph.put(from, edges);
        }
        // Finally add the edge to the list
        edges.add(to);

        topologyChanged = true;
    }

    /**
     * Get any incoming edges from the given node.
     *
     * @return a list containing any incoming edges, or null if there are none.
     */
    @Nullable
    @Override
    public List<Node> getIncomingEdges(@NonNull Node node) {
        return mGraph.get(node);
    }

    /**
     * Get any outgoing edges for the given node (i.e. nodes which have an incoming edge
     * from the given node).
     *
     * @return a list containing any outgoing edges, or null if there are none.
     */
    @Nullable
    @Override
    public List<Node> getOutgoingEdges(@NonNull Node node) {
        ArrayList<Node> result = null;
        for (int i = 0, size = mGraph.size(); i < size; i++) {
            ArrayList<Node> edges = mGraph.valueAt(i);
            if (edges != null && edges.contains(node)) {
                if (result == null) {
                    result = new ArrayList<>();
                }
                result.add(mGraph.keyAt(i));
            }
        }
        return result;
    }

    public boolean hasOutgoingEdges(@NonNull Node node) {
        for (int i = 0, size = mGraph.size(); i < size; i++) {
            ArrayList<Node> edges = mGraph.valueAt(i);
            if (edges != null && edges.contains(node)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Clears the internal graph, and releases resources to pools.
     */
    @Override
    public void clear() {
        for (int i = 0, size = mGraph.size(); i < size; i++) {
            ArrayList<Node> edges = mGraph.valueAt(i);
            if (edges != null) {
                poolList(edges);
            }
        }
        mGraph.clear();
        topologyChanged = true;
    }

    /**
     * Returns a topologically sorted list of the nodes in this graph. This uses the DFS algorithm
     * as described by Cormen et al. (2001). If this graph contains cyclic dependencies then this
     * method will throw a {@link RuntimeException}.
     *
     * <p>The resulting list will be ordered such that index 0 will contain the node at the bottom
     * of the graph. The node at the end of the list will have no dependencies on other nodes.</p>
     */
    @NonNull
    public List<Node> getAllNodesSorted() {
        if (topologyChanged) {
            mSortResult.clear();
            mSortTmpMarked.clear();

            // Start a DFS from each node in the graph
            for (int i = 0, size = mGraph.size(); i < size; i++) {
                dfs(mGraph.keyAt(i), mSortResult, mSortTmpMarked);
            }

            topologyChanged = false;
        }
        return mSortResult;
    }

    private void dfs(final Node node, final ArrayList<Node> result, final HashSet<Node> tmpMarked) {
        if (result.contains(node)) {
            // We've already seen and added the node to the result list, skip...
            return;
        }
        if (tmpMarked.contains(node)) {
            throw new RuntimeException("This graph contains cyclic dependencies");
        }
        // Temporarily mark the node
        tmpMarked.add(node);
        // Recursively dfs all of the node's edges
        final ArrayList<Node> edges = mGraph.get(node);
        if (edges != null) {
            for (int i = 0, size = edges.size(); i < size; i++) {
                dfs(edges.get(i), result, tmpMarked);
            }
        }
        // Unmark the node from the temporary list
        tmpMarked.remove(node);
        // Finally add it to the result list
        result.add(node);
    }

    /**
     * Returns the size of the graph
     */
    public int size() {
        return mGraph.size();
    }

    @NonNull
    private ArrayList<Node> getEmptyList() {
        ArrayList<Node> list = mListPool.acquire();
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }

    private void poolList(@NonNull ArrayList<Node> list) {
        list.clear();
        mListPool.release(list);
    }
}