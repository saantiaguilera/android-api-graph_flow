package com.u.fragments;

import android.content.ActivityNotFoundException;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import com.u.core.Router;
import com.u.core.node.NodeSwitcher;
import java.lang.ref.WeakReference;

/**
 * Created by saguilera on 6/10/17.
 */
public class NodeFragmentSwitcher implements NodeSwitcher<Fragment> {

    private @NonNull WeakReference<FragmentActivity> contextR;
    private @IdRes int resId;

    public NodeFragmentSwitcher(@NonNull FragmentActivity context, @IdRes int resId) {
        this.contextR = new WeakReference<>(context);
        this.resId = resId;
    }

    private void findTypeOrThrow(@NonNull Class<?> clazz) {
        boolean isClassAFragment = Fragment.class.isAssignableFrom(clazz);

        if (!isClassAFragment) {
            throw new IllegalStateException("Provided class: " + clazz.getName() + " isnt a subtype of: " +
                Fragment.class.getName());
        }
    }

    private @Nullable Fragment createFragment(Class<?> clazz, long identifier) {
        try {
            FragmentActivity context = contextR.get();
            Fragment fragment;

            if (context == null) {
                throw new ActivityNotFoundException("Activity has been removed, so this will be removed too");
            }

            fragment = context.getSupportFragmentManager().findFragmentByTag(String.valueOf(identifier));

            if (fragment == null) {
                return Fragment.instantiate(context, clazz.getName());
            } else {
                return fragment;
            }
        } catch (ActivityNotFoundException ex) {
            // Silent, we are not existing anymore
            return null;
        }
    }

    @Nullable
    @Override
    public Fragment commit(@NonNull final Class<?> clazz, @Router.Direction final int how, final long identifier) {
        findTypeOrThrow(clazz);

        Fragment fragment = createFragment(clazz, identifier);
        if (fragment != null) {
            FragmentActivity context = contextR.get();

            FragmentTransaction transaction = context.getSupportFragmentManager().beginTransaction();

            switch (how) {
                case Router.DIRECTION_FORWARD:
                    transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
                    break;
                case Router.DIRECTION_BACKWARD:
                    transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
                    break;
                // By default dont add anything
            }

            transaction.replace(resId, fragment, String.valueOf(identifier));
            transaction.commit();
        }

        return fragment;
    }

    @Override
    public void clearAll() {
        // Nothing to do..
    }

}
