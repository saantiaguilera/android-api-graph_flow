package com.u.dag;

import android.animation.Animator;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;

/**
 * This class is in charge of replacing a node descriptor for another
 *
 * For a future release this could be decoupled into 2 modules (so only view people dont have
 * the support-fragment always as dependency)
 *
 * Created by saguilera on 6/10/17.
 */
final class NodeSwitcher {

    private static final int TYPE_UNKNOWN = -1;
    private static final int TYPE_FRAGMENT = 0;
    private static final int TYPE_VIEW = 1;

    private @NonNull WeakReference<Activity> contextR;
    private @IdRes int resId;

    private @ClassType int type;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TYPE_UNKNOWN, TYPE_FRAGMENT, TYPE_VIEW})
    @interface ClassType {}

    public NodeSwitcher(@NonNull Activity context, @IdRes int resId) {
        this.contextR = new WeakReference<>(context);
        this.resId = resId;
        this.type = TYPE_UNKNOWN;
    }

    private void findTypeOrThrow(@NonNull Class<?> clazz) {
        int clazzType = View.class.isAssignableFrom(clazz) ? TYPE_VIEW : TYPE_FRAGMENT;
        if (type != TYPE_UNKNOWN && type != clazzType) {
            throw new IllegalStateException("Found nodes with different descriptor classes. " +
                "All should be subclasses of either Fragment or View.");
        }
        if (type == TYPE_UNKNOWN) {
            type = clazzType;
        }
    }

    private @Nullable ViewGroup getParent() {
        return contextR.get() == null ? null : (ViewGroup) contextR.get().findViewById(resId);
    }

    private @Nullable View createView(Class<?> clazz) {
        try {
            Context context = contextR.get();

            if (context == null) {
                throw new ActivityNotFoundException("Activity has been removed, so this will be removed too");
            }

            Constructor constructor = clazz.getConstructor(Context.class);
            return (View) constructor.newInstance(contextR.get());
        } catch (ActivityNotFoundException ex) {
            // Silent, we are not existing anymore
            return null;
        } catch (Exception e) {
            throw new RuntimeException(
                "View constructor for node class doesnt exist. Please provide <init>(Context) constructor.", e);
        }
    }

    private @Nullable Fragment createFragment(Class<?> clazz) {
        try {
            Context context = contextR.get();

            if (context == null) {
                throw new ActivityNotFoundException("Activity has been removed, so this will be removed too");
            }

            return Fragment.instantiate(context, clazz.getName());
        } catch (ActivityNotFoundException ex) {
            // Silent, we are not existing anymore
            return null;
        }
    }

    private void moveView(@NonNull Class<?> clazz, @Router.Movement int how) {
        final ViewGroup parent = getParent();
        View nodeView = createView(clazz);
        if (parent != null && nodeView != null) {
            final View before = parent.getChildCount() > 0 ? parent.getChildAt(0) : null;
            if (before == null) {
                // If theres no view in the parent, add one directly without just alpha anim
                parent.addView(nodeView);
                nodeView.setAlpha(0f);
                nodeView.animate()
                    .setDuration(400)
                    .alpha(1f)
                    .start();
            } else {
                before.animate()
                    .setDuration(400)
                    .translationX(how == Router.MOVE_BACKWARD ? parent.getWidth() : -parent.getWidth())
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
                nodeView.setX(how == Router.MOVE_BACKWARD ? -parent.getWidth() : parent.getWidth());
                nodeView.setAlpha(0f);
                nodeView.animate()
                    .setDuration(400)
                    .translationX(how == Router.MOVE_BACKWARD ? parent.getWidth() : -parent.getWidth())
                    .alpha(1f)
                    .start();
            }
        }
    }

    private void moveFragment(@NonNull Class<?> clazz, @Router.Movement int how) {
        Fragment fragment = createFragment(clazz);
        if (contextR.get() != null && fragment != null && contextR.get() instanceof FragmentActivity) {
            FragmentActivity context = (FragmentActivity) contextR.get();

            FragmentTransaction transaction = context.getSupportFragmentManager().beginTransaction();

            switch (how) {
                case Router.MOVE_BACKWARD:
                    transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
                    break;
                case Router.MOVE_FORWARD:
                    transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
                    break;
                // By default dont add anything
            }

            transaction.replace(resId, fragment);
            transaction.addToBackStack(fragment.getTag());
            transaction.commit();
        }

    }

    public void commit(@NonNull Class<?> clazz, @Router.Movement int how) {
        findTypeOrThrow(clazz);
        switch (type) {
            case TYPE_VIEW:
                moveView(clazz, how);
                break;
            case TYPE_FRAGMENT:
                moveFragment(clazz, how);
                break;
            default:
                throw new RuntimeException("Cant reach here");
        }
    }

}
