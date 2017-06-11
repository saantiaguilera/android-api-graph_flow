package com.u.dag;

import android.os.Bundle;
import android.support.annotation.CheckResult;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.u.dag.graph.Graph;
import com.u.dag.node.Node;
import com.u.dag.node.NodeSwitcher;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.Stack;

/**
 * Entry point.
 * Created by saguilera on 6/10/17.
 */
public final class Router<RenderObject> {

    public static final int DIRECTION_NONE = 0;
    public static final int DIRECTION_FORWARD = 1;
    public static final int DIRECTION_BACKWARD = 2;

    private @NonNull Graph graph;
    private @NonNull Stack<Node> decisions;
    private Node current;

    private @NonNull NodeSwitcher<RenderObject> nodeSwitcher;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ DIRECTION_NONE, DIRECTION_FORWARD, DIRECTION_BACKWARD })
    public @interface Direction {}

    /**
     * Package protected constructor that initializes stabilized
     * @param nodeSwitcher instance in charge of switching nodes
     * @param graph that provides the node connections
     */
    Router(@NonNull NodeSwitcher<RenderObject> nodeSwitcher,
            @NonNull Graph graph) {
        this.graph = graph;
        this.nodeSwitcher = nodeSwitcher;
        this.decisions = new Stack<>();

        Node root = graph.getRoot();
        if (root == null) {
            throw new IllegalStateException("Router with empty graph is meaningless, please provide a non empty graph");
        }

        // Add the root and commit it
        decisions.push(current);
        commit(current, DIRECTION_NONE);
    }

    /**
     * Commits a node in the graph to the supplied view
     * @param node to show
     * @param movement with the direcion of the flow (eg for animations)
     * @return the render object (view/fragment/etc) commited to the parent
     */
    @Nullable
    private RenderObject commit(@Nullable Node node, @Direction int movement) {
        if (node == null) {
            return null;
        }

        this.current = node;

        return nodeSwitcher.commit(node.getDescriptor(), movement, node.hashCode());
    }

    /**
     * Move in the graph to the next node according to the bundle provided.
     * Eg we have a graph:
     * A (age picker) -> B (age > 18)
     *               \_> C (age <= 18)
     * And we are at node A and the user inputs his age.
     * We create a bundle that contains { "age": 23 } and we call this method,
     * This will make B to be commited to the parent. (and not C because 23 > 18)
     *
     * For information on how to "pick" a node see {@link Node#select(Bundle)}
     *
     * @param bundle with information for knowing which node to pick from the outgoing edges
     * @return render object of the commited node
     */
    @Nullable
    @CheckResult
    public RenderObject next(@NonNull Bundle bundle) {
        List<Node> outgoingEdges = graph.getOutgoingEdges(current);
        if (outgoingEdges == null || outgoingEdges.isEmpty()) {
            //we are at the end
            return null;
        }

        //move forward
        for (Node edge : outgoingEdges) {
            if (edge.select(bundle)) {
                decisions.push(edge);
                return commit(edge, DIRECTION_FORWARD);
            }
        }

        return null;
    }

    /**
     * Move the flow backwards. Read the code to see how the flow is decided
     * <b>Note:</b> If a jump is performed, the backstack is cleared. So if you call this method after
     * a jump, it will check for the incoming edges and ask to each of them which to call with 'null' bundle.
     * If none is selectable, it wont do anything
     *
     * For checking if back has commited a node or not, check that the result is != null.
     *
     * @return render object of the commited node.
     */
    @Nullable
    @CheckResult
    public RenderObject back() {
        List<Node> incomingEdges = graph.getIncomingEdges(current);
        if (incomingEdges == null || incomingEdges.isEmpty()) {
            //we are at the beginning
            return null;
        }

        // Use the decisions stack that have been done til now. If its empty means we are
        // either at the root or we have done a jump.
        if (!decisions.empty()) {
            return commit(decisions.pop(), DIRECTION_BACKWARD);
        }

        // If there are no decisions performed, use the incoming edges
        if (incomingEdges.size() == 1) {
            // Its only one choice so its this..
            return commit(incomingEdges.get(0), DIRECTION_BACKWARD);
        } else {
            for (Node edge : incomingEdges) {
                if (edge.select(null)) {
                    return commit(edge, DIRECTION_BACKWARD);
                }
            }
        }

        return null;
    }

    /**
     * Jump to the node.
     * This will clear the previous backstack.
     * @param node to jump to
     * @return object rendered from the node
     */
    @Nullable
    @CheckResult
    public RenderObject jump(@NonNull Node node) {
        return jump(node, DIRECTION_NONE);
    }

    /**
     * Jump to the node.
     * This will clear the previous backstack.
     * @param node to jump to
     * @param movement with the direcion of the flow (eg for animations)
     * @return object rendered from the node
     */
    @Nullable
    @CheckResult
    public RenderObject jump(@NonNull Node node, @Direction int movement) {
        if (graph.contains(node)) {
            decisions.clear();
            decisions.push(node);
            return commit(node, movement);
        } else {
            throw new IllegalStateException("Node doesnt exist in the graph. Maybe you have mistakenly jumped?");
        }
    }

    /**
     * Jump to the node.
     * This will clear the previous backstack.
     * @param tag of the node to jump to
     * @return object rendered from the node
     */
    @Nullable
    @CheckResult
    public RenderObject jump(@NonNull String tag) {
        return jump(tag, DIRECTION_NONE);
    }

    /**
     * Jump to the node.
     * This will clear the previous backstack.
     * @param tag of the node to jump to
     * @param movement with the direcion of the flow (eg for animations)
     * @return object rendered from the node
     */
    @Nullable
    @CheckResult
    public RenderObject jump(@NonNull String tag, @Direction int movement) {
        List<Node> nodes = graph.getAllNodesSorted();

        if (nodes != null) {
            for (Node node : nodes) {
                if (node.getTag() != null && node.getTag().contentEquals(tag)) {
                    return jump(node, movement);
                }
            }
        }

        return null;
    }

    /**
     * Create a new builder of routers
     * @param <RenderObject> that will output the nodes
     * @return Builder of routers
     */
    public static @NonNull <RenderObject> Router.Builder<RenderObject> create() {
        return new Builder<RenderObject>();
    }

    /**
     * Static inner class for creating stable instances of routers
     * @param <RenderObject> that will output from nodes
     */
    public static class Builder<RenderObject> {

        private Graph graph;
        private NodeSwitcher<RenderObject> nodeSwitcher;

        /**
         * Constructor.
         */
        public Builder() {
            // Default constructor
        }

        /**
         * Graph from which the router feeds
         * @param graph graph
         * @return builder instance
         */
        public @NonNull Builder<RenderObject> with(@NonNull Graph graph) {
            this.graph = graph;
            return this;
        }

        /**
         * Node switcher for knowing how to commit a new node in the parent resource id
         * @param nodeSwitcher instance
         * @return builder instance
         */
        public @NonNull Builder<RenderObject> switcher(@NonNull NodeSwitcher<RenderObject> nodeSwitcher) {
            this.nodeSwitcher = nodeSwitcher;
            return this;
        }

        /**
         * Create a new router from the provided parameters
         * @return Router instance
         */
        public @NonNull Router<RenderObject> build() {
            if (
                graph == null ||
                nodeSwitcher == null
                ) {
                throw new IllegalStateException("Missing parameters for constructing a stable router");
            }

            return new Router<RenderObject>(
                nodeSwitcher,
                graph
            );
        }

    }

}
