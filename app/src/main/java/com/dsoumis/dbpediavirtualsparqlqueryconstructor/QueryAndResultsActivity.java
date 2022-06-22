package com.dsoumis.dbpediavirtualsparqlqueryconstructor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dsoumis.dbpediavirtualsparqlqueryconstructor.singletons.OkHttpClientSingleton;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class QueryAndResultsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.query_and_results_layout);
        final TextView queryTextView = findViewById(R.id.text_view_query_id);
        final TableLayout resultsTableLayout = findViewById(R.id.table_layout_id);

        final Intent intent = getIntent();
        String query = intent.getStringExtra("query");
        final List<String> variablesList = new ArrayList<>(Arrays.asList(intent.getStringExtra("variables").split(" ")))
                .stream().map(variable -> variable.substring(1)).collect(Collectors.toList()); //Remove the '?' from variables

        TableRow tableRow = new TableRow(this);
        tableRow.setBackgroundColor(Color.GREEN);
        for (final String variable : variablesList) {
            final TextView view = new TextView(QueryAndResultsActivity.this);
            view.setText(variable);
            view.setTextColor(Color.WHITE);
            view.setPadding(10, 10, 10, 10);
            view.setTextSize(14);
            view.setGravity(Gravity.CENTER_HORIZONTAL);
            view.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 4));
            tableRow.addView(view);
        }
        resultsTableLayout.addView(tableRow);

        queryTextView.setText(query);

        //This replacement is used to be interpreted correctly in http request
        query = query.replace("#", "%23");
        query = query.replace(",", "%2C");

        final OkHttpClient client = OkHttpClientSingleton.getClient();

        final Request request = new Request.Builder().url(query).addHeader("Accept", "application/sparql-results+json").build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("Error", "Message: " + Log.getStackTraceString(e));
                onResponseFailure();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) onResponseFailure();

                final String result = Objects.requireNonNull(response.body()).string();

                try {
                    JSONObject jsonResult = new JSONObject(result);
                    JSONArray results = jsonResult.getJSONObject("results").getJSONArray("bindings");
                    for (int i = 0; i < results.length(); ++i) {
                        JSONObject innerObject = results.getJSONObject(i);
                        final TableRow tableRow = new TableRow(QueryAndResultsActivity.this);
                        for (final String variable : variablesList) {
                            final TextView view = new TextView(QueryAndResultsActivity.this);
                            view.setText(innerObject.getJSONObject(variable).getString("value"));
                            view.setTextColor(Color.BLACK);
                            view.setPadding(10, 10, 10, 10);
                            view.setTextSize(14);
                            view.setGravity(Gravity.CENTER_HORIZONTAL);
                            view.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 4));
                            tableRow.addView(view);
                        }
                        runOnUiThread(() -> resultsTableLayout.addView(tableRow));
                    }
                } catch (JSONException e) {
                    onResponseFailure();
                }

//                final Map<String, List<String>> valuesByPropertyMap = createValuesByPropertyMapFromJsonResult(result);
//

            }
        });
    }

    private void onResponseFailure() {
        runOnUiThread(() -> {
            Toast.makeText(getApplicationContext(),
                    "Can not access dbpedia at this time. Please try later.", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
        });
    }
}
