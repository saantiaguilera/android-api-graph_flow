package com.u.direction_acyclic_graph.views;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.u.core.Router;
import com.u.core.graph.DirectedAcyclicGraph;
import com.u.core.graph.Graph;
import com.u.core.node.Node;
import com.u.core.node.NodeSelector;
import com.u.core.node.NodeSwitcher;
import com.u.direction_acyclic_graph.R;
import com.u.views.NodeViewSwitcher;

/**
 * Created by saguilera on 6/11/17.
 */

public class ViewsActivity extends Activity implements Router.OnNodeCommitListener<View> {

    Router<View> router;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout viewGroup = new FrameLayout(this);
        viewGroup.setId(android.support.v7.appcompat.R.id.action0); // Whatever. Some id > 0

        setContentView(viewGroup);

        applyTo(viewGroup);
    }

    private void applyTo(@NonNull ViewGroup viewGroup) {
        NodeSwitcher<View> nodeSwitcher = new NodeViewSwitcher(this, viewGroup.getId());

        router = Router.<View>create()
            .with(buildGraph())
            .switcher(nodeSwitcher)
            .build();

        EditText editText = (EditText) router.fromRoot();
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {}

            @SuppressWarnings("CheckResult")
            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                if (s.length() != 2) {
                    return;
                }

                /**
                 * Here we will configure a bundle with the data we want to trigger the graph, and then call
                 * 'next'. Note that if we call next, we could again hook to the returned view, but for
                 * showing a bit of everything we will use for the others the global listener :)
                 */
                Bundle args = new Bundle();
                args.putString("age", s.toString());
                router.next(args); // We could here use also the result and hook again and again and like that.
            }

            @Override
            public void afterTextChanged(final Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        router.addOnNodeCommitListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        router.removeOnNodeCommitListener(this);
    }

    @Override
    public void onBackPressed() {
        // We handle back presses to move the graph for this case
        if (router.back() == null) {
            super.onBackPressed();
        }
    }

    /**
     * Of course this method is to showcase the lib. By all means, NEVER NEVER do something
     * like this.
     *
     * @param rendered object rendered from the node (view/fragment/etc)
     * @param nodeTag tag of the commited node (if applied, else null)
     */
    @Override
    public void onNodeCommited(@Nullable final View rendered, @Nullable final String nodeTag) {
        if (rendered == null) {
            throw new IllegalStateException("This should happen.");
        }

        if (nodeTag != null) {
            if (nodeTag.contentEquals("TAG_GROWNUP")) {
                ((ImageView) rendered).setImageResource(R.mipmap.ic_launcher);
            }

            if (nodeTag.contentEquals("TAG_END")) {
                TextView textView = (TextView) rendered;
                textView.setText("END");
                textView.setTextSize(50);
                textView.setGravity(Gravity.CENTER);
            }
        }

        // For the two of the middle, add a onclicklistener to move on
        if (rendered instanceof ImageView) {
            rendered.setOnClickListener(new View.OnClickListener() {
                @SuppressWarnings("CheckResult")
                @Override
                public void onClick(final View v) {
                    router.next(new Bundle());
                }
            });
        }
    }

    private @NonNull Graph buildGraph() {
        // Create the type of graph
        Graph graph = new DirectedAcyclicGraph();

        // Create a node with a custom view that sets its stuff
        Node start = new Node(CustomView1.class, new NodeSelector() {
            @Override
            public boolean select(@Nullable final Bundle args) {
                return true; // Always pick it, its the root.
            }
        });

        // Another one the same as the start but with a selector that uses arguments for deciding
        Node minor = new Node(CustomView2.class, new NodeSelector() {
            @Override
            public boolean select(@Nullable final Bundle args) {
                return Integer.valueOf(args.getString("age")) < 18;
            }
        });

        // This one is a normal image view but will be configured when selected from outside the view.
        Node grownUp = new Node(ImageView.class, new NodeSelector() {
            @Override
            public boolean select(@Nullable final Bundle args) {
                return Integer.valueOf(args.getString("age")) >= 18;
            }
        });
        grownUp.tag("TAG_GROWNUP");

        // Same as before
        Node end = new Node(TextView.class, new NodeSelector() {
            @Override
            public boolean select(@Nullable final Bundle args) {
                return true; // Both conclude here
            }
        });
        end.tag("TAG_END");

        // Add the nodes to the graph, the order doesnt mind, when connected the shape will be formed.
        graph.add(grownUp);
        graph.add(start);
        graph.add(minor);
        graph.add(end);

        // Connect them as we want. We will do it like this:
        // Start -> minor ----> end
        //   \____> grownUp /
        graph.connect(start, minor);
        graph.connect(start, grownUp);
        graph.connect(minor, end);
        graph.connect(grownUp, end);

        return graph;
    }

    @SuppressLint("AppCompatCustomView")
    public static class CustomView1 extends EditText {

        public CustomView1(final Context context) {
            super(context);
            setHint("Add your age with 2 digits...");
        }

    }

    @SuppressLint("AppCompatCustomView")
    public static class CustomView2 extends ImageView {

        public CustomView2(final Context context) {
            super(context);
            setImageResource(R.drawable.ic_kid);
        }

    }

}
