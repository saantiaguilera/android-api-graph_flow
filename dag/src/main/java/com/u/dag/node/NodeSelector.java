package com.u.dag.node;

import android.os.Bundle;
import android.support.annotation.Nullable;

public interface NodeSelector {

    boolean select(@Nullable Bundle args);

}