package com.adamnickle.delivering;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class SummaryFragment extends Fragment
{
    public static final String FRAGMENT_TAG = SummaryFragment.class.getName();

    private View mMainView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mList;
    private SummaryItemArrayAdapter mAdapter;

    private List<Delivery> mDeliveries;
    private List<Shift> mShifts;

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

            mSwipeRefreshLayout = (SwipeRefreshLayout)mMainView.findViewById( R.id.summary_fragment_swipe_refresh_layout );
            mList = (RecyclerView)mMainView.findViewById( R.id.summary_fragment_list );
            mAdapter = new SummaryItemArrayAdapter();
            mList.setAdapter( mAdapter );

            mSwipeRefreshLayout.setColorSchemeResources( R.color.colorAccent );
            mSwipeRefreshLayout.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener()
            {
                @Override
                public void onRefresh()
                {
                    getData();
                }
            } );
        }
        else
        {
            Utilities.removeFromParent( mMainView );
        }

        getData();

        return mMainView;
    }

    private void getData()
    {
        mSwipeRefreshLayout.setRefreshing( true );
        new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected Void doInBackground( Void... params )
            {
                queryData();
                return null;
            }

            @Override
            protected void onPostExecute( Void aVoid )
            {
                createSummaryItems();
            }
        }.execute();
    }

    private void queryData()
    {
        final CountDownLatch latch = new CountDownLatch( 2 );
        final Deliverer current = Deliverer.getCurrentUser();

        Delivery.createQuery()
                .whereEqualTo( Delivery.DELIVERER, current )
                .findInBackground( new FindCallback<Delivery>()
                {
                    @Override
                    public void done( List<Delivery> objects, ParseException e )
                    {
                        if( e == null )
                        {
                            mDeliveries = objects;
                        }
                        else
                        {
                            Delivering.log( "Could not find all Deliveries", e );
                        }
                        latch.countDown();
                    }
                } );
        Shift.createQuery()
                .whereEqualTo( Shift.DELIVERER, current )
                .findInBackground( new FindCallback<Shift>()
                {
                    @Override
                    public void done( List<Shift> objects, ParseException e )
                    {
                        if( e == null )
                        {
                            mShifts = objects;
                        }
                        else
                        {
                            Delivering.log( "Could not find all Shifts", e );
                        }
                        latch.countDown();
                    }
                } );
        try
        {
            latch.await();
        }
        catch( InterruptedException ex )
        {
            Delivering.log( "Interrupted", ex );
        }
    }

    private void createSummaryItems()
    {
        final List<SummaryItem> items = new ArrayList<>();

        int deliveryCount = 0;
        int tipCount = 0;
        BigDecimal totalTip = BigDecimal.ZERO;
        int totalsCount = 0;
        BigDecimal totalTotal = BigDecimal.ZERO;
        double milesDriven = 0.0;
        float hoursSpentDelivering = 0.0f;

        if( mDeliveries != null )
        {
            deliveryCount = mDeliveries.size();
            for( Delivery delivery : mDeliveries )
            {
                final BigDecimal tip = delivery.getTip();
                if( tip != null )
                {
                    tipCount++;
                    totalTip = totalTip.add( tip );
                }

                final BigDecimal total = delivery.getTotal();
                if( total != null )
                {
                    totalsCount++;
                    totalTotal = totalTotal.add( total );
                }

                if( delivery.hasStartMileage() && delivery.hasEndMileage() )
                {
                    milesDriven += delivery.getEndMileage() - delivery.getStartMileage();
                }

                if( delivery.isCompleted() )
                {
                    final long start = delivery.getDeliveryStart().getTime();
                    final long end = delivery.getDeliveryEnd().getTime();
                    hoursSpentDelivering += TimeUnit.HOURS.convert( end - start, TimeUnit.MILLISECONDS );
                }
            }
        }

        int shiftCount = 0;
        float hoursWorked = 0.0f;

        if( mShifts != null )
        {
            shiftCount = mShifts.size();
            for( Shift shift : mShifts )
            {
                if( shift.isCompleted() )
                {
                    final long start = shift.getStart().getTime();
                    final long end = shift.getEnd().getTime();
                    hoursWorked += TimeUnit.HOURS.convert( end - start, TimeUnit.MILLISECONDS );
                }
            }
        }

        items.add( new SummaryItem( "Total Deliveries", null, deliveryCount ) );
        items.add( new SummaryItem( "Tip Count", null, tipCount ) );
        items.add( new SummaryItem( "Total Tip Amount", null, Formatter.currency( totalTip ) ) );
        items.add( new SummaryItem( "Total Payment Count", null, totalsCount ) );
        items.add( new SummaryItem( "Total Paid Amount", null, Formatter.currency( totalTotal ) ) );
        items.add( new SummaryItem( "Miles Driven", null, Formatter.mileage( milesDriven ) ) );
        items.add( new SummaryItem( "Hours Spent Delivering", null, hoursSpentDelivering ) );
        items.add( new SummaryItem( "Total Shifts", null, shiftCount ) );
        items.add( new SummaryItem( "Hours Worked", null, hoursWorked ) );

        mAdapter.clear();
        mAdapter.addAll( items );
        mSwipeRefreshLayout.setRefreshing( false );
    }

    private static class SummaryItem
    {
        public final String Title;
        public final String Subtitle;
        public final String Data;

        public SummaryItem( String title, String subtitle, Object data )
        {
            Title = title;
            Subtitle = subtitle;
            Data = String.valueOf( data );
        }
    }

    private class SummaryItemViewHolder extends RecyclerView.ViewHolder
    {
        public SummaryItem Item;
        public final TextView Title;
        public final TextView Subtitle;
        public final TextView Data;

        public SummaryItemViewHolder( View itemView )
        {
            super( itemView );

            Title = (TextView)itemView.findViewById( R.id.summary_item_title );
            Subtitle = (TextView)itemView.findViewById( R.id.summary_item_subtitle );
            Data = (TextView)itemView.findViewById( R.id.summary_item_data );
        }

        public void update()
        {
            Title.setText( Item.Title );
            Data.setText( Item.Data );

            Subtitle.setText( Item.Subtitle );
            Subtitle.setVisibility( Item.Subtitle == null ? View.GONE : View.VISIBLE );
        }
    }

    private class SummaryItemArrayAdapter extends ArrayRecyclerAdapter<SummaryItem, SummaryItemViewHolder>
    {
        @Override
        public SummaryItemViewHolder onCreateViewHolder( ViewGroup parent, int viewType )
        {
            final View view = LayoutInflater.from( parent.getContext() )
                    .inflate( R.layout.summary_item_layout, parent, false );
            return new SummaryItemViewHolder( view );
        }

        @Override
        public void onBindViewHolder( SummaryItemViewHolder holder, int position )
        {
            holder.Item = get( position );
            holder.update();
        }
    }
}
