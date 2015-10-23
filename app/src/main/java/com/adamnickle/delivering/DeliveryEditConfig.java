package com.adamnickle.delivering;

import android.os.Parcelable;

import java.math.BigDecimal;
import java.util.Date;

import auto.parcel.AutoParcel;


@AutoParcel
public abstract class DeliveryEditConfig implements Parcelable
{
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

    public static Builder builder()
    {
        return new AutoParcel_DeliveryEditConfig.Builder()
                .name( "" ).hasName( false )
                .start( new Date() ).hasStart( false )
                .end( new Date() ).hasEnd( false )
                .tip( BigDecimal.ZERO ).hasTip( false )
                .tipPaymentMethod( "" ).hasTipPaymentMethod( false )
                .total( BigDecimal.ZERO ).hasTotal( false )
                .totalPaymentMethod( "" ).hasTotalPaymentMethod( false )
                .startMileage( 0.0 ).hasStartMileage( false )
                .endMileage( 0.0 ).hasEndMileage( false );
    }
}
