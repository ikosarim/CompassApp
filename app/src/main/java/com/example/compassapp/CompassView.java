package com.example.compassapp;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.Nullable;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static android.graphics.Paint.Style.FILL_AND_STROKE;
import static android.view.accessibility.AccessibilityEvent.MAX_TEXT_LENGTH;
import static android.view.accessibility.AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED;

public class CompassView extends View {

    private float bearing;

    private Paint markerPaint;
    private Paint textPaint;
    private Paint circlePaint;
    private String northString;
    private String eastString;
    private String southString;
    private String westString;
    private int textHeight;

    public CompassView(Context context) {
        super(context);
        initCompassView();
    }

    public CompassView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initCompassView();
    }

    public CompassView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initCompassView();
    }

    private void initCompassView() {
        setFocusable(true);

        Resources r = getResources();
        circlePaint = new Paint(ANTI_ALIAS_FLAG);
        circlePaint.setColor(r.getColor(R.color.background_color));
        circlePaint.setStrokeWidth(1);
        circlePaint.setStyle(FILL_AND_STROKE);
        northString = r.getString(R.string.cardinal_north);
        eastString = r.getString(R.string.cardinal_east);
        southString = r.getString(R.string.cardinal_south);
        westString = r.getString(R.string.cardinal_west);

        textPaint = new Paint(ANTI_ALIAS_FLAG);
        textPaint.setColor(r.getColor(R.color.text_colour));

        textHeight = (int) textPaint.measureText("yY");

        markerPaint = new Paint(ANTI_ALIAS_FLAG);
        markerPaint.setColor(r.getColor(R.color.marker_color));
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        Компасс представляет собой окружность, занимающая все доступное пространство
//        Установить размеры элемента, вычислив короткую грань (высоту или ширину)
        int measuredWidth = measure(widthMeasureSpec);
        int measuredHeight = measure(heightMeasureSpec);

        int d = Math.min(measuredWidth, measuredHeight);

        setMeasuredDimension(d, d);
    }

    @Override
    public void onDraw(Canvas canvas) {
        int mMeasuredWidth = getMeasuredWidth();
        int mMeasuredHeight = getMeasuredHeight();

        int px = mMeasuredWidth / 2;
        int py = mMeasuredHeight / 2;

        int radius = Math.min(px, py);

//        нарисовать фон
        canvas.drawCircle(px, py, radius, circlePaint);

//        поворачивать ракурс таким оьразом, чтобы "верх" всегда укахывал на текущее направление
        canvas.save();
        canvas.rotate(-bearing, px, py);

        int textWidth = (int) textPaint.measureText("W");
        int cardinalX = px - textWidth / 2;
        int cardinalY = py - radius + textHeight;

//        рисовать отметки каждые 15 градусов и текст каждые 45 градусов
        for (int i = 0; i < 24; i++) {
//            нарисовать метку
            canvas.drawLine(px, py - radius, px, py - radius + 10, markerPaint);

            canvas.save();
            canvas.translate(0, textHeight);

//              нарисовать оснвные точки
            if (i % 6 == 0) {
                String dirString = "";
                switch (i) {
                    case 0:
                        dirString = northString;
                        int arrowY = 2 * textHeight;
                        canvas.drawLine(px, arrowY, px - 5, 3 * textHeight, markerPaint);
                        canvas.drawLine(px, arrowY, px + 5, 3 * textHeight, markerPaint);
                        break;
                    case 6:
                        dirString = eastString;
                        break;
                    case 12:
                        dirString = southString;
                        break;
                    case 18:
                        dirString = westString;
                        break;
                }
                canvas.drawText(dirString, cardinalX, cardinalY, textPaint);
            } else if (i % 3 == 0) {
//                отображать текст каждые 45 градусов
                String angle = String.valueOf(i * 15);
                float angleTextView = textPaint.measureText(angle);

                int angleTextX = (int) (px - angleTextView / 2);
                int angleTextY = py - radius + textHeight;
                canvas.drawText(angle, angleTextX, angleTextY, textPaint);
            }
            canvas.restore();
            canvas.rotate(15, px, py);
        }
        canvas.restore();
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(final AccessibilityEvent event) {
        super.dispatchPopulateAccessibilityEvent(event);

        if (isShown()) {
            String bearingString = String.valueOf(bearing);
            if (bearingString.length() > MAX_TEXT_LENGTH) {
                bearingString = bearingString.substring(0, MAX_TEXT_LENGTH);
            }

            event.getText().add(bearingString);
            return true;
        } else {
            return false;
        }
    }

    private int measure(int measureSpec) {
//        Декодтровать параметр measureSpec
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.UNSPECIFIED) {
            return 200;
        } else {
//            Т.к. нужно заполнить все доступное пространство, надо вернуть максимальный рахмер
            return specSize;
        }
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
        sendAccessibilityEvent(TYPE_VIEW_TEXT_CHANGED);
    }
}
