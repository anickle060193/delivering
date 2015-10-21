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
    private ShiftsAdapter mAdapter;

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

            mAdapter = new ShiftsAdapter( getActivity(), mShiftQueryFactory );
            mAdapter.setListener( mShiftsAdapterListener );
            mAdapter.addOnQueryListener( mQueryListener );

            mSwipeRefreshLayout = (SwipeRefreshLayout)mMainView.findViewById( R.id.shifts_fragment_swipe_refresh_layout );
            mSwipeRefreshLayout.setColorSchemeResources( R.color.colorAccent );
            mSwipeRefreshLayout.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener()
            {
                @Override
                public void onRefresh()
                {
                    mAdapter.refresh();
                }
            } );

            mShiftsList = (RecyclerView)mMainView.findViewById( R.id.shifts_fragment_list );
            mShiftsList.setAdapter( mAdapter );
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
                            Delivering.oops( ex );
                        }
                    }
                } );
            }
        } );
    }

    final ParseObjectArrayAdapter.ParseQueryFactory<Shift> mShiftQueryFactory = new ParseObjectArrayAdapter.ParseQueryFactory<Shift>()
    {
        @Override
        public ParseQuery<Shift> getQuery()
        {
            return Shift.createQuery()
                    .whereEqualTo( Shift.DELIVERER, Deliverer.getCurrentUser() )
                    .addDescendingOrder( Shift.CREATED_AT );
        }
    };

    private final ShiftsAdapter.ShiftsAdapterListener mShiftsAdapterListener = new ShiftsAdapter.ShiftsAdapterListener()
    {
        @Override
        public void onShiftClicked( Shift shift )
        {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .addToBackStack( null )
                    .replace( R.id.main_activity_content_holder, ShiftFragment.newInstance( shift ) )
                    .commit();
        }
    };

    private final ParseObjectArrayAdapter.OnQueryListener mQueryListener = new ParseObjectArrayAdapter.OnQueryListener()
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
    };
}
