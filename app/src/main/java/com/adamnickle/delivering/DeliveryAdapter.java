package com.adamnickle.delivering;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.Date;


public class DeliveryAdapter extends ParseObjectArrayAdapter<Delivery, DeliveryAdapter.DeliveryViewHolder>
{
    public interface DeliveryAdapterListener
    {
        void onDeliveryClick( Delivery delivery );
    }

    private final Context mContext;

    private DeliveryAdapterListener mListener;

    public DeliveryAdapter( Context context, ParseQueryFactory<Delivery> factory )
    {
        super( factory );

        mContext = context;
    }

    public void setListener( DeliveryAdapterListener listener )
    {
        mListener = listener;
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

    private void onSetPaymentClick( final DeliveryViewHolder holder )
    {
        DeliveryDialogs.setPayment( mContext, holder.Delivery, new DeliveryDialogs.DeliveryPaymentSetListener()
        {
            @Override
            public void onDeliveryPaymentSet( BigDecimal total, String totalPaymentMethod, BigDecimal tip, String tipPaymentMethod )
            {
                holder.Delivery.setTip( tip );
                holder.Delivery.setTipPaymentMethod( tipPaymentMethod );
                holder.Delivery.setTotal( total );
                holder.Delivery.setTotalPaymentMethod( totalPaymentMethod );
                holder.Delivery.saveEventually();
                holder.update();
            }
        } );
    }

    private void onCompleteDeliveryClick( final DeliveryViewHolder holder )
    {
        DeliveryDialogs.completeDelivery( mContext, holder.Delivery, new DeliveryDialogs.DeliveryCompleteListener()
        {
            @Override
            public void onDeliveryComplete( double endMileage )
            {
                holder.Delivery.setDeliveryEnd( new Date() );
                holder.Delivery.setEndMileage( endMileage );
                holder.Delivery.saveEventually();
                holder.update();
            }
        } );
    }

    private void onStartDeliveryClick( final DeliveryViewHolder holder )
    {
        DeliveryDialogs.startDelivery( mContext, new DeliveryDialogs.DeliveryStartListener()
        {
            @Override
            public void onDeliveryStarted( double startMileage )
            {
                holder.Delivery.setDeliveryStart( new Date() );
                holder.Delivery.setStartMileage( startMileage );
                holder.Delivery.saveEventually();
                holder.update();
            }
        } );
    }

    public class DeliveryViewHolder extends ParseObjectArrayAdapter.ViewHolder
    {
        public Delivery Delivery;
        public final TextView DeliveryName;
        public final TextView DeliveryStatus;
        public final View DeliveryTotalGroup;
        public final TextView DeliveryTotal;
        public final TextView DeliveryTotalTotalPaymentMethod;
        public final View DeliveryTipGroup;
        public final TextView DeliveryTip;
        public final TextView DeliveryTipPaymentMethod;
        public final View DeliveryMileageGroup;
        public final TextView DeliveryMileage;
        public final View SetPayment;
        public final View UpdateDeliveryStatus;
        public final View DeliveryStatusInProgress;
        public final View DeliveryStatusCompleted;

        public DeliveryViewHolder( View itemView )
        {
            super( itemView );

            DeliveryName = findViewById( R.id.delivery_item_name );
            DeliveryStatus = findViewById( R.id.delivery_item_status );
            DeliveryTotalGroup = findViewById( R.id.delivery_item_total_group );
            DeliveryTotal = findViewById( R.id.delivery_item_total );
            DeliveryTotalTotalPaymentMethod = findViewById( R.id.delivery_item_total_payment_method );
            DeliveryTipGroup = findViewById( R.id.delivery_item_tip_group );
            DeliveryTip = findViewById( R.id.delivery_item_tip );
            DeliveryTipPaymentMethod = findViewById( R.id.delivery_item_tip_payment_method );
            DeliveryMileageGroup = findViewById( R.id.delivery_item_mileage_group );
            DeliveryMileage = findViewById( R.id.delivery_item_mileage );
            SetPayment = findViewById( R.id.delivery_item_set_payment );
            UpdateDeliveryStatus = findViewById( R.id.delivery_item_update_status );
            DeliveryStatusInProgress = findViewById( R.id.delivery_item_status_in_progress );
            DeliveryStatusCompleted = findViewById( R.id.delivery_item_status_completed );

            itemView.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v )
                {
                    if( mListener != null )
                    {
                        mListener.onDeliveryClick( DeliveryViewHolder.this.Delivery );
                    }
                }
            } );

            SetPayment.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v )
                {
                    onSetPaymentClick( DeliveryViewHolder.this );
                }
            } );

            UpdateDeliveryStatus.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v )
                {
                    if( Delivery.isCompleted() )
                    {
                        Delivering.toast( "Delivery already complete" );
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

                if( Delivery.isCompleted() )
                {
                    DeliveryStatusInProgress.setVisibility( View.GONE );
                    DeliveryStatusCompleted.setVisibility( View.VISIBLE );
                    final long start = Delivery.getDeliveryStart().getTime();
                    final long end = Delivery.getDeliveryEnd().getTime();
                    final String timeSpan = Formatter.timeSpan( end - start );
                    DeliveryStatus.setText( "Delivery completed in " + timeSpan );
                }
                else if( Delivery.isInProgress() )
                {
                    DeliveryStatusInProgress.setVisibility( View.VISIBLE );
                    DeliveryStatusCompleted.setVisibility( View.GONE );
                    final long start = Delivery.getDeliveryStart().getTime();
                    final String timeSpan = Formatter.pastTime( start );
                    DeliveryStatus.setText( "Delivery started " + timeSpan );
                }
                else
                {
                    DeliveryStatusInProgress.setVisibility( View.GONE );
                    DeliveryStatusCompleted.setVisibility( View.GONE );
                    DeliveryStatus.setText( "Delivery not yet started." );
                }

                Formatter.deliveryTipTotal( Delivery, DeliveryTip, DeliveryTipPaymentMethod, DeliveryTotal, DeliveryTotalTotalPaymentMethod );
                DeliveryTotalGroup.setVisibility( Delivery.getTotal() == null ? View.GONE : View.VISIBLE );
                DeliveryTipGroup.setVisibility( Delivery.getTip() == null ? View.GONE : View.VISIBLE );

                if( !Delivery.hasStartMileage() || !Delivery.hasEndMileage() )
                {
                    DeliveryMileageGroup.setVisibility( View.GONE );
                }
                else
                {
                    Formatter.deliveryTotalMileage( Delivery, DeliveryMileage );
                }
            }
        }
    }
}
