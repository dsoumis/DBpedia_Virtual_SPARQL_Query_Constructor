package com.dsoumis.dbpediavirtualsparqlqueryconstructor.dtos;

import java.io.Serializable;

public class ConnectionViewDto implements Serializable {
    private final int viewId;
    private final int color;
    private int paintViewId;

    public ConnectionViewDto(final int viewId, final int color, final int paintViewId) {
        this.viewId = viewId;
        this.color = color;
        this.paintViewId = paintViewId;
    }

    public int getViewId() {
        return viewId;
    }

    public int getColor() {
        return color;
    }

    public int getPaintViewId() {
        return paintViewId;
    }

    public void setPaintViewId(int paintViewId) {
        this.paintViewId = paintViewId;
    }
}
