package com.dsoumis.dbpediavirtualsparqlqueryconstructor.dtos;

import android.graphics.Color;
import android.widget.ArrayAdapter;

import java.util.List;

public class ListViewPropertiesDto {
    private final ArrayAdapter<String> arrayAdapter;
    private final List<String> items;
    private final List<ConnectionViewDto> connectionChildrenViews;
    private final List<ConnectionViewDto> connectionParentViews;

    public ListViewPropertiesDto(final ArrayAdapter<String> arrayAdapter,
                                 final List<String> items,
                                 final List<ConnectionViewDto> connectionChildrenViews,
                                 final List<ConnectionViewDto> connectionParentViews) {
        this.arrayAdapter = arrayAdapter;
        this.items = items;
        this.connectionChildrenViews = connectionChildrenViews;
        this.connectionParentViews = connectionParentViews;
    }

    public ArrayAdapter<String> getArrayAdapter() {
        return arrayAdapter;
    }

    public List<String> getItems() {
        return items;
    }

    public List<ConnectionViewDto> getConnectionChildrenViews() {
        return connectionChildrenViews;
    }

    public List<ConnectionViewDto> getConnectionParentViews() {
        return connectionParentViews;
    }

    public static int getColorOfProperty(final int position) {
        if (position == 0) return Color.BLACK;
        switch (position % 9) {
            case 1: return Color.BLUE;
            case 2: return Color.CYAN;
            case 3: return Color.DKGRAY;
            case 4: return Color.MAGENTA;
            case 5: return Color.GREEN;
            case 6: return Color.GRAY;
            case 7: return Color.LTGRAY;
            case 8: return Color.YELLOW;
            case 0: return Color.RED;
            default: return Color.BLACK;
        }
    }
}
