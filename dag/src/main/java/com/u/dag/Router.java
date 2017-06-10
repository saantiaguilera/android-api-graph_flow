package com.u.dag;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.u.dag.graph.Graph;
import com.u.dag.node.Node;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Stack;

/**
 * Entry point.
 * Created by saguilera on 6/10/17.
 */
public final class Router {

    public static final int MOVE_NONE = 0;
    public static final int MOVE_FORWARD = 1;
    public static final int MOVE_BACKWARD = 2;

    private @NonNull Graph graph;
    private @NonNull Stack<Node> decisions;
    private Node current;

    private @NonNull NodeSwitcher nodeSwitcher;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MOVE_NONE, MOVE_FORWARD, MOVE_BACKWARD})
    public @interface Movement {}

    Router(@NonNull Activity context,
            @IdRes int resId,
            @NonNull Graph graph) {
        this.graph = graph;
        this.decisions = new Stack<>();
        this.nodeSwitcher = new NodeSwitcher(context, resId);

        Node root = graph.getRoot();
        if (root == null) {
            throw new IllegalStateException("Router with empty graph is meaningless, please provide a non empty graph");
        }

        // Add the root and feed it
        decisions.push(current);
        feed(current, MOVE_NONE);
    }

    private void feed(@Nullable Node node, @Movement int movement) {
        if (node == null) {
            return;
        }

        this.current = node;

        nodeSwitcher.commit(node.getDescriptor(), movement);
    }

    public void next(@NonNull Bundle bundle) {
        List<Node> outgoingEdges = graph.getOutgoingEdges(current);
        if (outgoingEdges == null || outgoingEdges.isEmpty()) {
            //we are at the end
            return;
        }

        //move forward
        for (Node edge : outgoingEdges) {
            if (edge.select(bundle)) {
                decisions.push(edge);
                feed(edge, MOVE_FORWARD);
                break;
            }
        }
    }

    public boolean back() {
        List<Node> incomingEdges = graph.getIncomingEdges(current);
        if (incomingEdges == null || incomingEdges.isEmpty()) {
            //we are at the beginning
            return false;
        }

        // Use the decisions stack that have been done til now. If its empty means we are
        // either at the root or we have done a jump.
        if (!decisions.empty()) {
            feed(decisions.pop(), MOVE_BACKWARD);
            return true;
        }

        // If there are no decisions performed, use the incoming edges
        if (incomingEdges.size() == 1) {
            // Its only one choice so its this..
            feed(incomingEdges.get(0), MOVE_BACKWARD);
            return true;
        } else {
            for (Node edge : incomingEdges) {
                if (edge.select(null)) {
                    feed(edge, MOVE_BACKWARD);
                    return true;
                }
            }
        }

        return false;
    }

    public void jump(@NonNull Node node) {
        jump(node, MOVE_NONE);
    }

    public void jump(@NonNull Node node, @Movement int movement) {
        if (graph.contains(node)) {
            decisions.clear();
            decisions.push(node);
            feed(node, movement);
        } else {
            throw new IllegalStateException("Node doesnt exist in the graph. Maybe you have mistakenly jumped?");
        }
    }

    public void jump(@NonNull String tag) {
        jump(tag, MOVE_NONE);
    }

    public void jump(@NonNull String tag, @Movement int movement) {
        List<Node> nodes = graph.getAllNodesSorted();
        if (nodes != null) {
            for (Node node : nodes) {
                if (node.getTag() != null && node.getTag().contentEquals(tag)) {
                    jump(node, movement);
                    break;
                }
            }
        }
    }

    public static @NonNull Router.Builder create(@NonNull Activity context) {
        return new Builder(context);
    }

    public static class Builder {

        private WeakReference<Activity> contextR;
        private Graph graph;

        /**
         * Constructor.
         *
         * @param context of the activity showing the graph
         */
        public Builder(@NonNull Activity context) {
            this.contextR = new WeakReference<>(context);
        }

        /**
         * Graph from which the router feeds
         * @param graph graph
         * @return builder instance
         */
        public @NonNull Builder with(@NonNull Graph graph) {
            this.graph = graph;
            return this;
        }

        public @NonNull Router into(@IdRes int resId) {
            return new Router(
                contextR.get(),
                resId,
                graph
            );
        }

    }

}
