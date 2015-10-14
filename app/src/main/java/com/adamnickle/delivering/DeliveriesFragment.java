package com.adamnickle.delivering;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseQuery;

import java.text.DateFormat;
import java.util.Date;


public class DeliveriesFragment extends Fragment
{
    private static final DateFormat FORMATTER = DateFormat.getDateTimeInstance();

    private View mMainView;
    private RecyclerView mDeliveriesList;

    public static DeliveriesFragment newInstance()
    {
        return new DeliveriesFragment();
    }

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setRetainInstance( true );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        if( mMainView == null )
        {
            mMainView = inflater.inflate( R.layout.fragment_deliveries, container, false );

            mDeliveriesList = (RecyclerView)mMainView.findViewById( R.id.deliveries_list );
            mDeliveriesList.setAdapter( new DeliveryArrayAdapter() );
        }
        else
        {
            Utilities.removeFromParent( mMainView );
        }
        return mMainView;
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
                        Delivery.setDeliveredAt( new Date() );
                        Delivery.saveEventually();
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
                final Date date = Delivery.getDeliveredAt();
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
                            .addDescendingOrder( Delivery.DELIVERED_AT )
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
