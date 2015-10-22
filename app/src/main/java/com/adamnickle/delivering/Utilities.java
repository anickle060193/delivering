package com.adamnickle.delivering;

import android.view.View;
import android.view.ViewGroup;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public final class Utilities
{
    public static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance();
    public static final NumberFormat PLAIN_MONEY_FORMATTER = new DecimalFormat( "0.00" );
    public static final DateFormat DATE_FORMAT = SimpleDateFormat.getDateInstance();
    public static final DateFormat SHORT_TIME_FORMAT = SimpleDateFormat.getTimeInstance( DateFormat.SHORT );
    public static final DateFormat DAY_MONTH_DATE_FORMAT = new SimpleDateFormat( android.text.format.DateFormat.getBestDateTimePattern( Locale.getDefault(), "Md" ), Locale.getDefault() );
    public static final NumberFormat MILEAGE_FORMATTER = new DecimalFormat( "0.0" );

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
}
