package com.adamnickle.delivering;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseQuery;


public class DeliveriesFragment extends Fragment
{
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

        public DeliveryViewHolder( View itemView )
        {
            super( itemView );

            DeliveryName = (TextView)itemView.findViewById( R.id.delivery_name );
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
                    return Delivery.createQuery().addAscendingOrder( Delivery.NAME );
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
            final Delivery delivery = get( position );
            holder.Delivery = delivery;
            holder.DeliveryName.setText( delivery.getName() );
        }
    }
}
