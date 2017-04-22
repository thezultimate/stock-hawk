package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.ui.MainActivity;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class CollectionWidgetRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(Contract.Quote.URI,
                        Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                        null,
                        null,
                        Contract.Quote.COLUMN_SYMBOL);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                DecimalFormat percentageFormat =
                        (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
                percentageFormat.setMaximumFractionDigits(2);
                percentageFormat.setMinimumFractionDigits(2);
                percentageFormat.setPositivePrefix("+");

                DecimalFormat dollarFormat =
                        (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);

                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }

                String symbol = data.getString(Contract.Quote.POSITION_SYMBOL);
                float priceNumber = data.getFloat(Contract.Quote.POSITION_PRICE);
                String price = dollarFormat.format(priceNumber);
                float percentageChange = data.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);
                String percentage = percentageFormat.format(percentageChange / 100);

                RemoteViews views =
                        new RemoteViews(getPackageName(), R.layout.widget_collection_list_item);
                views.setTextViewText(R.id.widget_symbol, symbol);
                views.setContentDescription(R.id.widget_symbol, getString(R.string.a11y_symbol, symbol));
                views.setTextViewText(R.id.widget_price, price);
                views.setContentDescription(R.id.widget_price, getString(R.string.a11y_price, price));
                views.setTextViewText(R.id.widget_change, percentage);
                views.setContentDescription(R.id.widget_change, getString(R.string.a11y_change_percentage, percentage));
                if (percentageChange < 0) {
                    views.setInt(R.id.widget_change, "setBackgroundResource", R.drawable.percent_change_pill_red);
                } else {
                    views.setInt(R.id.widget_change, "setBackgroundResource", R.drawable.percent_change_pill_green);
                }

                Uri stockUri = Contract.Quote.makeUriForStock(symbol);
                final Intent fillInIntent = new Intent();
                fillInIntent.setData(stockUri);
                fillInIntent.putExtra(MainActivity.CHART_SYMBOL_KEY, symbol);
                views.setOnClickFillInIntent(R.id.widget_collection_item, fillInIntent);
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_collection_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(Contract.Quote.POSITION_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }

        };
    }

}
