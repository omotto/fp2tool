package com.example.fp2tool;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class ResultListActivity extends Activity {
    // Member fields
    private ArrayAdapter<String> resultArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get String input parameter
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.result_list);

        // Initialize the button to perform device discovery
        Button scanButton = (Button) findViewById(R.id.button_ok);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //v.setVisibility(View.GONE);
                finish();
            }
        });

        resultArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        // Find and set up the ListView for paired devices
        ListView resultListView = (ListView) findViewById(R.id.results_list);
        resultListView.setAdapter(resultArrayAdapter);

        String[] lines = message.split(System.getProperty("line.separator"));

        int position;
        for (String line : lines) {
            line.replaceAll("\\s+","");
            position = line.indexOf(":");
            resultArrayAdapter.add(line.substring(0, position)+"\r\n"+line.substring(position+1,line.length()));
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}