package com.dsoumis.dbpediavirtualsparqlqueryconstructor.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dsoumis.dbpediavirtualsparqlqueryconstructor.R;
import com.dsoumis.dbpediavirtualsparqlqueryconstructor.dtos.ConnectionViewDto;
import com.dsoumis.dbpediavirtualsparqlqueryconstructor.dtos.ListViewPropertiesDto;
import com.dsoumis.dbpediavirtualsparqlqueryconstructor.dtos.ListViewPropertiesWithoutArrayAdapterDto;
import com.dsoumis.dbpediavirtualsparqlqueryconstructor.singletons.OkHttpClientSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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

    private Map<String, String> prefixesByUris;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fillPrefixesByUrisMap();

        setContentView(R.layout.query_and_results_layout);
        final TextView queryTextView = findViewById(R.id.text_view_query_id);
        final TableLayout resultsTableLayout = findViewById(R.id.table_layout_id);

        final Intent intent = getIntent();
        final HashMap<Integer, ListViewPropertiesWithoutArrayAdapterDto> listViewPropertiesByListViewId =
                (HashMap<Integer, ListViewPropertiesWithoutArrayAdapterDto>) intent.getSerializableExtra("listViewPropertiesByListViewId");
        final int firstListViewCreatedId = (int) intent.getSerializableExtra("firstListViewCreatedId");

        final StringBuilder variables = new StringBuilder();

        listViewPropertiesByListViewId.forEach((key, value) -> {
            final String firstItem = value.getItems().get(0);
            if (firstItem.contains("?var")) variables.append(firstItem).append(" ");
        });

        if (variables.length() == 0) {
            missingVariablesResponse();
            return;
        }

        String query = createQuery(listViewPropertiesByListViewId, firstListViewCreatedId, variables.toString());

        final List<String> variablesList = new ArrayList<>(Arrays.asList(variables.toString().split(" ")))
                .stream().map(variable -> variable.substring(1)).collect(Collectors.toList()); //Remove the '?' from variables

        resultsTableLayout.addView(createTableRowOfVariables(variablesList));

        queryTextView.setText(query);

        //This replacement is used to be interpreted correctly in http request
        query = query.replace("#", "%23").replace(",", "%2C");

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
                        final TableRow tableRow = createTableRowOfResults(variablesList, innerObject);
                        runOnUiThread(() -> resultsTableLayout.addView(tableRow));
                    }
                } catch (JSONException e) {
                    onResponseFailure();
                }

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

    private TableRow createTableRowOfVariables(final List<String> variablesList) {
        final TableRow tableRow = new TableRow(this);
        tableRow.setBackgroundColor(Color.GREEN);
        for (final String variable : variablesList) {
            tableRow.addView(createTextViewForTableRow(variable, Color.WHITE));
        }
        return tableRow;
    }

    private TableRow createTableRowOfResults(final List<String> variablesList, final JSONObject object) throws JSONException {
        final TableRow tableRow = new TableRow(QueryAndResultsActivity.this);
        for (final String variable : variablesList) {
            tableRow.addView(createTextViewForTableRow(object.getJSONObject(variable).getString("value"), Color.BLACK));
        }
        return tableRow;
    }

    private TextView createTextViewForTableRow(final String text, final int color) {
        final TextView view = new TextView(QueryAndResultsActivity.this);
        view.setText(text);
        view.setTextColor(color);
        view.setPadding(10, 10, 10, 10);
        view.setTextSize(14);
        view.setGravity(Gravity.CENTER_HORIZONTAL);
        view.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 4));
        return view;
    }

    private String createQuery(final Map<Integer, ListViewPropertiesWithoutArrayAdapterDto> listViewPropertiesByListViewId,
                               final int firstListViewCreatedId, final String variables) {

        final StringBuilder query = new StringBuilder();
        query.append("https://dbpedia.org/sparql?default-graph-uri=http://dbpedia.org&query=\n");
        query.append("PREFIX owl: <http://www.w3.org/2002/07/owl#>\n");
        query.append("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n");
        query.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n");
        query.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n");
        query.append("PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n");
        query.append("PREFIX dc: <http://purl.org/dc/elements/1.1/>\n");
        query.append("PREFIX : <http://dbpedia.org/resource/>\n");
        query.append("PREFIX dbpedia2: <http://dbpedia.org/property/>\n");
        query.append("PREFIX dbpedia: <http://dbpedia.org/>\n");
        query.append("PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n");
        query.append("SELECT DISTINCT ");

        query.append(variables);

        final StringBuilder conditions = new StringBuilder();
        conditions.append("\nWHERE { ");

        createConditionsOfQuery(listViewPropertiesByListViewId, firstListViewCreatedId, conditions);
        conditions.append("\n}");

        query.append(conditions);

        return query.toString();
    }

    private void missingVariablesResponse() {
        runOnUiThread(() -> {
            Toast.makeText(getApplicationContext(),
                    "Please define variables in the flowchart.", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private void createConditionsOfQuery(final Map<Integer, ListViewPropertiesWithoutArrayAdapterDto> listViewPropertiesByListViewId,
                                         final int viewId, final StringBuilder conditions) {
        final ListViewPropertiesWithoutArrayAdapterDto listViewPropertiesDto = listViewPropertiesByListViewId.get(viewId);

        if (listViewPropertiesDto != null) {

            final List<String> items = listViewPropertiesDto.getItems();
            final List<ConnectionViewDto> connectionChildrenViews = listViewPropertiesDto.getConnectionChildrenViews();
            final String firstItem;
            if (items.get(0).contains("?var")) {
                firstItem = items.get(0);
            } else {
                firstItem = createStringWithPrefix(items.get(0));
            }
            for (int itemIndex = 1; itemIndex < items.size(); ++itemIndex) {
                final int colorOfItem = ListViewPropertiesDto.getColorOfProperty(itemIndex);
                final ConnectionViewDto matchingChild = connectionChildrenViews.stream().filter(c -> c.getColor() == colorOfItem).findFirst().orElse(null);
                if (matchingChild != null) {
                    final String childFirstItem = createStringWithPrefix(Objects.requireNonNull(listViewPropertiesByListViewId.get(matchingChild.getViewId()))
                            .getItems().get(0));
                    final String predicate = createStringWithPrefix(items.get(itemIndex));

                    conditions.append("\n").append(firstItem).append(" ").append(predicate).append(" ").append(childFirstItem).append(" .");

                    createConditionsOfQuery(listViewPropertiesByListViewId, matchingChild.getViewId(), conditions);
                }
            }

        }
    }

    private String createStringWithPrefix(final String itemString) {
        final int lastIndex = itemString.lastIndexOf('/');
        if (lastIndex != -1) {
            final String prefixUri = itemString.substring(0, lastIndex + 1).trim();
            if (prefixesByUris.containsKey(prefixUri)) {
                return prefixesByUris.get(prefixUri) + itemString.substring(lastIndex + 1).trim();
            }
        }

        return itemString.contains("http") ? '<' + itemString + '>' : itemString;

    }

    private void fillPrefixesByUrisMap() {
        prefixesByUris = new HashMap<>();
        prefixesByUris.put("http://www.w3.org/2002/07/owl#", "owl:");
        prefixesByUris.put("http://www.w3.org/2001/XMLSchema#", "xsd:");
        prefixesByUris.put("http://www.w3.org/2000/01/rdf-schema#", "rdfs:");
        prefixesByUris.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:");
        prefixesByUris.put("http://xmlns.com/foaf/0.1/", "foaf");
        prefixesByUris.put("http://purl.org/dc/elements/1.1/", "dc");
        prefixesByUris.put("http://dbpedia.org/resource/", ":");
        prefixesByUris.put("http://dbpedia.org/property/", "dbpedia2:");
        prefixesByUris.put("http://dbpedia.org/", "dbpedia:");
        prefixesByUris.put("http://www.w3.org/2004/02/skos/core#", "skos:");
        prefixesByUris.put("http://dbpedia.org/ontology/", "dbo:");
    }
}
