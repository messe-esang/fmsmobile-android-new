package net.e_sang.fmsmobile.ui;

import android.content.Context;
import android.widget.TextView;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import net.e_sang.fmsmobile.R;

import java.util.StringTokenizer;

import static net.e_sang.fmsmobile.kit.Kit.convertCurrencyStr;

public class MyMarkerView extends MarkerView {

    private TextView tvContent;
    private String sValue = "";

    public MyMarkerView(Context context, int layoutResource, String value) {
        super(context, layoutResource);

        tvContent = findViewById(R.id.tvContent);
        sValue = value;
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        try {
            StringTokenizer value = new StringTokenizer(sValue,".");
            tvContent.setText(value.nextToken()); // set the entry-value as the display text
        } catch (Exception s) {
            s.printStackTrace();
        }
    }

    @Override
    public int getXOffset(float xpos) {
        // this will center the marker-view horizontally
        return -(getWidth() / 2);
    }

    @Override
    public int getYOffset(float ypos) {
        // this will cause the marker-view to be above the selected value
        return -getHeight();
    }
}