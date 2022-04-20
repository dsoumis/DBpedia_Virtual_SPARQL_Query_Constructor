package com.dsoumis.dbpediavirtualsparqlqueryconstructor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.dsoumis.dbpediavirtualsparqlqueryconstructor.dtos.DbpediaLookupResultDto;

import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private ViewGroup mainLayout;

    private int xDelta;
    private int yDelta;

//    private Map<>

    private static final int SEARCH_ACTIVITY_CODE = 0;
    private static final int RESOURCE_PROPERTIES_ACTIVITY_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Button searchButton = findViewById(R.id.search_button);
//        searchButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });

        mainLayout = findViewById(R.id.main);
//        image = findViewById(R.id.image);

//        RelativeLayout.LayoutParams lparams = new RelativeLayout.LayoutParams(
//                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//        lparams.addRule(RelativeLayout.CENTER_IN_PARENT);
//        Button button = new Button(this);
//        button.setLayoutParams(lparams);
//        button.setText("ADD VARIABLE");
//        this.mainLayout.addView(button);

        // returns True if the listener has
        // consumed the event, otherwise False.
        //image.setOnTouchListener(onTouchListener());
    }

    // Called when the user taps the "SEARCH DBPEDIA" button
    public void searchDbpedia(View view) {// Do something in response to button

        final EditText editText = findViewById(R.id.search_dbpedia_query);

        final Intent intent = new Intent(MainActivity.this, SearchActivity.class); //Used to pass values from MainActivity(this) to SearchActivity
        intent.putExtra("queryText", editText.getText().toString()); //Extra is a pair with key area and value message
        startActivityForResult(intent, SEARCH_ACTIVITY_CODE);

    }


    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SEARCH_ACTIVITY_CODE && resultCode == Activity.RESULT_OK) {
            final DbpediaLookupResultDto dbpediaLookupResultDto = data.getParcelableExtra("dbpediaLookupResultDto");
            createListViewWithDbpediaResult(dbpediaLookupResultDto);
        }
    }

    private void createListViewWithDbpediaResult(final DbpediaLookupResultDto dbpediaLookupResultDto){

        ListView listView = new ListView(this);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                Collections.singletonList(dbpediaLookupResultDto.getLabel()));

        listView.setAdapter(arrayAdapter);

        listView.setOnTouchListener(onTouchListener());
        listView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            final Intent intent = new Intent(MainActivity.this, ResourcePropertiesActivity.class);
            intent.putExtra("resource", dbpediaLookupResultDto.getUri().substring(dbpediaLookupResultDto.getUri().lastIndexOf('/') + 1).trim());
            startActivityForResult(intent, RESOURCE_PROPERTIES_ACTIVITY_CODE);
            return false;
        });
        listView.setLongClickable(true);
        listView.setId(View.generateViewId());
        this.mainLayout.addView(listView);
    }

    private View.OnTouchListener onTouchListener () {
        return new View.OnTouchListener() {

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                final int x = (int) event.getRawX();
                final int y = (int) event.getRawY();

                switch (event.getAction() & MotionEvent.ACTION_MASK) {

                    case MotionEvent.ACTION_DOWN:
                        RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams)
                                view.getLayoutParams();

                        xDelta = x - lParams.leftMargin;
                        yDelta = y - lParams.topMargin;
                        break;

                    case MotionEvent.ACTION_UP:
                        break;

                    case MotionEvent.ACTION_MOVE:
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view
                                .getLayoutParams();
                        layoutParams.leftMargin = x - xDelta;
                        layoutParams.topMargin = y - yDelta;
                        layoutParams.rightMargin = 0;
                        layoutParams.bottomMargin = 0;
                        view.setLayoutParams(layoutParams);
                        break;
                }
                mainLayout.invalidate();
                return false;
            }
        };
    }
}