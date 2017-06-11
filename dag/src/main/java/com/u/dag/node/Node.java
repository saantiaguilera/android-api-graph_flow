package com.u.dag.node;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;

public class Node {

    @NonNull
    private final Class<?> descriptor;

    @NonNull
    private final NodeSelector selector;

    @Nullable
    private String tag;

    public Node(@NonNull Class<?> descriptor, @NonNull NodeSelector selector) {
        this.descriptor = descriptor;
        this.selector = selector;
    }

    /**
     * Identifier if you wish to jump directly to this node
     * @param nodeTag tag reference. Unique
     */
    public void tag(@NonNull String nodeTag) {
        this.tag = nodeTag;
    }

    @NonNull
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public Class<?> getDescriptor() {
        return descriptor;
    }

    public @Nullable String getTag() {
        return tag;
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY)
    public boolean select(@Nullable Bundle flowParams) {
        return selector.select(flowParams);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Node)) {
            return false;
        }

        final Node node = (Node) o;

        if (!getDescriptor().equals(node.getDescriptor())) {
            return false;
        }
        if (!selector.equals(node.selector)) {
            return false;
        }
        return getTag() != null ? getTag().equals(node.getTag()) : node.getTag() == null;
    }

    @Override
    public int hashCode() {
        int result = getDescriptor().hashCode();
        result = 31 * result + selector.hashCode();
        result = 31 * result + (getTag() != null ? getTag().hashCode() : 0);
        return result;
    }
}