package com.dsoumis.dbpediavirtualsparqlqueryconstructor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ViewGroup mainLayout;

    private Integer lastViewClickedId;
    private Integer firstListViewCreatedId;
    private Map<Integer, ListViewPropertiesDto> listViewPropertiesByListViewId;

    private static final int SEARCH_ACTIVITY_CODE = 0;
    private static final int RESOURCE_PROPERTIES_ACTIVITY_CODE = 1;

    private long lastClickTime;
    private int varCounter;
    private Map<String, String> prefixesByUris;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.listViewPropertiesByListViewId = new HashMap<>();
        mainLayout = findViewById(R.id.main);

        lastClickTime = System.currentTimeMillis();
        varCounter = 0;

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

    // Called when the user taps the "SEARCH DBPEDIA" button
    @SuppressWarnings("deprecation")
    public void searchDbpedia(View view) {// Do something in response to button

        final EditText editText = findViewById(R.id.search_dbpedia_query);

        final Intent intent = new Intent(MainActivity.this, SearchActivity.class); //Used to pass values from MainActivity(this) to SearchActivity
        intent.putExtra("queryText", editText.getText().toString()); //Extra is a pair with key area and value message
        startActivityForResult(intent, SEARCH_ACTIVITY_CODE);

    }

    public void createQuery(View view) {
        if (firstListViewCreatedId == null) return;

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

        listViewPropertiesByListViewId.forEach((key, value) -> {
            final String firstItem = value.getItems().get(0);
            if (firstItem.contains("?var")) query.append(firstItem).append(" ");
        });

        final StringBuilder conditions = new StringBuilder();
        conditions.append("\nWHERE { ");

        createConditionsOfQuery(firstListViewCreatedId, conditions);
        conditions.append("\n}");

        query.append(conditions);

        final Intent intent = new Intent(MainActivity.this, QueryAndResultsActivity.class); //Used to pass values from MainActivity(this) to SearchActivity
        intent.putExtra("query", query.toString());
        startActivity(intent);
    }

    private void createConditionsOfQuery(final int viewId, final StringBuilder conditions) {
        final ListViewPropertiesDto listViewPropertiesDto = listViewPropertiesByListViewId.get(viewId);

        if (listViewPropertiesDto != null) {

            final List<String> items = listViewPropertiesDto.getItems();
            final List<ConnectionViewDto> connectionChildrenViews = listViewPropertiesDto.getConnectionChildrenViews();
            final String firstItem;
            if (items.get(0).contains("?var")) {
                firstItem = items.get(0);
            }else {
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

                    createConditionsOfQuery(matchingChild.getViewId(), conditions);
                }
            }

        }
    }

    private String createStringWithPrefix(final String itemString){
        final int lastIndex = itemString.lastIndexOf('/');
        if (lastIndex != -1) {
            final String prefixUri = itemString.substring(0, lastIndex + 1).trim();
            if (prefixesByUris.containsKey(prefixUri)) {
                return prefixesByUris.get(prefixUri) + itemString.substring(lastIndex + 1).trim();
            }
        }

        return  itemString.contains("http") ? '<' + itemString + '>' : itemString;

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

                final ListViewPropertiesDto lastListViewPropertiesDto = listViewPropertiesByListViewId.get(lastViewClickedId);
                if (lastListViewPropertiesDto != null) {
                    final ArrayAdapter<String> listViewParentArrayAdapter = lastListViewPropertiesDto.getArrayAdapter();
                    listViewParentArrayAdapter.add(property.substring(property.lastIndexOf('/') + 1).trim());

                    lastListViewPropertiesDto.getItems().add(property);

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
        final List<String> listOfItemUrisOfListview = new ArrayList<>(Collections.singleton(dbpediaLookupResultDto.getUri()));
        final List<String> listOfListview = new ArrayList<>(Collections.singleton(dbpediaLookupResultDto.getLabel()));
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.properties_layout, listOfListview) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final TextView textView = (TextView) super.getView(position, convertView, parent);

                textView.setTextColor(ListViewPropertiesDto.getColorOfProperty(position));

                return textView;
            }
        };

        listView.setAdapter(arrayAdapter);

        listView.setOnTouchListener(onTouchListener());
        listView.setOnItemClickListener((adapterView, view, i, l) -> {
            long currTime = System.currentTimeMillis();
            if (currTime - lastClickTime < ViewConfiguration.getDoubleTapTimeout()) {
                final String var = "?var" + varCounter++;
                listOfItemUrisOfListview.set(0, var);
                listOfListview.set(0, var);
                arrayAdapter.notifyDataSetChanged();
                listView.setLongClickable(false);
            }
            lastClickTime = currTime;
        });

        listView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            final Intent intent = new Intent(MainActivity.this, ResourcePropertiesActivity.class);
            intent.putExtra("resource", dbpediaLookupResultDto.getUri().substring(dbpediaLookupResultDto.getUri().lastIndexOf('/') + 1).trim());
            lastViewClickedId = listView.getId();
            startActivityForResult(intent, RESOURCE_PROPERTIES_ACTIVITY_CODE);
            return false;
        });
        listView.setLongClickable(true);
        listView.setId(View.generateViewId());
        listViewPropertiesByListViewId.put(listView.getId(), new ListViewPropertiesDto(arrayAdapter, listOfItemUrisOfListview, new ArrayList<>(), new ArrayList<>()));
        listView.setLayoutParams(new RelativeLayout.LayoutParams(200, 100));
        listView.setBackgroundColor(0xffffdbdb);
        this.mainLayout.addView(listView);

        if (firstListViewCreatedId == null) firstListViewCreatedId = listView.getId();

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