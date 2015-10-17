package com.adamnickle.delivering;

import android.view.View;
import android.view.ViewGroup;

import java.text.NumberFormat;


public abstract class Utilities
{
    public static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance();

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
