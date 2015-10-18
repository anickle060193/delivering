package com.adamnickle.delivering;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.Date;


public class ShiftsFragment extends Fragment
{
    public static final String FRAGMENT_TAG = ShiftsFragment.class.getName();

    private View mMainView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mShiftsList;
    private ShiftsArrayAdapter mAdapter;

    public static ShiftsFragment newInstance()
    {
        return new ShiftsFragment();
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
            mMainView = inflater.inflate( R.layout.fragment_shifts, container, false );

            mSwipeRefreshLayout = (SwipeRefreshLayout)mMainView.findViewById( R.id.shifts_fragment_swipe_refresh_layout );
            mShiftsList = (RecyclerView)mMainView.findViewById( R.id.shifts_fragment_list );
            mAdapter = new ShiftsArrayAdapter();
            mShiftsList.setAdapter( mAdapter );
            mAdapter.addOnQueryListener( new ParseObjectArrayAdapter.OnQueryListener()
            {
                @Override
                public void onQueryStarted()
                {
                    mSwipeRefreshLayout.setRefreshing( true );
                }

                @Override
                public void onQueryEnded( boolean successful )
                {
                    mSwipeRefreshLayout.setRefreshing( false );
                }
            } );

            mSwipeRefreshLayout.setColorSchemeResources( R.color.colorAccent );
            mSwipeRefreshLayout.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener()
            {
                @Override
                public void onRefresh()
                {
                    mAdapter.refresh();
                }
            } );
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
        inflater.inflate( R.menu.fragment_shifts, menu );
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        switch( item.getItemId() )
        {
            case R.id.shifts_fragment_action_add_shift:
                onClickCreateShift();
                return true;

            default:
                return super.onOptionsItemSelected( item );
        }
    }

    private void onClickCreateShift()
    {
        ShiftDialogs.create( getActivity(), new ShiftDialogs.ShiftCreatorListener()
        {
            @Override
            public void OnShiftCreated( boolean clockIn )
            {
                final Shift shift = Shift.createShift( Deliverer.getCurrentUser() );
                if( clockIn )
                {
                    shift.setStart( new Date() );
                }
                shift.pinInBackground( new SaveCallback()
                {
                    @Override
                    public void done( ParseException ex )
                    {
                        if( ex == null )
                        {
                            mAdapter.add( 0, shift );
                            shift.saveEventually();
                        }
                        else
                        {
                            Delivering.log( "Could not create new Shift", ex );
                            Delivering.toast( "Shift couldn't be created. Try again." );
                        }
                    }
                } );
            }
        } );
    }

    private void openShift( ShiftViewHolder holder )
    {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack( null )
                .replace( R.id.main_activity_content_holder, ShiftFragment.newInstance( holder.Shift ) )
                .commit();
    }

    private void onClockInOutClick( final ShiftViewHolder holder )
    {
        ShiftDialogs.clockInOut( getActivity(), holder.Shift, new ShiftDialogs.ShiftStatusListener()
        {
            @Override
            public void onShiftClockIn()
            {
                holder.Shift.setStart( new Date() );
                holder.Shift.saveEventually();
                holder.update();
            }

            @Override
            public void onShiftClockOut()
            {
                holder.Shift.setEnd( new Date() );
                holder.Shift.saveEventually();
                holder.update();
            }
        } );
    }

    private class ShiftViewHolder extends ParseObjectArrayAdapter.ViewHolder
    {
        public Shift Shift;
        public final TextView ShiftDate;
        public final TextView ClockInTime;
        public final TextView ClockOutTime;
        public final View ClockInOut;

        public ShiftViewHolder( View itemView )
        {
            super( itemView );

            ShiftDate = findViewById( R.id.shift_item_date );
            ClockInTime = findViewById( R.id.shift_item_clock_in_time );
            ClockOutTime = findViewById( R.id.shift_item_clock_out_time );
            ClockInOut = findViewById( R.id.shift_item_clock_in_out );

            itemView.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v )
                {
                    openShift( ShiftViewHolder.this );
                }
            } );
            ClockInOut.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v )
                {
                    onClockInOutClick( ShiftViewHolder.this );
                }
            } );
        }

        public void update()
        {
            if( Shift != null )
            {
                final Date start = Shift.getStart();
                final Date end = Shift.getEnd();
                if( start == null )
                {
                    final Date createdAt = Shift.getCreatedAt();
                    ShiftDate.setText( Utilities.DATE_FORMAT.format( createdAt ) );
                    ClockInTime.setText( "[Not clocked-in yet]" );
                }
                else
                {
                    ShiftDate.setText( Utilities.DATE_FORMAT.format( start ) );
                    ClockInTime.setText( Utilities.SHORT_TIME_FORMAT.format( start ) );
                }
                if( end == null )
                {
                    ClockOutTime.setText( "[Not clocked-out yet]" );
                }
                else
                {
                    ClockOutTime.setText( Utilities.SHORT_TIME_FORMAT.format( end ) );
                }
            }
        }
    }

    private class ShiftsArrayAdapter extends ParseObjectArrayAdapter<Shift, ShiftViewHolder>
    {
        public ShiftsArrayAdapter()
        {
            super( new ParseQueryFactory<Shift>()
            {
                @Override
                public ParseQuery<Shift> getQuery()
                {
                    return Shift.createQuery()
                            .whereEqualTo( Shift.DELIVERER, Deliverer.getCurrentUser() )
                            .addDescendingOrder( Shift.CREATED_AT );
                }
            } );
        }

        @Override
        public ShiftViewHolder onCreateParseObjectViewHolder( ViewGroup parent, int viewType )
        {
            final View view = LayoutInflater.from( parent.getContext() ).inflate( R.layout.shift_item_layout, parent, false );
            return new ShiftViewHolder( view );
        }

        @Override
        public void onBindParseObjectViewHolder( ShiftViewHolder holder, int position )
        {
            holder.Shift = get( position );
            holder.update();
        }
    }
}
