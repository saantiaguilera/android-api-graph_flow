package com.u.dag.node;

import android.support.annotation.NonNull;
import com.u.dag.Router;

/**
 * Created by saguilera on 6/10/17.
 */

public interface NodeSwitcher<RenderObject> {

    @NonNull RenderObject commit(@NonNull Class<?> clazz, @Router.Movement int how);

}
