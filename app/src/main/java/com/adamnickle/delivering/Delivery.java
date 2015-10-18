package com.adamnickle.delivering;

import com.parse.ParseACL;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.math.BigDecimal;
import java.util.Date;


@ParseClassName( "Delivery" )
public class Delivery extends ParseObject
{
    public static final String NAME = "name";
    public static final String DELIVERER = "deliverer";
    public static final String SHIFT = "shift";
    public static final String START_MILEAGE = "start_mileage";
    public static final String END_MILEAGE = "end_mileage";
    public static final String TOTAL = "total";
    public static final String TOTAL_PAYMENT_METHOD = "total_payment_method";
    public static final String TIP = "tip";
    public static final String TIP_PAYMENT_METHOD = "tip_payment_method";
    public static final String DELIVERY_START = "delivery_started_at";
    public static final String DELIVERY_END = "delivered_at";

    public static final String CREATED_AT = "createdAt";
    public static final String UPDATED_AT = "updatedAt";

    public static Delivery create( Deliverer deliverer, Shift shift, String name )
    {
        final Delivery delivery = new Delivery();
        delivery.setACL( new ParseACL( deliverer ) );
        delivery.setDeliverer( deliverer );
        if( shift != null )
        {
            delivery.setShift( shift );
        }
        delivery.setName( name );
        return delivery;
    }

    public static ParseQuery<Delivery> createQuery()
    {
        return new ParseQuery<>( Delivery.class );
    }

    public boolean isCompleted()
    {
        return getDeliveryEnd() != null && getDeliveryStart() != null;
    }

    public boolean isInProgress()
    {
        return !isCompleted() && getDeliveryStart() != null;
    }

    public void setDeliverer( Deliverer deliverer )
    {
        put( DELIVERER, deliverer );
    }

    public Deliverer getDeliverer()
    {
        return (Deliverer)getParseUser( DELIVERER );
    }

    public void setShift( Shift shift )
    {
        put( SHIFT, shift );
    }

    public Shift getShift()
    {
        return (Shift)get( SHIFT );
    }

    public void setName( String name )
    {
        put( NAME, name );
    }

    public String getName()
    {
        return getString( NAME );
    }

    public void setStartMileage( double startMileage )
    {
        put( START_MILEAGE, startMileage );
    }

    public boolean hasStartMileage()
    {
        return has( START_MILEAGE );
    }

    public double getStartMileage()
    {
        return getDouble( START_MILEAGE );
    }

    public void setEndMileage( double endMileage )
    {
        put( END_MILEAGE, endMileage );
    }

    public boolean hasEndMileage()
    {
        return has( END_MILEAGE );
    }

    public double getEndMileage()
    {
        return getDouble( END_MILEAGE );
    }

    public void setTip( BigDecimal tip )
    {
        final String tipString = tip.toString();
        put( TIP, tipString );
    }

    public BigDecimal getTip()
    {
        final String tipString = getString( TIP );
        return tipString == null ? null : new BigDecimal( tipString );
    }

    public void setTipPaymentMethod( String paymentMethod )
    {
        put( TIP_PAYMENT_METHOD, paymentMethod );
    }

    public String getTipPaymentMethod()
    {
        return getString( TIP_PAYMENT_METHOD );
    }

    public void setTotal( BigDecimal total )
    {
        final String totalString = total.toString();
        put( TOTAL, totalString );
    }

    public BigDecimal getTotal()
    {
        final String totalString = getString( TOTAL );
        return totalString == null ? null : new BigDecimal( totalString );
    }

    public void setTotalPaymentMethod( String paymentMethod )
    {
        put( TOTAL_PAYMENT_METHOD, paymentMethod );
    }

    public String getTotalPaymentMethod()
    {
        return getString( TOTAL_PAYMENT_METHOD );
    }

    public void setDeliveryStart( Date date )
    {
        put( DELIVERY_START, date );
    }

    public Date getDeliveryStart()
    {
        return getDate( DELIVERY_START );
    }

    public void setDeliveryEnd( Date date )
    {
        put( DELIVERY_END, date );
    }

    public Date getDeliveryEnd()
    {
        return getDate( DELIVERY_END );
    }
}
