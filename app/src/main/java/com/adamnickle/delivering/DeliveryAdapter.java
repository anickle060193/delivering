package com.adamnickle.delivering;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.SaveCallback;

import java.math.BigDecimal;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class DeliveryAdapter extends ParseObjectArrayAdapter<Delivery, DeliveryAdapter.DeliveryViewHolder>
{
    private final Context mContext;

    public DeliveryAdapter( Context context, ParseQueryFactory<Delivery> factory )
    {
        super( factory );

        mContext = context;
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
                holder.Delivery.saveInBackground( new SaveCallback()
                {
                    @Override
                    public void done( ParseException e )
                    {
                        if( e != null )
                        {
                            Delivering.log( "Error occurred while setting Delivery payment." );
                            Delivering.oops();
                        }
                    }
                } );
            }
        } );
        holder.update();
    }

    private void onCompleteDeliveryClick( final DeliveryViewHolder holder )
    {
        DeliveryDialogs.completeDelivery( mContext, new DeliveryDialogs.DeliveryCompleteListener()
        {
            @Override
            public void onDeliveryComplete()
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
        } );
    }

    private void onStartDeliveryClick( final DeliveryViewHolder holder )
    {
        DeliveryDialogs.startDelivery( mContext, new DeliveryDialogs.DeliveryStartListener()
        {
            @Override
            public void onDeliveryStarted()
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
        } );
    }

    public class DeliveryViewHolder extends ParseObjectArrayAdapter.ViewHolder
    {
        public Delivery Delivery;
        public final TextView DeliveryName;
        public final TextView DeliveryStatus;
        public final TextView DeliveryTip;
        public final View SetPayment;
        public final View UpdateDeliveryStatus;
        public final View DeliveryStatusInProgress;
        public final View DeliveryStatusCompleted;

        public DeliveryViewHolder( View itemView )
        {
            super( itemView );

            DeliveryName = findViewById( R.id.delivery_item_name );
            DeliveryStatus = findViewById( R.id.delivery_item_status );
            DeliveryTip = findViewById( R.id.delivery_item_tip );
            SetPayment = findViewById( R.id.delivery_item_set_payment );
            UpdateDeliveryStatus = findViewById( R.id.delivery_item_update_status );
            DeliveryStatusInProgress = findViewById( R.id.delivery_item_status_in_progress );
            DeliveryStatusCompleted = findViewById( R.id.delivery_item_status_completed );

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
}
