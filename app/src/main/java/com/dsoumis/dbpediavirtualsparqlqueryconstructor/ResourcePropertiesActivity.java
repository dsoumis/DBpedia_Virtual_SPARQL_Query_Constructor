package com.dsoumis.dbpediavirtualsparqlqueryconstructor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dsoumis.dbpediavirtualsparqlqueryconstructor.singletons.OkHttpClientSingleton;

import java.io.IOException;
import java.util.Objects;

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
                Log.d("Query_result", "Result: " + result);

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
