package com.sam_chordas.android.stockhawk.ui;

import android.app.Fragment;
import android.database.Cursor;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.db.chart.model.LineSet;
import com.db.chart.model.Point;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.text.DecimalFormat;


/**
 * Created by Akshata on 5/9/2016.
 */
public class StockGraphFragment extends Fragment {

    final String LOG_TAG = this.getClass().getSimpleName();

    public StockGraphFragment(){

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        if( getArguments() != null ) {
            makeGraph();
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setHasOptionsMenu(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_line_graph, container, false);
    }


    private void makeGraph(){

        String symbol = (String) getArguments().get(QuoteColumns.SYMBOL);
        Cursor finalCursor = getActivity().getContentResolver().query( QuoteProvider.Quotes.withSymbol(symbol), null, null, null, null);

        if(finalCursor.getCount()  < 3){
            Log.v(LOG_TAG, "Not enough data");
            getView().findViewById(R.id.no_data_for_graph).setVisibility(View.VISIBLE);
        }
        else{
            getView().findViewById(R.id.no_data_for_graph).setVisibility(View.GONE);
            finalCursor.moveToFirst();

            LineChartView lineChart = (LineChartView) getView().findViewById(R.id.linechart);

            lineChart.setXAxis(false);
            lineChart.setYAxis(false);

            lineChart.setGrid(ChartView.GridType.FULL, new Paint());
            lineChart.setLabelsFormat(new DecimalFormat(".##"));

            LineSet dataset = new LineSet();

            dataset.setThickness(getResources().getDimension(R.dimen.line_thickness));
            //dataset.setColor(2);

            lineChart.addData(dataset);
            lineChart.show();

            float min_price = -5.0f + 100.f*Float.parseFloat(finalCursor.getString(finalCursor.getColumnIndex(QuoteColumns.BIDPRICE)));
            float max_price = min_price + 5.0f;

            while(!finalCursor.isLast()){

                //String date = finalCursor.getString(finalCursor.getColumnIndex(QuoteColumns.CREATED));
                Float bidprice = Float.parseFloat(finalCursor.getString(finalCursor.getColumnIndex(QuoteColumns.BIDPRICE)));
                Float scaledBidPrice = 100.0f*bidprice;

                if(!bidprice.equals("null")){
                    dataset.addPoint(new Point("", scaledBidPrice) );
                    if(scaledBidPrice > max_price)
                        max_price = scaledBidPrice+5.0f;
                    else if(scaledBidPrice < min_price)
                        min_price = scaledBidPrice-5.0f;

                    lineChart.notifyDataUpdate();

                }


                finalCursor.moveToNext();

                int divisions = 10;
                int divisor = Math.round((max_price - min_price) / divisions);
                lineChart.setAxisBorderValues(Math.round(min_price)-1, Math.round(min_price) + divisor*divisions - 1, divisor );
            }
        }
    }

}
