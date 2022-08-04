package com.dsoumis.dbpediavirtualsparqlqueryconstructor.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dsoumis.dbpediavirtualsparqlqueryconstructor.R;
import com.dsoumis.dbpediavirtualsparqlqueryconstructor.dtos.DbpediaLookupResultDto;
import com.dsoumis.dbpediavirtualsparqlqueryconstructor.parsers.DbpediaLookupXmlParser;
import com.dsoumis.dbpediavirtualsparqlqueryconstructor.singletons.OkHttpClientSingleton;

import org.jetbrains.annotations.NotNull;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dbpedia_results_layout);

        // Get the Intent that started this activity and extract the string
        final Intent intent = getIntent();
        final String queryText = intent.getStringExtra("queryText");

        searchDbpedia(queryText);

    }

    private void searchDbpedia(final String text) {

        final OkHttpClient client = OkHttpClientSingleton.getClient();

        final String urlStr = "https://lookup.dbpedia.org/api/search?query=" + text;
        final Request request = new Request.Builder().url(urlStr).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d("Network_error","Message of fault: " + Log.getStackTraceString(e));
                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(),
                            "Can not access dbpedia at this time. Please try later.", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_CANCELED);
                    finish();
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                //response.body().string() can be consumed only once. Calling it twice will give a FATAL EXCEPTION: OkHttp Dispatcher
                final String dbpediaResultsString = Objects.requireNonNull(response.body()).string();
                Log.d("dbpediaResults: ", dbpediaResultsString);
                runOnUiThread(() -> createDbpediaResults(dbpediaResultsString));
            }
        });
    }

    private void createDbpediaResults(final String dbpediaResultsString) {
        try {
            final ListView dbpediaResults = findViewById(R.id.dbpedia_results);

            final DbpediaLookupXmlParser parser = new DbpediaLookupXmlParser();
            final Set<DbpediaLookupResultDto> dbpediaLookupResultDtos = parser.parse(new ByteArrayInputStream(dbpediaResultsString.getBytes()));

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(SearchActivity.this, android.R.layout.simple_list_item_1,
                    dbpediaLookupResultDtos.stream().map(DbpediaLookupResultDto::getLabel).collect(Collectors.toList()));

            dbpediaResults.setAdapter(arrayAdapter);

            //When user taps on a result, DbpediaLookupResultDto is returned to the previous activity
            dbpediaResults.setOnItemClickListener((adapterView, view, position, id) -> {

                final DbpediaLookupResultDto dbpediaLookupResultDto = dbpediaLookupResultDtos.stream()
                        .filter(r -> r.getLabel().equals(dbpediaResults.getAdapter().getItem(position)))
                        .findFirst().orElse(null);

                setResult(Activity.RESULT_OK,
                        new Intent().putExtra("dbpediaLookupResultDto", dbpediaLookupResultDto));
                finish();

            });
        } catch (XmlPullParserException | IOException e) {
            Log.d("Xml error", "Message of fault: " + Log.getStackTraceString(e));
        }
    }
}
