package com.adamnickle.delivering;

import android.os.Parcelable;

import java.math.BigDecimal;
import java.util.Date;

import auto.parcel.AutoParcel;


@AutoParcel
public abstract class DeliveryEditConfig implements Parcelable
{
    abstract String activityTitle();

    abstract boolean showTipIncluded();

    abstract String name();
    abstract Date start();
    abstract Date end();
    abstract BigDecimal tip();
    abstract String tipPaymentMethod();
    abstract BigDecimal total();
    abstract String totalPaymentMethod();
    abstract double startMileage();
    abstract double endMileage();

    abstract boolean hasName();
    abstract boolean hasStart();
    abstract boolean hasEnd();
    abstract boolean hasTip();
    abstract boolean hasTipPaymentMethod();
    abstract boolean hasTotal();
    abstract boolean hasTotalPaymentMethod();
    abstract boolean hasStartMileage();
    abstract boolean hasEndMileage();

    @AutoParcel.Builder
    public abstract static class Builder
    {
        public abstract DeliveryEditConfig build();

        public abstract Builder activityTitle( String title );

        public abstract Builder showTipIncluded( boolean show );

        public abstract Builder name( String name );
        public abstract Builder start( Date start );
        public abstract Builder end( Date end );
        public abstract Builder tip( BigDecimal tip );
        public abstract Builder tipPaymentMethod( String tipPaymentMethod );
        public abstract Builder total( BigDecimal total );
        public abstract Builder totalPaymentMethod( String totalPaymentMethod );
        public abstract Builder startMileage( double startMileage );
        public abstract Builder endMileage( double endMileage );

        public abstract Builder hasName( boolean has );
        public abstract Builder hasStart( boolean has );
        public abstract Builder hasEnd( boolean has );
        public abstract Builder hasTip( boolean has );
        public abstract Builder hasTipPaymentMethod( boolean has );
        public abstract Builder hasTotal( boolean has );
        public abstract Builder hasTotalPaymentMethod( boolean has );
        public abstract Builder hasStartMileage( boolean has );
        public abstract Builder hasEndMileage( boolean has );
    }

    public void updateDelivery( Delivery delivery )
    {
        delivery.setName( name() );
        delivery.setDeliveryStart( start() );
        delivery.setDeliveryEnd( end() );
        delivery.setTip( tip() );
        delivery.setTipPaymentMethod( tipPaymentMethod() );
        delivery.setTotal( total() );
        delivery.setTotalPaymentMethod( totalPaymentMethod() );
        delivery.setStartMileage( startMileage() );
        delivery.setEndMileage( endMileage() );
    }

    private static Builder builder( boolean showAll )
    {
        return new AutoParcel_DeliveryEditConfig.Builder()
                .activityTitle( "Delivery" )
                .showTipIncluded( showAll )
                .name( "" ).hasName( showAll )
                .start( new Date() ).hasStart( showAll )
                .end( new Date() ).hasEnd( showAll )
                .tip( BigDecimal.ZERO ).hasTip( showAll )
                .tipPaymentMethod( "" ).hasTipPaymentMethod( showAll )
                .total( BigDecimal.ZERO ).hasTotal( showAll )
                .totalPaymentMethod( "" ).hasTotalPaymentMethod( showAll )
                .startMileage( 0.0 ).hasStartMileage( showAll )
                .endMileage( 0.0 ).hasEndMileage( showAll );
    }

    public static Builder creating()
    {
        return builder( false )
                .activityTitle( "Create Delivery" )
                .hasName( true );
    }

    public static Builder editing( Delivery delivery )
    {
        final Builder builder = builder( true )
                .activityTitle( "Edit Delivery" )
                .name( delivery.getName() )
                .startMileage( delivery.getStartMileage() )
                .endMileage( delivery.getEndMileage() );

        final Date start = delivery.getDeliveryStart();
        if( start != null )
        {
            builder.start( start );
        }

        final Date end = delivery.getDeliveryEnd();
        if( end != null )
        {
            builder.end( end );
        }

        final BigDecimal tip = delivery.getTip();
        if( tip != null )
        {
            builder.tip( tip );
        }

        final String tipPaymentMethod = delivery.getTipPaymentMethod();
        if( tipPaymentMethod != null )
        {
            builder.tipPaymentMethod( tipPaymentMethod );
        }

        final BigDecimal total = delivery.getTotal();
        if( total != null )
        {
            builder.total( total );
        }

        final String totalPaymentMethod = delivery.getTotalPaymentMethod();
        if( totalPaymentMethod != null )
        {
            builder.totalPaymentMethod( totalPaymentMethod );
        }

        return builder;
    }

    public static Builder copy( DeliveryEditConfig config )
    {
        return new AutoParcel_DeliveryEditConfig.Builder( config );
    }
}
