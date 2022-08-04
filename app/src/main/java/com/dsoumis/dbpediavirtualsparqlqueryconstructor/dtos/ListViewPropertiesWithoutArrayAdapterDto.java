package com.dsoumis.dbpediavirtualsparqlqueryconstructor.dtos;

import java.io.Serializable;
import java.util.List;

public class ListViewPropertiesWithoutArrayAdapterDto implements Serializable {
    private final List<String> items;
    private final List<ConnectionViewDto> connectionChildrenViews;

    public ListViewPropertiesWithoutArrayAdapterDto(final List<String> items,
                                 final List<ConnectionViewDto> connectionChildrenViews) {
        this.items = items;
        this.connectionChildrenViews = connectionChildrenViews;
    }

    public List<String> getItems() {
        return items;
    }

    public List<ConnectionViewDto> getConnectionChildrenViews() {
        return connectionChildrenViews;
    }

}
