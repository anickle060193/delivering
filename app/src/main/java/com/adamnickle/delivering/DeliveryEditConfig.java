package com.adamnickle.delivering;

import android.os.Parcelable;

import auto.parcel.AutoParcel;


@AutoParcel
public abstract class DeliveryEditConfig implements Parcelable
{
    abstract String activityTitle();

    abstract boolean showTipIncluded();

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

    private static Builder builder( boolean showAll )
    {
        return new AutoParcel_DeliveryEditConfig.Builder()
                .activityTitle( "Delivery" )
                .showTipIncluded( showAll )
                .hasName( showAll )
                .hasStart( showAll )
                .hasEnd( showAll )
                .hasTip( showAll )
                .hasTipPaymentMethod( showAll )
                .hasTotal( showAll )
                .hasTotalPaymentMethod( showAll )
                .hasStartMileage( showAll )
                .hasEndMileage( showAll );
    }

    public static Builder creating()
    {
        return builder( false )
                .activityTitle( "Create Delivery" )
                .hasName( true );
    }

    public static Builder setTip()
    {
        return builder( false )
                .activityTitle( "Set Tip" )
                .showTipIncluded( true )
                .hasTip( true )
                .hasTipPaymentMethod( true )
                .hasTotal( true )
                .hasTotalPaymentMethod( true );
    }

    public static Builder startDelivery()
    {
        return builder( false )
                .activityTitle( "Start Delivery" )
                .hasStartMileage( true );
    }

    public static Builder endDelivery()
    {
        return builder( false )
                .activityTitle( "End Delivery" )
                .hasEndMileage( true );
    }

    public static Builder editing()
    {
        return builder( true )
                .activityTitle( "Edit Delivery" );
    }
}
