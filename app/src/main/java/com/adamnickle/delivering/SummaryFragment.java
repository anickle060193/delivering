package com.adamnickle.delivering;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.parse.FindCallback;
import com.parse.ParseException;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class SummaryFragment extends Fragment
{
    public static final String FRAGMENT_TAG = SummaryFragment.class.getName();

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance();
    private static final java.text.DateFormat SHORT_DATE_FORMAT = new SimpleDateFormat( DateFormat.getBestDateTimePattern( Locale.getDefault(), "Md" ), Locale.getDefault() );

    private View mMainView;
    private LineChart mTipsChart;
    private LineChart mTotalTipsChart;

    public static SummaryFragment newInstance()
    {
        return new SummaryFragment();
    }

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        if( mMainView == null )
        {
            mMainView = inflater.inflate( R.layout.fragment_summary, container, false );

            mTipsChart = (LineChart)mMainView.findViewById( R.id.tips_chart );
            formatLineCharts( mTipsChart );

            mTotalTipsChart = (LineChart)mMainView.findViewById( R.id.total_tips_chart );
            formatLineCharts( mTotalTipsChart );
        }
        else
        {
            Utilities.removeFromParent( mMainView );
        }
        loadData();
        return mMainView;
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    private void formatLineCharts( final LineChart chart )
    {
        chart.getLegend().setEnabled( false );
        chart.setDescription( null );
        chart.getXAxis().setPosition( XAxis.XAxisPosition.BOTTOM );
        chart.getAxisLeft().setEnabled( true );
        chart.getAxisRight().setEnabled( false );
        chart.getAxisLeft().setValueFormatter( new YAxisValueFormatter()
        {
            @Override
            public String getFormattedValue( float value, YAxis yAxis )
            {
                return NumberFormat.getCurrencyInstance().format( value );
            }
        } );
        chart.animateX( 1000, Easing.EasingOption.EaseInCubic );
        chart.animateY( 1000, Easing.EasingOption.EaseInCubic );
    }

    private void loadData()
    {
        mTipsChart.clear();
        Delivery.createQuery()
                .whereEqualTo( Delivery.DELIVERER, DeliveringUser.getCurrentUser() )
                .whereExists( Delivery.TIP )
                .whereExists( Delivery.DELIVERY_END )
                .addAscendingOrder( Delivery.DELIVERY_END )
                .addAscendingOrder( Delivery.DELIVERY_START )
                .addAscendingOrder( Delivery.CREATED_AT )
                .findInBackground( new FindCallback<Delivery>()
                {
                    @Override
                    public void done( List<Delivery> objects, ParseException ex )
                    {
                        if( ex != null )
                        {
                            Delivering.log( "Could not retrieve all Deliveries.", ex );
                            Delivering.oops();
                            return;
                        }

                        final ArrayList<Entry> tipEntries = new ArrayList<>();
                        final ArrayList<String> tipXLabels = new ArrayList<>();

                        final ArrayList<Entry> totalTipEntries = new ArrayList<>();
                        final ArrayList<String> totalTipXLabels = new ArrayList<>();

                        BigDecimal total = BigDecimal.ZERO;

                        final int deliveries = objects.size();
                        for( int i = 0; i < deliveries; i++ )
                        {
                            final Delivery delivery = objects.get( i );
                            final BigDecimal tip = delivery.getTip();
                            if( tip != null )
                            {
                                final Entry tipEntry = new Entry( tip.floatValue(), tipEntries.size(), delivery );
                                tipEntries.add( tipEntry );

                                total = total.add( tip );
                                final Entry totalTipEntry = new Entry( total.floatValue(), totalTipEntries.size(), delivery );
                                totalTipEntries.add( totalTipEntry );

                                final String dateLabel = SHORT_DATE_FORMAT.format( delivery.getDeliveryEnd() );
                                tipXLabels.add( dateLabel );
                                totalTipXLabels.add( dateLabel );
                            }
                        }

                        final ValueFormatter currencyValueFormatter = new ValueFormatter()
                        {
                            @Override
                            public String getFormattedValue( float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler )
                            {
                                return CURRENCY_FORMAT.format( value );
                            }
                        };

                        final LineDataSet tipSet = new LineDataSet( tipEntries, "Tips" );
                        tipSet.setAxisDependency( YAxis.AxisDependency.LEFT );
                        final int tipColor = ContextCompat.getColor( getActivity(), R.color.colorAccent );
                        tipSet.setColor( tipColor );
                        tipSet.setCircleColor( tipColor );
                        tipSet.setLineWidth( 1.5f );
                        tipSet.setValueFormatter( currencyValueFormatter );

                        final LineData tipData = new LineData( tipXLabels, Collections.singletonList( tipSet ) );
                        mTipsChart.setData( tipData );
                        mTipsChart.invalidate();


                        final LineDataSet totalTipSet = new LineDataSet( totalTipEntries, "Total Tips" );
                        totalTipSet.setAxisDependency( YAxis.AxisDependency.LEFT );
                        final int totalTipColor = Color.GREEN;
                        totalTipSet.setColor( totalTipColor );
                        totalTipSet.setCircleColor( totalTipColor );
                        totalTipSet.setLineWidth( 1.5f );
                        totalTipSet.setValueFormatter( currencyValueFormatter );

                        final LineData totalTipData = new LineData( totalTipXLabels, Collections.singletonList( totalTipSet ) );
                        mTotalTipsChart.setData( totalTipData );
                        mTotalTipsChart.invalidate();
                    }
                } );
    }
}
