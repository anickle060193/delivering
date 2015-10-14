package com.adamnickle.delivering;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.text.DateFormat;
import java.util.Date;


public class DeliveriesFragment extends Fragment
{
    private static final DateFormat FORMATTER = DateFormat.getDateTimeInstance();

    private View mMainView;
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

            mDeliveriesList = (RecyclerView)mMainView.findViewById( R.id.deliveries_list );
            mAdapter = new DeliveryArrayAdapter();
            mDeliveriesList.setAdapter( mAdapter );
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
        inflater.inflate( R.menu.deliveries, menu );
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
        new AlertDialog.Builder( getActivity() )
                .setView( R.layout.delivery_creator_dialog_layout )
                .setPositiveButton( "Create Delivery", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick( DialogInterface dialogInterface, int which )
                    {
                        final AlertDialog dialog = (AlertDialog)dialogInterface;

                        final EditText deliveryNameEditText = (EditText)dialog.findViewById( R.id.delivery_name );
                        final String deliveryName = deliveryNameEditText.getText().toString();

                        final Delivery deliver = Delivery.create( deliveryName );
                        deliver.saveInBackground( new SaveCallback()
                        {
                            @Override
                            public void done( ParseException ex )
                            {
                                if( ex == null )
                                {
                                    mAdapter.add( 0, deliver );
                                }
                                else
                                {
                                    Delivering.log( "Created Delivery could not be saved.", ex );
                                    Delivering.toast( "Delivery could not be created at this time." );
                                }
                            }
                        } );
                    }
                } )
                .setNegativeButton( "Cancel", null )
                .show();
    }

    private class DeliveryViewHolder extends ParseObjectArrayAdapter.ViewHolder
    {
        public Delivery Delivery;
        public final TextView DeliveryName;
        public final TextView DeliveredAt;

        public DeliveryViewHolder( View itemView )
        {
            super( itemView );

            DeliveryName = (TextView)itemView.findViewById( R.id.delivery_name );
            DeliveredAt = (TextView)itemView.findViewById( R.id.delivery_delivered_at );
            itemView.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v )
                {
                    if( Delivery != null )
                    {
                        Delivery.setDeliveryEnd( new Date() );
                        Delivery.pinInBackground();
                        update();
                    }
                }
            } );
        }

        public void update()
        {
            if( Delivery != null )
            {
                DeliveryName.setText( Delivery.getName() );
                final Date date = Delivery.getDeliveryEnd();
                if( date == null )
                {
                    DeliveredAt.setText( "Not yet delivered." );
                }
                else
                {
                    DeliveredAt.setText( "Delivered At: " + FORMATTER.format( date ) );
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
                            .fromLocalDatastore()
                            .addDescendingOrder( Delivery.DELIVERY_END )
                            .addDescendingOrder( Delivery.CREATED_AT )
                            .addAscendingOrder( Delivery.NAME );
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
