package com.dsoumis.dbpediavirtualsparqlqueryconstructor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.dsoumis.dbpediavirtualsparqlqueryconstructor.dtos.ConnectionViewDto;
import com.dsoumis.dbpediavirtualsparqlqueryconstructor.dtos.CustomParcelablePairDto;
import com.dsoumis.dbpediavirtualsparqlqueryconstructor.dtos.DbpediaLookupResultDto;
import com.dsoumis.dbpediavirtualsparqlqueryconstructor.dtos.ListViewPropertiesDto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ViewGroup mainLayout;

    private Integer lastViewClickedId;
    private Map<Integer, ListViewPropertiesDto> listViewPropertiesByListViewId;

    private static final int SEARCH_ACTIVITY_CODE = 0;
    private static final int RESOURCE_PROPERTIES_ACTIVITY_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.listViewPropertiesByListViewId = new HashMap<>();
        mainLayout = findViewById(R.id.main);
    }

    // Called when the user taps the "SEARCH DBPEDIA" button
    @SuppressWarnings("deprecation")
    public void searchDbpedia(View view) {// Do something in response to button

        final EditText editText = findViewById(R.id.search_dbpedia_query);

        final Intent intent = new Intent(MainActivity.this, SearchActivity.class); //Used to pass values from MainActivity(this) to SearchActivity
        intent.putExtra("queryText", editText.getText().toString()); //Extra is a pair with key area and value message
        startActivityForResult(intent, SEARCH_ACTIVITY_CODE);

    }


    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SEARCH_ACTIVITY_CODE) {

                final DbpediaLookupResultDto dbpediaLookupResultDto = data.getParcelableExtra("dbpediaLookupResultDto");
                createListViewWithDbpediaResult(dbpediaLookupResultDto);

            } else if (requestCode == RESOURCE_PROPERTIES_ACTIVITY_CODE) {

                final CustomParcelablePairDto customParcelablePairDto = data.getParcelableExtra("customParcelablePairDto");
                final String property = customParcelablePairDto.getFirstValue();
                final String value = customParcelablePairDto.getSecondValue();
                final int childListViewId = createListViewWithDbpediaResult(new DbpediaLookupResultDto(value.substring(value.lastIndexOf('/') + 1).trim(), value));

                final ArrayAdapter<String> listViewParentArrayAdapter = Objects.requireNonNull(listViewPropertiesByListViewId.get(lastViewClickedId))
                                                                                                                             .getArrayAdapter();
                listViewParentArrayAdapter.add(property.substring(property.lastIndexOf('/') + 1).trim());

                final int colorOfProperty = ListViewPropertiesDto.getColorOfProperty(listViewParentArrayAdapter.getCount() - 1);

                final ListView listViewParent = findViewById(lastViewClickedId);
                final ListView listViewChild = findViewById(childListViewId);
                final PaintView paintView = new PaintView(this, listViewParent, listViewChild, colorOfProperty);
                paintView.setId(View.generateViewId());
                this.mainLayout.addView(paintView);

                final ListViewPropertiesDto listViewPropertiesDtoOfParentListView = listViewPropertiesByListViewId.get(lastViewClickedId);
                if (listViewPropertiesDtoOfParentListView != null) {

                    listViewPropertiesDtoOfParentListView.getConnectionChildrenViews().add(
                            new ConnectionViewDto(childListViewId, colorOfProperty, paintView.getId()));
                }

                final ListViewPropertiesDto listViewPropertiesDtoOfChildListView = listViewPropertiesByListViewId.get(childListViewId);
                if (listViewPropertiesDtoOfChildListView != null) {
                    listViewPropertiesDtoOfChildListView.getConnectionParentViews().add(
                            new ConnectionViewDto(lastViewClickedId, colorOfProperty, paintView.getId()));
                }

            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @SuppressWarnings("deprecation")
    private int createListViewWithDbpediaResult(final DbpediaLookupResultDto dbpediaLookupResultDto){

        final ListView listView = new ListView(this){
            @Override
            public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                        MeasureSpec.AT_MOST);
                super.onMeasure(widthMeasureSpec, expandSpec);
            }
        };

        // Arraylist should be used to the adapter due to following issue:
        // https://stackoverflow.com/questions/3200551/unable-to-modify-arrayadapter-in-listview-unsupportedoperationexception
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.properties_layout,//android.R.layout.simple_list_item_1,
                new ArrayList<>(Collections.singleton(dbpediaLookupResultDto.getLabel()))) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final TextView textView = (TextView) super.getView(position, convertView, parent);

                textView.setTextColor(ListViewPropertiesDto.getColorOfProperty(position));

                return textView;
            }
        };

        listView.setAdapter(arrayAdapter);

        listView.setOnTouchListener(onTouchListener());
        listView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            final Intent intent = new Intent(MainActivity.this, ResourcePropertiesActivity.class);
            intent.putExtra("resource", dbpediaLookupResultDto.getUri().substring(dbpediaLookupResultDto.getUri().lastIndexOf('/') + 1).trim());
            lastViewClickedId = listView.getId();
            startActivityForResult(intent, RESOURCE_PROPERTIES_ACTIVITY_CODE);
            return false;
        });
        listView.setLongClickable(true);
        listView.setId(View.generateViewId());
        listViewPropertiesByListViewId.put(listView.getId(), new ListViewPropertiesDto(arrayAdapter, new ArrayList<>(), new ArrayList<>()));
        listView.setLayoutParams(new RelativeLayout.LayoutParams(200, 100));
        listView.setBackgroundColor(0xffffdbdb);
        this.mainLayout.addView(listView);

        return listView.getId();
    }

    private View.OnTouchListener onTouchListener () {
        return new View.OnTouchListener() {

            private int xDelta;
            private int yDelta;

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

                final ListView listViewSelected = findViewById(view.getId());
                final ListViewPropertiesDto listViewSelectedPropertiesDto = listViewPropertiesByListViewId.get(view.getId());
                if (listViewSelectedPropertiesDto != null) {

                    final List<ConnectionViewDto> childrenListViews = listViewSelectedPropertiesDto.getConnectionChildrenViews();

                    childrenListViews.forEach((connectionViewDto) -> {
                        mainLayout.removeView(findViewById(connectionViewDto.getPaintViewId()));
                        final PaintView paintView = new PaintView(MainActivity.this,
                                listViewSelected, findViewById(connectionViewDto.getViewId()), connectionViewDto.getColor());
                        paintView.setId(View.generateViewId());

                        //Change also the parent paint view of the child
                        Objects.requireNonNull(listViewPropertiesByListViewId.get(connectionViewDto.getViewId())).getConnectionParentViews()
                                .stream()
                                .filter(c -> c.getPaintViewId() == connectionViewDto.getPaintViewId()).findFirst()
                                .ifPresent(parent -> parent.setPaintViewId(paintView.getId()));

                        connectionViewDto.setPaintViewId(paintView.getId());
                        mainLayout.addView(paintView);
                    });

                    mainLayout.invalidate();

                    final List<ConnectionViewDto> parentListViews = listViewSelectedPropertiesDto.getConnectionParentViews();
                    parentListViews.forEach((connectionViewDto) -> {
                        mainLayout.removeView(findViewById(connectionViewDto.getPaintViewId()));
                        final PaintView paintView = new PaintView(MainActivity.this,
                                findViewById(connectionViewDto.getViewId()), listViewSelected, connectionViewDto.getColor());
                        paintView.setId(View.generateViewId());

                        //Change also the child paint view of the parent
                        Objects.requireNonNull(listViewPropertiesByListViewId.get(connectionViewDto.getViewId())).getConnectionChildrenViews()
                                .stream()
                                .filter(c -> c.getPaintViewId() == connectionViewDto.getPaintViewId()).findFirst()
                                .ifPresent(child -> child.setPaintViewId(paintView.getId()));

                        connectionViewDto.setPaintViewId(paintView.getId());
                        mainLayout.addView(paintView);
                    });

                }


                mainLayout.invalidate();
                return false;
            }
        };
    }
}