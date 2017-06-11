package com.u.core.node;

import android.os.Bundle;
import android.support.annotation.Nullable;

public interface NodeSelector {

    /**
     * Method for knowing if the node should be picked or not according to the information
     * inside the bundle
     * @param args bundle with arguments which the node should rely on for knowing if it has to be picked or not
     * @return true if the node should be picked, false otherwise
     */
    boolean select(@Nullable Bundle args);

}