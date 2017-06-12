package com.u.conductor;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ViewGroup;
import com.bluelinelabs.conductor.Conductor;
import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.u.core.node.NodeSwitcher;
import java.lang.reflect.Method;

/**
 * Created by saguilera on 6/11/17.
 */
public class NodeControllerSwitcher implements NodeSwitcher<Controller> {

    private @NonNull Router router;
    private boolean forceClean;

    public NodeControllerSwitcher(@NonNull Activity context,
            @NonNull ViewGroup container,
            @Nullable Bundle bundle) {
        this.router = Conductor.attachRouter(context, container, bundle);
        this.forceClean = true; // Since its the first time, force a clean.
    }

    private void findTypeOrThrow(@NonNull Class<?> clazz) {
        boolean isClassAFragment = Controller.class.isAssignableFrom(clazz);

        if (!isClassAFragment) {
            throw new IllegalStateException("Provided class: " + clazz.getName() + " isnt a subtype of: " +
                Controller.class.getName());
        }
    }

    private @NonNull Controller createController(Class<?> clazz) {
        try {
            Method createStatic = Controller.class.getDeclaredMethod("newInstance", Bundle.class);
            createStatic.setAccessible(true);

            Bundle bundle = new Bundle();
            // Save class name that is used inside
            bundle.putString("Controller.className", clazz.getName());

            return (Controller) createStatic.invoke(null, bundle);
        } catch (Exception e) {
            // Something was broken, this shouldnt happen
            throw new RuntimeException("Something broke, please check cause", e);
        }
    }

    private @NonNull Controller asRoot(final @NonNull Class<?> clazz, final long identifier) {
        Controller controller = createController(clazz);
        router.setRoot(
            RouterTransaction.with(controller)
                .tag(String.valueOf(identifier))
                .pushChangeHandler(new HorizontalChangeHandler())
                .popChangeHandler(new HorizontalChangeHandler())
        );
        return controller;
    }

    @Nullable
    @Override
    public Controller commit(@NonNull final Class<?> clazz, @com.u.core.Router.Direction final int how, final long identifier) {
        findTypeOrThrow(clazz);

        if (forceClean) {
            forceClean = false;
            return asRoot(clazz, identifier);
        }

        switch (how) {
            case com.u.core.Router.DIRECTION_FORWARD:
                Controller controller = createController(clazz);
                router.pushController(
                    RouterTransaction.with(controller)
                        .tag(String.valueOf(identifier))
                        .pushChangeHandler(new HorizontalChangeHandler())
                        .popChangeHandler(new HorizontalChangeHandler())
                );
                return controller;
            case com.u.core.Router.DIRECTION_BACKWARD:
                // If its backwards, first check the identifier isnt in the backstack (if it exists, just rollback to there)
                if (!router.popToTag(String.valueOf(identifier), new HorizontalChangeHandler())) {
                    // Wasnt in the backstack, so this is probably a jump with direction backwards or a back in a
                    // graph that is not cyclic, simply swap roots and is like a fresh start
                    return asRoot(clazz, identifier);
                } else {
                    return router.getControllerWithTag(String.valueOf(identifier));
                }
            default:
                return null;
        }
    }

    @Override
    public void clearAll() {
        forceClean = true;
    }

}
