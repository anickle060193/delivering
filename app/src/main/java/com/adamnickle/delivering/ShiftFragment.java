package com.adamnickle.delivering;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseQuery;

import java.util.Date;


@SuppressWarnings( "ValidFragment" )
public class ShiftFragment extends Fragment
{
    private View mMainView;
    private RecyclerView mDeliveriesList;
    private TextView mShiftDate;
    private TextView mShiftClockInTime;
    private TextView mShiftClockOutTime;

    public static ShiftFragment newInstance( Shift shift )
    {
        return new ShiftFragment( shift );
    }

    private final Shift mShift;

    public ShiftFragment( Shift shift )
    {
        mShift = shift;
    }

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setRetainInstance( true );
        setHasOptionsMenu( true );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        if( mMainView == null )
        {
            mMainView = inflater.inflate( R.layout.fragment_shift, container, false );
            mShiftDate = (TextView)mMainView.findViewById( R.id.shift_fragment_date );
            mShiftClockInTime = (TextView)mMainView.findViewById( R.id.shift_fragment_clock_in_time );
            mShiftClockOutTime = (TextView)mMainView.findViewById( R.id.shift_fragment_clock_out_time );
            mDeliveriesList = (RecyclerView)mMainView.findViewById( R.id.shift_fragment_deliveries_list );
            mDeliveriesList.setAdapter( new DeliveryAdapter( getActivity(), new ParseObjectArrayAdapter.ParseQueryFactory<Delivery>()
            {
                @Override
                public ParseQuery<Delivery> getQuery()
                {
                    return Delivery.createQuery()
                            .whereEqualTo( Delivery.DELIVERER, Deliverer.getCurrentUser() )
                            .whereEqualTo( Delivery.SHIFT, mShift )
                            .addDescendingOrder( Delivery.CREATED_AT );
                }
            } ) );
            update();
        }
        else
        {
            Utilities.removeFromParent( mMainView );
        }
        return mMainView;
    }

    @Override
    public void onCreateOptionsMenu( Menu menu, MenuInflater inflater )
    {
        inflater.inflate( R.menu.fragment_shift, menu );
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        switch( item.getItemId() )
        {
            case R.id.shift_fragment_action_clock_in_out:
                onClockInOutClick();
                return true;

            default:
                return super.onOptionsItemSelected( item );
        }
    }

    private void update()
    {
        final Date start = mShift.getStart();
        final Date end = mShift.getEnd();
        if( start == null )
        {
            mShiftDate.setText( "Not Clocked-In" );
            mShiftClockInTime.setText( "Not clocked-in" );
        }
        else
        {
            mShiftDate.setText( Utilities.formatDate( start ) );
            mShiftClockInTime.setText( Utilities.formatDateTime( start ) );
        }
        if( end == null )
        {
            mShiftClockOutTime.setText( "Not clocked-out" );
        }
        else
        {
            mShiftClockOutTime.setText( Utilities.formatDateTime( end ) );
        }
    }

    private void onClockInOutClick()
    {
        ShiftDialogs.clockInOut( getActivity(), mShift, new ShiftDialogs.ShiftStatusListener()
        {
            @Override
            public void onShiftClockIn()
            {
                mShift.setStart( new Date() );
                mShift.saveEventually();
                update();
            }

            @Override
            public void onShiftClockOut()
            {
                mShift.setEnd( new Date() );
                mShift.saveEventually();
                update();
            }
        } );
    }
}
