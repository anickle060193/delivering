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

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.SaveCallback;


public class DeliveriesFragment extends Fragment
{
    public static final String FRAGMENT_TAG = DeliveriesFragment.class.getName();

    private View mMainView;
    private SwipeRefreshLayout mSwipeToRefreshLayout;
    private RecyclerView mDeliveriesList;
    private DeliveryAdapter mAdapter;

    public static DeliveriesFragment newInstance()
    {
        return new DeliveriesFragment();
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
            mMainView = inflater.inflate( R.layout.fragment_deliveries, container, false );

            mSwipeToRefreshLayout = (SwipeRefreshLayout)mMainView.findViewById( R.id.deliveries_fragment_swipe_refresh_layout );
            mDeliveriesList = (RecyclerView)mMainView.findViewById( R.id.deliveries_fragment_list );
            mAdapter = new DeliveryAdapter( getActivity(), new ParseObjectArrayAdapter.ParseQueryFactory<Delivery>()
            {
                @Override
                public ParseQuery<Delivery> getQuery()
                {
                    return Delivery.createQuery()
                            .whereEqualTo( Delivery.DELIVERER, Deliverer.getCurrentUser() )
                            .addDescendingOrder( Delivery.CREATED_AT );
                }
            } );
            mDeliveriesList.setAdapter( mAdapter );
            mAdapter.addOnQueryListener( new ParseObjectArrayAdapter.OnQueryListener()
            {
                @Override
                public void onQueryStarted()
                {
                    mSwipeToRefreshLayout.setRefreshing( true );
                }

                @Override
                public void onQueryEnded( boolean successful )
                {
                    mSwipeToRefreshLayout.setRefreshing( false );
                }
            } );

            mSwipeToRefreshLayout.setColorSchemeResources( R.color.colorAccent );
            mSwipeToRefreshLayout.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener()
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
        inflater.inflate( R.menu.fragment_deliveries, menu );
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        switch( item.getItemId() )
        {
            case R.id.deliveries_fragment_action_add_delivery:
                onCreateDeliveryClick();
                return true;

            default:
                return super.onOptionsItemSelected( item );
        }
    }

    private void onCreateDeliveryClick()
    {
        DeliveryDialogs.create( getActivity(), new DeliveryDialogs.DeliveryCreatorListener()
        {
            @Override
            public void onDeliveryCreated( final String deliveryName )
            {
                Shift.createQuery()
                        .whereDoesNotExist( Shift.END )
                        .whereExists( Shift.START )
                        .addDescendingOrder( Shift.START )
                        .getFirstInBackground( new GetCallback<Shift>()
                        {
                            @Override
                            public void done( Shift shift, ParseException e )
                            {
                                final Deliverer deliverer = Deliverer.getCurrentUser();
                                final Delivery delivery = Delivery.create( deliverer, shift, deliveryName );
                                delivery.pinInBackground( new SaveCallback()
                                {
                                    @Override
                                    public void done( ParseException ex )
                                    {
                                        if( ex == null )
                                        {
                                            mAdapter.add( 0, delivery );
                                            delivery.saveEventually();
                                        }
                                        else
                                        {
                                            Delivering.log( "Delivery could not be pinned.", ex );
                                            Delivering.oops( ex );
                                        }
                                    }
                                } );
                            }
                        } );
            }
        } );
    }
}
