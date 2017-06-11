package com.u.dag.node;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.u.dag.Router;

/**
 * Created by saguilera on 6/10/17.
 */
public interface NodeSwitcher<RenderObject> {

    /**
     * Commit the class into the parent.
     * The class should be an instance of RenderObject. You should validate that previously
     * (else it will throw a bad cast when returning the instance)
     *
     * @param clazz to render
     * @param how to commit it (animations?)
     * @param identifier of the transaction that is being done
     * @return rendered instance (view/fragment/etc)
     */
    @Nullable RenderObject commit(@NonNull Class<?> clazz, @Router.Direction int how, long identifier);

    /**
     * This method is called when there was a jump in the flow of the graph node. If you have a cache
     * of the currently rendered objects, its safe to remove them at this point.
     */
    void clearAll();

}
