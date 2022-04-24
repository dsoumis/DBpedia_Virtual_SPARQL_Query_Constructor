package com.dsoumis.dbpediavirtualsparqlqueryconstructor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dsoumis.dbpediavirtualsparqlqueryconstructor.adapters.ResourcePropertiesExpandableListAdapter;
import com.dsoumis.dbpediavirtualsparqlqueryconstructor.dtos.CustomParcelablePairDto;
import com.dsoumis.dbpediavirtualsparqlqueryconstructor.singletons.OkHttpClientSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ResourcePropertiesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.resource_properties_layout);

        final Intent intent = getIntent();
        final String resource = intent.getStringExtra("resource");

        final OkHttpClient client = OkHttpClientSingleton.getClient();

        final String urlQuery = "https://dbpedia.org/sparql?default-graph-uri=http://dbpedia.org&" +
                "query=select+distinct+?property+?value+?label+{+" +
                "dbr:" + resource + "+?property+?value+.+" +
                "?property+rdfs:label+?label+.+" +
                "+filter+langMatches(lang(?label),'en')+}";

        final Request request = new Request.Builder().url(urlQuery).addHeader("Accept", "application/sparql-results+json").build();
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
                final Map<String, List<String>> valuesByPropertyMap = createValuesByPropertyMapFromJsonResult(result);

                runOnUiThread(() -> createExpandableListView(valuesByPropertyMap));
            }
        });
    }

    private Map<String, List<String>> createValuesByPropertyMapFromJsonResult(final String result) {
        Log.d("Query_result", "Result: " + result);
        final Map<String, List<String>> valuesByPropertyMap = new TreeMap<>();
        try {
            final JSONObject jsonResult = new JSONObject(result);
            final JSONArray results = jsonResult.getJSONObject("results").getJSONArray("bindings");
            for (int index = 0; index < results.length(); ++index) {

                final JSONObject innerObject = results.getJSONObject(index);

                final String currentProperty = innerObject.getJSONObject("property").getString("value");
                final String currentValue = innerObject.getJSONObject("value").getString("value");

                if (!valuesByPropertyMap.containsKey(currentProperty)) {
                    valuesByPropertyMap.put(currentProperty, new ArrayList<>(Collections.singletonList(currentValue)));
                } else {
                    Objects.requireNonNull(valuesByPropertyMap.get(currentProperty)).add(currentValue);
                }

            }
        } catch (JSONException e) {
            Log.d("JSON ERROR", "Message of fault: " + Log.getStackTraceString(e));
            onResponseFailure();
        }

        return valuesByPropertyMap;
    }

    private void createExpandableListView(final Map<String, List<String>> valuesByPropertyMap) {
        final ExpandableListView expandableListView = findViewById(R.id.propertiesWithValuesExpandableListView);
        final ExpandableListAdapter expandableListAdapter = new ResourcePropertiesExpandableListAdapter(ResourcePropertiesActivity.this,
                valuesByPropertyMap);
        expandableListView.setAdapter(expandableListAdapter);
        expandableListView.setOnChildClickListener((expandableListView1, view, groupPosition, childPosition, id) -> {

            final String propertyClicked = (String) valuesByPropertyMap.keySet().toArray()[groupPosition];

            setResult(Activity.RESULT_OK, new Intent().putExtra("customParcelablePairDto",
                    new CustomParcelablePairDto(propertyClicked,
                            Objects.requireNonNull(Objects.requireNonNull(valuesByPropertyMap.get(propertyClicked)).get(childPosition)))));
            finish();

            return false;
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
