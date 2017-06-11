package com.u.views;

import android.animation.Animator;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import com.u.dag.Router;
import com.u.dag.node.NodeSwitcher;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by saguilera on 6/10/17.
 */
public class NodeViewSwitcher implements NodeSwitcher<View> {

    private @NonNull WeakReference<Activity> contextR;
    private @IdRes int resId;

    /**
     * Since only one of the views is visible at a time, we keep them in memory so the state isnt lost.
     * This shouldnt affect memory, unless you load your view with all the bussiness logic and such
     */
    private @NonNull Map<Long, View> views;

    public NodeViewSwitcher(@NonNull Activity context, @IdRes int resId) {
        this.contextR = new WeakReference<>(context);
        this.resId = resId;
        this.views = new HashMap<>();
    }

    private void findTypeOrThrow(@NonNull Class<?> clazz) {
        boolean isClassAView = View.class.isAssignableFrom(clazz);

        if (!isClassAView) {
            throw new IllegalStateException("Provided class: " + clazz.getName() + " isnt a subtype of: " +
                View.class.getName());
        }
    }

    private @Nullable ViewGroup getParent() {
        return contextR.get() == null ? null : (ViewGroup) contextR.get().findViewById(resId);
    }

    private @Nullable View createView(Class<?> clazz, long identifier) {
        if (views.get(identifier) != null) {
            // If the view was already created, return it!
            return views.get(identifier);
        }

        try {
            Context context = contextR.get();

            if (context == null) {
                throw new ActivityNotFoundException("Activity has been removed, so this will be removed too");
            }

            Constructor constructor = clazz.getConstructor(Context.class);
            View view = (View) constructor.newInstance(contextR.get());

            views.put(identifier, view);
            return view;
        } catch (ActivityNotFoundException ex) {
            // Silent, we are not existing anymore
            return null;
        } catch (Exception e) {
            throw new RuntimeException(
                "View constructor for node class doesnt exist. Please provide <init>(Context) constructor.", e);
        }
    }

    private @Nullable View moveView(@NonNull Class<?> clazz, @Router.Direction int how, long id) {
        final ViewGroup parent = getParent();
        View nodeView = createView(clazz, id);
        if (parent != null && nodeView != null) {
            final View before = parent.getChildCount() > 0 ? parent.getChildAt(0) : null;
            if (before == null) {
                // If theres no view in the parent, add one directly without just alpha anim
                parent.addView(nodeView);
                nodeView.setAlpha(0f);
                nodeView.animate()
                    .setDuration(400)
                    .alpha(1f)
                    .setListener(null)
                    .start();
            } else {
                if (before == nodeView) {
                    return nodeView; // They are the same...
                }

                before.animate()
                    .setDuration(400)
                    .xBy(how == Router.DIRECTION_BACKWARD ? parent.getWidth() : -parent.getWidth())
                    .alpha(0f)
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(final Animator animation) {}

                        @Override
                        public void onAnimationEnd(final Animator animation) {
                            parent.removeView(before);
                        }

                        @Override
                        public void onAnimationCancel(final Animator animation) {}

                        @Override
                        public void onAnimationRepeat(final Animator animation) {}
                    })
                    .start();

                parent.addView(nodeView);
                nodeView.setX(how == Router.DIRECTION_BACKWARD ? -parent.getWidth() : parent.getWidth());
                nodeView.setAlpha(0f);
                nodeView.animate()
                    .setDuration(400)
                    .xBy(how == Router.DIRECTION_BACKWARD ? parent.getWidth() : -parent.getWidth())
                    .alpha(1f)
                    .setListener(null)
                    .start();
            }
        }

        return nodeView;
    }

    public @Nullable View commit(@NonNull Class<?> clazz, @Router.Direction int how, long identifier) {
        findTypeOrThrow(clazz);
        return moveView(clazz, how, identifier);
    }

    @Override
    public void clearAll() {
        views.clear();
    }

}
