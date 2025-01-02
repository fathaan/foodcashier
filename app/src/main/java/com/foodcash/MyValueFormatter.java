package com.foodcash;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import java.util.List;

public class MyValueFormatter extends ValueFormatter {
    private final List<String> labels;

    public MyValueFormatter(List<String> labels) {
        this.labels = labels;
    }

    @Override
    public String getBarLabel(BarEntry barEntry) {
        return labels.get((int) barEntry.getX());
    }
}