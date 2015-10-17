package com.adamnickle.delivering;

import android.view.View;
import android.view.ViewGroup;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;


public abstract class Utilities
{
    public static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance();
    public static final DateFormat DATE_FORMAT = SimpleDateFormat.getDateInstance();
    public static final DateFormat SHORT_TIME_FORMAT = SimpleDateFormat.getTimeInstance( DateFormat.SHORT );
    public static final DateFormat DAY_MONTH_DATE_FORMAT = new SimpleDateFormat( android.text.format.DateFormat.getBestDateTimePattern( Locale.getDefault(), "Md" ), Locale.getDefault() );

    private Utilities() { }

    public static void removeFromParent( View view )
    {
        final ViewGroup parent = (ViewGroup)view.getParent();
        if( parent != null )
        {
            parent.removeView( view );
        }
    }
}
