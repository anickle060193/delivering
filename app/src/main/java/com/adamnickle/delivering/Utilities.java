package com.adamnickle.delivering;

import android.view.View;
import android.view.ViewGroup;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public final class Utilities
{
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance();
    private static final NumberFormat PLAIN_MONEY_FORMATTER = new DecimalFormat( "0.00" );
    private static final DateFormat DATE_FORMAT = SimpleDateFormat.getDateInstance();
    private static final DateFormat SHORT_TIME_FORMAT = SimpleDateFormat.getTimeInstance( DateFormat.SHORT );
    private static final DateFormat DATE_TIME_FORMAT = SimpleDateFormat.getDateTimeInstance();
    private static final DateFormat DAY_MONTH_DATE_FORMAT = new SimpleDateFormat( android.text.format.DateFormat.getBestDateTimePattern( Locale.getDefault(), "Md" ), Locale.getDefault() );
    private static final NumberFormat MILEAGE_FORMATTER = new DecimalFormat( "0.0" );

    private Utilities() { }

    public static void removeFromParent( View view )
    {
        final ViewGroup parent = (ViewGroup)view.getParent();
        if( parent != null )
        {
            parent.removeView( view );
        }
    }

    public static String formatTimeSpan( long timeSpan )
    {
        final long minutes = TimeUnit.MINUTES.convert( timeSpan, TimeUnit.MILLISECONDS );
        return String.valueOf( minutes ) + " minutes";
    }

    public static String formatPastTime( long time )
    {
        final long minutes = TimeUnit.MINUTES.convert( System.currentTimeMillis() - time, TimeUnit.MILLISECONDS );
        return String.valueOf( minutes ) + " minutes ago";
    }

    public static String formatCurrency( BigDecimal currency )
    {
        if( currency != null )
        {
            return CURRENCY_FORMATTER.format( currency );
        }
        return "";
    }

    public static String formatPlainMoney( BigDecimal money )
    {
        if( money != null )
        {
            return PLAIN_MONEY_FORMATTER.format( money );
        }
        return "";
    }

    public static String formatDate( Date date )
    {
        if( date != null )
        {
            return DATE_FORMAT.format( date );
        }
        return "";
    }

    public static String formatShortTime( Date date )
    {
        if( date != null )
        {
            return SHORT_TIME_FORMAT.format( date );
        }
        return "";
    }

    public static String formatDateTime( Date date )
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

    public static String formatDayMonthDate( Date date )
    {
        if( date != null )
        {
            return DAY_MONTH_DATE_FORMAT.format( date );
        }
        return "";
    }

    public static String formatMileage( double mileage )
    {
        return MILEAGE_FORMATTER.format( mileage );
    }
}
