package com.dsoumis.dbpediavirtualsparqlqueryconstructor.dtos;

public class ConnectionViewDto {
    private int viewId;
    private int color;
    private int paintViewId;

    public ConnectionViewDto(final int viewId, final int color, final int paintViewId) {
        this.viewId = viewId;
        this.color = color;
        this.paintViewId = paintViewId;
    }

    public int getViewId() {
        return viewId;
    }

    public void setViewId(final int viewId) {
        this.viewId = viewId;
    }

    public int getColor() {
        return color;
    }

    public void setColor(final int color) {
        this.color = color;
    }

    public int getPaintViewId() {
        return paintViewId;
    }

    public void setPaintViewId(int paintViewId) {
        this.paintViewId = paintViewId;
    }
}
