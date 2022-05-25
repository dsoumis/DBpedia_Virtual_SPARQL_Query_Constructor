package com.dsoumis.dbpediavirtualsparqlqueryconstructor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;

@SuppressLint("ViewConstructor")
public class PaintView extends View {

    private final View view1;
    private final View view2;

    private final Paint linePaint;

    @SuppressLint("ResourceAsColor")
    public PaintView(final Context context, final View view1, final View view2, final int color) {
        super(context);

        this.view1 = view1;
        this.view2 = view2;

        linePaint = new Paint();
        linePaint.setColor(color);
        linePaint.setStrokeWidth(10f);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        final int lineStartX = ((RelativeLayout.LayoutParams) view1.getLayoutParams()).leftMargin + view1.getMeasuredWidth();
        final int lineStartY = ((RelativeLayout.LayoutParams) view1.getLayoutParams()).topMargin;
        final int lineEndX = ((RelativeLayout.LayoutParams) view2.getLayoutParams()).leftMargin;
        final int lineEndY = ((RelativeLayout.LayoutParams) view2.getLayoutParams()).topMargin;
        canvas.drawLine(lineStartX, lineStartY, lineEndX, lineEndY, linePaint);
        drawArrow(canvas, linePaint, lineStartX, lineEndX, lineStartY, lineEndY);
    }

    private void drawArrow(Canvas canvas, Paint paint, float x, float x1, float y, float y1) {
        double degree = calculateDegree(x, x1, y, y1);
        float endX1 = (float) (x1 + ((35) * Math.cos(Math.toRadians((degree-30)+90))));
        float endY1 = (float) (y1 + ((35) * Math.sin(Math.toRadians(((degree-30)+90)))));

        float endX2 = (float) (x1 + ((35) * Math.cos(Math.toRadians((degree-60)+180))));
        float endY2 = (float) (y1 + ((35) * Math.sin(Math.toRadians(((degree-60)+180)))));

        canvas.drawLine(x1,y1,endX1,endY1,paint);
        canvas.drawLine(x1, y1, endX2,endY2,paint);
    }

    public double calculateDegree(float x1, float x2, float y1, float y2) {
        float startRadians = (float) Math.atan((y2 - y1) / (x2 - x1));
        startRadians += ((x2 >= x1) ? 90 : -90) * Math.PI / 180;
        return Math.toDegrees(startRadians);
    }

    public int getPaintViewColor() {
        return linePaint.getColor();
    }
}

