package com.adamnickle.delivering;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;


public class DeliveriesFragment extends Fragment
{
    public static final String FRAGMENT_TAG = DeliveriesFragment.class.getName();

    private static final Pattern TIP_PATTERN = Pattern.compile( "^[1-9]\\d*(?:\\.\\d{2})?$" );

    private View mMainView;
    private SwipeRefreshLayout mSwipeToRefreshLayout;
    private RecyclerView mDeliveriesList;
    private DeliveryArrayAdapter mAdapter;

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

            mSwipeToRefreshLayout = (SwipeRefreshLayout)mMainView.findViewById( R.id.deliveries_swipe_refresh_layout );
            mDeliveriesList = (RecyclerView)mMainView.findViewById( R.id.deliveries_list );
            mAdapter = new DeliveryArrayAdapter();
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
            case R.id.add_delivery:
                createDelivery();
                return true;

            default:
                return super.onOptionsItemSelected( item );
        }
    }

    private void createDelivery()
    {
        final AlertDialog dialog = new AlertDialog.Builder( getActivity() )
                .setView( R.layout.delivery_creator_dialog_layout )
                .setPositiveButton( "Create Delivery", null )
                .setNegativeButton( "Cancel", null )
                .show();
        final EditText distanceEditText = (EditText)dialog.findViewById( R.id.delivery_creator_distance );
        distanceEditText.setOnEditorActionListener( new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction( TextView v, int actionId, KeyEvent event )
            {
                if( actionId == EditorInfo.IME_ACTION_DONE )
                {
                    // This is gross
                    dialog.getButton( DialogInterface.BUTTON_POSITIVE ).callOnClick();
                    return true;
                }
                return false;
            }
        } );
        final EditText deliveryNameEditText = (EditText)dialog.findViewById( R.id.delivery_creator_name );
        dialog.getButton( DialogInterface.BUTTON_POSITIVE ).setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                final String deliveryName = deliveryNameEditText.getText().toString();

                final Deliverer deliverer = Deliverer.getCurrentUser();
                final Delivery deliver = Delivery.create( deliverer, deliveryName );
                deliver.saveInBackground( new SaveCallback()
                {
                    @Override
                    public void done( ParseException ex )
                    {
                        if( ex == null )
                        {
                            mAdapter.add( 0, deliver );
                            mDeliveriesList.scrollToPosition( 0 );
                        }
                        else
                        {
                            Delivering.log( "Created Delivery could not be saved.", ex );
                            Delivering.oops();
                        }
                    }
                } );
                dialog.dismiss();
            }
        } );
    }

    private void onSetTipClick( final DeliveryViewHolder holder )
    {
        final AlertDialog dialog = new AlertDialog.Builder( getActivity() )
                .setView( R.layout.delivery_tip_dialog_layout )
                .setPositiveButton( "Tip", null )
                .show();
        final EditText tipEditText = (EditText)dialog.findViewById( R.id.delivery_tip );
        final BigDecimal initialTip = holder.Delivery.getTip();
        if( initialTip != null )
        {
            tipEditText.setText( initialTip.toString() );
        }
        tipEditText.setOnEditorActionListener( new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction( TextView v, int actionId, KeyEvent event )
            {
                if( actionId == EditorInfo.IME_ACTION_DONE )
                {
                    // This is gross
                    dialog.getButton( DialogInterface.BUTTON_POSITIVE ).callOnClick();
                    return true;
                }
                return false;
            }
        } );
        dialog.getButton( DialogInterface.BUTTON_POSITIVE ).setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                final String tipString = tipEditText.getText().toString();
                if( TIP_PATTERN.matcher( tipString ).matches() )
                {
                    final BigDecimal tip = new BigDecimal( tipString );
                    setTip( holder, tip );
                    dialog.dismiss();
                }
                else
                {
                    tipEditText.setError( "Invalid tip format (e.g. 4.56)" );
                    tipEditText.requestFocus();
                }
            }
        } );
    }

    private void setTip( DeliveryViewHolder holder, BigDecimal tip )
    {
        holder.Delivery.setTip( tip );
        holder.Delivery.saveInBackground( new SaveCallback()
        {
            @Override
            public void done( ParseException e )
            {
                if( e != null )
                {
                    Delivering.log( "Error occurred while setting tip." );
                    Delivering.oops();
                }
            }
        } );
        holder.update();
    }

    private void onCompleteDeliveryClick( final DeliveryViewHolder holder )
    {
        new AlertDialog.Builder( getActivity() )
                .setMessage( "Are you sure you want to complete this delivery?" )
                .setPositiveButton( "Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick( DialogInterface dialog, int which )
                    {
                        completeDelivery( holder );
                    }
                } )
                .setNegativeButton( "No", null )
                .show();
    }

    private void completeDelivery( DeliveryViewHolder holder )
    {
        holder.Delivery.setDeliveryEnd( new Date() );
        holder.Delivery.saveInBackground( new SaveCallback()
        {
            @Override
            public void done( ParseException ex )
            {
                if( ex != null )
                {
                    Delivering.log( "Failed to complete Delivery.", ex );
                    Delivering.oops();
                }
            }
        } );
        holder.update();
    }

    private void onStartDeliveryClick( final DeliveryViewHolder holder )
    {
        new AlertDialog.Builder( getActivity() )
                .setTitle( holder.Delivery.getName() )
                .setMessage( "Are you sure you want to start this delivery?" )
                .setPositiveButton( "Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick( DialogInterface dialog, int which )
                    {
                        startDelivery( holder );
                    }
                } )
                .setNegativeButton( "No", null )
                .show();
    }

    private void startDelivery( DeliveryViewHolder holder )
    {
        holder.Delivery.setDeliveryStart( new Date() );
        holder.Delivery.saveInBackground( new SaveCallback()
        {
            @Override
            public void done( ParseException ex )
            {
                if( ex != null )
                {
                    Delivering.log( "Failed to start Delivery.", ex );
                    Delivering.oops();
                }
            }
        } );
        holder.update();
    }

    private class DeliveryViewHolder extends ParseObjectArrayAdapter.ViewHolder
    {
        public Delivery Delivery;
        public final TextView DeliveryName;
        public final TextView DeliveryStatus;
        public final TextView DeliveryTip;
        public final View SetTip;
        public final View UpdateDeliveryStatus;
        public final View DeliveryStatusInProgress;
        public final View DeliveryStatusCompleted;

        public DeliveryViewHolder( View itemView )
        {
            super( itemView );

            DeliveryName = findViewById( R.id.delivery_name );
            DeliveryStatus = findViewById( R.id.delivery_status );
            DeliveryTip = findViewById( R.id.delivery_tip );
            SetTip = findViewById( R.id.delivery_set_tip );
            UpdateDeliveryStatus = findViewById( R.id.delivery_update_status );
            DeliveryStatusInProgress = findViewById( R.id.delivery_status_in_progress );
            DeliveryStatusCompleted = findViewById( R.id.delivery_status_completed );

            itemView.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v )
                {
                    if( Delivery != null )
                    {
                        Delivery.setDeliveryEnd( new Date() );
                        Delivery.saveEventually();
                        update();
                    }
                }
            } );

            SetTip.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v )
                {
                    onSetTipClick( DeliveryViewHolder.this );
                }
            } );

            UpdateDeliveryStatus.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v )
                {
                    if( Delivery.isCompleted() )
                    {
                        onSetTipClick( DeliveryViewHolder.this );
                    }
                    else if( Delivery.isInProgress() )
                    {
                        onCompleteDeliveryClick( DeliveryViewHolder.this );
                    }
                    else
                    {
                        onStartDeliveryClick( DeliveryViewHolder.this );
                    }
                }
            } );
        }

        public void update()
        {
            if( Delivery != null )
            {
                DeliveryName.setText( Delivery.getName() );

                final BigDecimal tip = Delivery.getTip();
                if( tip == null )
                {
                    DeliveryTip.setText( "No tip yet" );
                }
                else
                {
                    DeliveryTip.setText( Utilities.CURRENCY_FORMATTER.format( tip ) );
                }

                if( Delivery.isCompleted() )
                {
                    DeliveryStatusInProgress.setVisibility( View.GONE );
                    DeliveryStatusCompleted.setVisibility( View.VISIBLE );
                    final long start = Delivery.getDeliveryStart().getTime();
                    final long end = Delivery.getDeliveryEnd().getTime();
                    final long minutes = TimeUnit.MINUTES.convert( end - start, TimeUnit.MILLISECONDS );
                    DeliveryStatus.setText( "Delivery completed in " + minutes + " minutes" );
                }
                else if( Delivery.isInProgress() )
                {
                    DeliveryStatusInProgress.setVisibility( View.VISIBLE );
                    DeliveryStatusCompleted.setVisibility( View.GONE );
                    final long start = Delivery.getDeliveryStart().getTime();
                    final long now = System.currentTimeMillis();
                    final long minutes = TimeUnit.MINUTES.convert( now - start, TimeUnit.MILLISECONDS );
                    DeliveryStatus.setText( "Delivery started " + minutes + " minutes ago" );
                }
                else
                {
                    DeliveryStatusInProgress.setVisibility( View.GONE );
                    DeliveryStatusCompleted.setVisibility( View.GONE );
                    DeliveryStatus.setText( "Delivery not yet started." );
                }
            }
        }
    }

    private class DeliveryArrayAdapter extends ParseObjectArrayAdapter<Delivery, DeliveryViewHolder>
    {
        public DeliveryArrayAdapter()
        {
            super( new ParseQueryFactory<Delivery>()
            {
                @Override
                public ParseQuery<Delivery> getQuery()
                {
                    return Delivery.createQuery()
                            .whereEqualTo( Delivery.DELIVERER, Deliverer.getCurrentUser() )
                            .addDescendingOrder( Delivery.CREATED_AT );
                }
            } );
        }

        @Override
        public DeliveryViewHolder onCreateParseObjectViewHolder( ViewGroup parent, int viewType )
        {
            final View view = LayoutInflater.from( parent.getContext() ).inflate( R.layout.delivery_item_layout, parent, false );
            return new DeliveryViewHolder( view );
        }

        @Override
        public void onBindParseObjectViewHolder( DeliveryViewHolder holder, int position )
        {
            holder.Delivery = get( position );
            holder.update();
        }
    }
}
