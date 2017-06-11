package com.u.direction_acyclic_graph;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;
import com.u.direction_acyclic_graph.views.ViewsActivity;

/**
 * Created by saguilera on 6/11/17.
 */

public class MainActivity extends Activity {

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.activity_main_with_views)
            .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    startActivity(new Intent(MainActivity.this, ViewsActivity.class));
                }
            });

        findViewById(R.id.activity_main_with_fragments)
            .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    Toast.makeText(MainActivity.this, "TODO", Toast.LENGTH_SHORT).show();
                }
            });
    }
}
