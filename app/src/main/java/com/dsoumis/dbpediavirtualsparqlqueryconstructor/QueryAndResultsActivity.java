package com.dsoumis.dbpediavirtualsparqlqueryconstructor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dsoumis.dbpediavirtualsparqlqueryconstructor.singletons.OkHttpClientSingleton;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

        final Intent intent = getIntent();
        final String query = intent.getStringExtra("query");

        queryTextView.setText(query);

        final OkHttpClient client = OkHttpClientSingleton.getClient();

        final Request request = new Request.Builder().url(query).addHeader("Accept", "application/sparql-results+json").build();
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                Log.d("Error", "Message: " + Log.getStackTraceString(e));
//                onResponseFailure();
//            }
//
//            @Override
//            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                if (!response.isSuccessful()) onResponseFailure();
//
////                final String result = Objects.requireNonNull(response.body()).string();
////                final Map<String, List<String>> valuesByPropertyMap = createValuesByPropertyMapFromJsonResult(result);
////
////                runOnUiThread(() -> createExpandableListView(valuesByPropertyMap));
//            }
//        });
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
