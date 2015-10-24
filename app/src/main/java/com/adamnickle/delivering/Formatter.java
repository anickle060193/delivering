package com.adamnickle.delivering;

import android.widget.TextView;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public final class Formatter
{
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance();
    private static final NumberFormat PLAIN_MONEY_FORMATTER = new DecimalFormat( "0.00" );
    private static final DateFormat DATE_FORMAT = SimpleDateFormat.getDateInstance();
    private static final DateFormat TIME_FORMAT = SimpleDateFormat.getTimeInstance( DateFormat.SHORT );
    private static final DateFormat DATE_TIME_FORMAT = SimpleDateFormat.getDateTimeInstance( DateFormat.SHORT, DateFormat.SHORT );
    private static final NumberFormat MILEAGE_FORMATTER = new DecimalFormat( "0.0" );

    private Formatter() { }

    public static String timeSpan( long timeSpan )
    {
        final long minutes = TimeUnit.MINUTES.convert( timeSpan, TimeUnit.MILLISECONDS );
        return String.valueOf( minutes ) + " minutes";
    }

    public static String pastTime( long time )
    {
        final long minutes = TimeUnit.MINUTES.convert( System.currentTimeMillis() - time, TimeUnit.MILLISECONDS );
        return String.valueOf( minutes ) + " minutes ago";
    }

    public static String currency( BigDecimal currency )
    {
        if( currency != null )
        {
            return CURRENCY_FORMATTER.format( currency );
        }
        return "";
    }

    public static String plainMoney( BigDecimal money )
    {
        if( money != null )
        {
            return PLAIN_MONEY_FORMATTER.format( money );
        }
        return "";
    }

    public static String date( Date date )
    {
        if( date != null )
        {
            return DATE_FORMAT.format( date );
        }
        return "";
    }

    public static String time( Date date )
    {
        if( date != null )
        {
            return TIME_FORMAT.format( date );
        }
        return "";
    }

    public static String dateTime( Date date )
    {
        if( date != null )
        {
            return DATE_TIME_FORMAT.format( date );
        }
        return "";
    }

    public static DateFormat getDateTimeFormat()
    {
        return DATE_TIME_FORMAT;
    }

    public static String mileage( double mileage )
    {
        return MILEAGE_FORMATTER.format( mileage );
    }

    public static void shiftStartEndTimes( Shift shift, TextView dateView, TextView startView, TextView endView )
    {
        if( shift.isCompleted() )
        {
            final Date start = shift.getStart();
            final Date end = shift.getEnd();

            dateView.setText( Formatter.date( start ) );
            startView.setText( Formatter.time( start ) );
            endView.setText( Formatter.time( end ) );
        }
        else if( shift.isInProgress() )
        {
            final Date start = shift.getStart();

            dateView.setText( Formatter.date( start ) );
            startView.setText( Formatter.time( start ) );
            endView.setText( "Not clocked out" );
        }
        else
        {
            dateView.setText( "Not clocked in" );
            startView.setText( "Not clocked in" );
            endView.setText( "Not clocked out" );
        }
    }

    public static void deliveryStartEndTimes( Delivery delivery, TextView totalTimeView, TextView startView, TextView endView )
    {
        if( delivery.isCompleted() )
        {
            final Date start = delivery.getDeliveryStart();
            final Date end = delivery.getDeliveryEnd();
            final String timeSpan = Formatter.timeSpan( end.getTime() - start.getTime() );
            totalTimeView.setText( timeSpan );
            startView.setText( Formatter.time( start ) );
            endView.setText( Formatter.time( end ) );
        }
        else if( delivery.isInProgress() )
        {
            final Date start = delivery.getDeliveryStart();
            final String pastTime = Formatter.pastTime( start.getTime() );
            totalTimeView.setText( "Started " + pastTime );
            startView.setText( Formatter.time( start ) );
            endView.setText( "Not yet ended" );
        }
        else
        {
            totalTimeView.setText( "--------" );
            startView.setText( "Not yet started" );
            endView.setText( "Not yet started" );
        }
    }

    public static void deliveryTotalMileage( Delivery delivery, TextView totalMileageView )
    {
        if( delivery.isCompleted() )
        {
            final double startMileage = delivery.getStartMileage();
            final double endMileage = delivery.getEndMileage();
            totalMileageView.setText( Formatter.mileage( endMileage - startMileage ) + " miles" );
        }
        else if( delivery.isInProgress() )
        {
            totalMileageView.setText( "--------" );
        }
        else
        {
            totalMileageView.setText( "--------" );
        }
    }

    public static void deliveryStartEndMileage( Delivery delivery, TextView totalMileageView, TextView startMileageView, TextView endMileageView )
    {
        Formatter.deliveryTotalMileage( delivery, totalMileageView );
        if( delivery.isCompleted() )
        {
            final double startMileage = delivery.getStartMileage();
            final double endMileage = delivery.getEndMileage();
            startMileageView.setText( Formatter.mileage( startMileage ) );
            endMileageView.setText( Formatter.mileage( endMileage ) );
        }
        else if( delivery.isInProgress() )
        {
            final double startMileage = delivery.getStartMileage();
            startMileageView.setText( Formatter.mileage( startMileage ) );
            endMileageView.setText( "Not yet ended" );
        }
        else
        {
            startMileageView.setText( "Not yet started" );
            endMileageView.setText( "Not yet started" );
        }
    }

    public static void deliveryTipTotal( Delivery delivery, TextView tipView, TextView tipPaymentMethodView, TextView totalView, TextView totalPaymentMethodView )
    {
        final BigDecimal tip = delivery.getTip();
        if( tip != null )
        {
            tipView.setText( Formatter.currency( tip ) );
        }
        else
        {
            tipView.setText( "No tip yet" );
        }
        tipPaymentMethodView.setText( delivery.getTipPaymentMethod() );

        final BigDecimal total = delivery.getTotal();
        if( total != null )
        {
            totalView.setText( Formatter.currency( total ) );
        }
        else
        {
            totalView.setText( "No total yet" );
        }
        totalPaymentMethodView.setText( delivery.getTotalPaymentMethod() );
    }
}
