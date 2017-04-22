package com.udacity.stockhawk.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChartActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int CHART_HISTORY_LOADER_ID = 5;

    private String symbol;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.stock_chart)
    LineChart lineChart;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);
        ButterKnife.bind(this);

        Intent intentThatStartedThisActivity = getIntent();
        symbol = intentThatStartedThisActivity.getStringExtra(MainActivity.CHART_SYMBOL_KEY);

        getSupportLoaderManager().initLoader(CHART_HISTORY_LOADER_ID, null, this);
    }

    private void showChartHistory(Cursor data) {
        String chartHistoryRaw = data.getString(data.getColumnIndex(Contract.Quote.COLUMN_HISTORY));
        String[] chartHistorySplit = chartHistoryRaw.split("\n");
        List<Entry> entries = new ArrayList<>();
        final List<String> labels = new ArrayList<>();

        int xIndex = 0;
        for (int i = chartHistorySplit.length - 1; i >= 0; i--) {
            String[] chartDataSplit = chartHistorySplit[i].split(",");
            float timeMillis = Float.parseFloat(chartDataSplit[0].trim());
            String date = convertMillisToDate(timeMillis, "dd/MM/yyyy");
            labels.add(date);
            float price = Float.parseFloat(chartDataSplit[1].trim());
            entries.add(new Entry(xIndex, price));
            xIndex++;
        }

        IAxisValueFormatter formatter = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return labels.get((int) value);
            }
        };

        Description description = new Description();
        description.setText(symbol + " price in USD");
        description.setTextColor(Color.WHITE);
        lineChart.setDescription(description);

        lineChart.getLegend().setTextColor(Color.WHITE);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(formatter);
        xAxis.setTextColor(Color.WHITE);

        lineChart.getAxisLeft().setTextColor(Color.WHITE);
        lineChart.getAxisRight().setEnabled(false);

        LineDataSet dataSet = new LineDataSet(entries, symbol);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setColor(ColorTemplate.getHoloBlue());
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.invalidate();

        lineChart.setContentDescription(String.format(getString(R.string.chart_content_description), symbol));
    }

    private String convertMillisToDate(float millis, String dateFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis((long) millis);
        return formatter.format(calendar.getTime());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                Contract.Quote.makeUriForStock(symbol),
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null, null, Contract.Quote.COLUMN_HISTORY);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() > 0) {
            data.moveToFirst();
            showChartHistory(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

}
