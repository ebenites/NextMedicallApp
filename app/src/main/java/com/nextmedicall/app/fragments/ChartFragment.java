package com.nextmedicall.app.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.nextmedicall.app.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChartFragment extends Fragment {

    private static final String TAG = ChartFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle(getString(R.string.app_name) + " | " + getString(R.string.title_chart));
        View view = inflater.inflate(R.layout.fragment_chart, container, false);
/*
        BarChart mChart = view.findViewById(R.id.chart);
        mChart.getDescription().setEnabled(false);

        mChart.setPinchZoom(false);

        mChart.setDrawBarShadow(false);

        mChart.setDrawGridBackground(false);

        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(true);
        l.setYOffset(0f);
        l.setXOffset(10f);
        l.setYEntrySpace(0f);
        l.setTextSize(8f);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setValueFormatter(new LargeValueFormatter());
        leftAxis.setDrawGridLines(false);
        leftAxis.setSpaceTop(35f);
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        mChart.getAxisRight().setEnabled(false);


        float groupSpace = 0.08f;
        float barSpace = 0.03f; // x4 DataSet
        float barWidth = 0.2f; // x4 DataSet
        // (0.2 + 0.03) * 4 + 0.08 = 1.00 -> interval per "group"

        int groupCount = 4; // Solo los 4 ultimos anios
        int startYear = Calendar.getInstance().get(Calendar.YEAR) - groupCount + 1;
        int endYear = startYear + groupCount;

        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
        ArrayList<BarEntry> yVals2 = new ArrayList<BarEntry>();
        ArrayList<BarEntry> yVals3 = new ArrayList<BarEntry>();

        float randomMultiplier = new Random().nextFloat() * 100f;

        for (int i = startYear; i < endYear; i++) {
            Log.e(TAG, "i: " + i);
            yVals1.add(new BarEntry(i, (float) (Math.random() * randomMultiplier)));
            yVals2.add(new BarEntry(i, (float) (Math.random() * randomMultiplier)));
            yVals3.add(new BarEntry(i, (float) (Math.random() * randomMultiplier)));
        }

        BarDataSet set1, set2, set3;

        if (mChart.getData() != null && mChart.getData().getDataSetCount() > 0) {

            set1 = (BarDataSet) mChart.getData().getDataSetByIndex(0);
            set2 = (BarDataSet) mChart.getData().getDataSetByIndex(1);
            set3 = (BarDataSet) mChart.getData().getDataSetByIndex(2);
            set1.setValues(yVals1);
            set2.setValues(yVals2);
            set3.setValues(yVals3);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();

        } else {
            // create 4 DataSets
            set1 = new BarDataSet(yVals1, "Temp");
            set1.setColor(Color.rgb(104, 241, 175));
            set2 = new BarDataSet(yVals2, "Bmp");
            set2.setColor(Color.rgb(164, 228, 251));
            set3 = new BarDataSet(yVals3, "Sp");
            set3.setColor(Color.rgb(242, 247, 158));

            BarData data = new BarData(set1, set2, set3);
            data.setValueFormatter(new LargeValueFormatter());

            mChart.setData(data);
        }

        // specify the width each bar should have
        mChart.getBarData().setBarWidth(barWidth);

//        mChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        // restrict the x-axis range
        mChart.getXAxis().setAxisMinimum(startYear - .2f);

        // barData.getGroupWith(...) is a helper that calculates the width each group needs based on the provided parameters
        mChart.getXAxis().setAxisMaximum(startYear + mChart.getBarData().getGroupWidth(groupSpace, barSpace) * groupCount + .2f);
//        mChart.getXAxis().setLabelCount(groupCount + 2 , false);   // Total de data + 2
        mChart.groupBars(startYear, groupSpace, barSpace);
        mChart.invalidate();
*/

        drawLineChart((LineChart) view.findViewById(R.id.chart1), "Temp", Color.GREEN);
        drawLineChart((LineChart) view.findViewById(R.id.chart2), "Bpm", Color.CYAN);
        drawLineChart((LineChart) view.findViewById(R.id.chart3), "Spo2", Color.MAGENTA);

        return view;
    }

    private void drawLineChart(LineChart scoreChart, String name, int color) {

        int sizeData = 7;

        scoreChart.getDescription().setText("Días de la Semana");

        // if disabled, scaling can be done on x- and y-axis separately
        scoreChart.setPinchZoom(true);

        XAxis xAxis = scoreChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(12f);
        xAxis.setAxisMinimum(0f);
        xAxis.setAxisMaximum(sizeData + 1f);    // Total de data + 1
        xAxis.setLabelCount(sizeData+ 2, false);   // Total de data + 2
        xAxis.setGranularity(1f);   // Intervalo mínimo (sin decimales)
        if(sizeData > 12)
            xAxis.setLabelRotationAngle(-90f);

        final String[] labels = new String[sizeData+2];
        labels[0] = "";
        int ii = 1;
        for (int i = 0; i < sizeData; i++) {
                labels[ii++] = "Pra" + ii;
        }
        labels[ii++] = "";
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                if(value >= labels.length) return ""; // Fixed
                return ((int)value == value)?labels[(int)value]:"";
            }
        });

//        scoreSubtitle.setText(String.format(getString(R.string.course_dashboard_totalstudents), nestudiantes));

        YAxis leftAxis = scoreChart.getAxisLeft();
        leftAxis.setTextSize(12f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(36f);
        leftAxis.setLabelCount(11, false);
        leftAxis.setGranularity(1f);

//        LimitLine limitLine = new LimitLine(minScore.floatValue(), "Nota Mínima " + new DecimalFormat("###.#").format(minScore));
//        limitLine.setLineWidth(2f);
//        limitLine.enableDashedLine(10f, 10f, 0f);
//        limitLine.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
//        limitLine.setTextSize(10f);
//        leftAxis.addLimitLine(limitLine);
        // limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(true);

        YAxis rightAxis = scoreChart.getAxisRight();
        rightAxis.setEnabled(false);

        Legend legend = scoreChart.getLegend();
        legend.setTextSize(12f);
        //legend.setDrawInside(true);
        //legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        //legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        //legend.setXOffset(12f);
        //legend.setYOffset(12f);


        List<Entry> entries = new ArrayList<>();
        //entries.add(new Entry(3, 16));

        int iii = 1;
        for (int i = 0; i < sizeData; i++) {
            entries.add(new Entry(iii++, Math.round((new Random().nextFloat() * 20 + 10) * 100) / 100f  ));
        }

        LineDataSet dataSet = new LineDataSet(entries, name);
        dataSet.setLineWidth(2f);
        dataSet.setColor(color);
        dataSet.setCircleRadius(6f);
        dataSet.setCircleHoleRadius(4f);
        //dataSet.setDrawCircleHole(false);
        dataSet.setCircleColor(color);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(color);

        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(dataSet);

        LineData lineData = new LineData(dataSets);
        lineData.setValueTextSize(12f);
        lineData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                DecimalFormat formatter = new DecimalFormat("###,###,##0.0");
                return formatter.format(value);
            }
        });

        scoreChart.setData(lineData);

        //scoreChart.invalidate(); // refresh
        scoreChart.animateY(1000);

    }

}