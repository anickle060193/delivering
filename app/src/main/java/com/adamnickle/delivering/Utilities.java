package com.adamnickle.delivering;

import android.view.View;
import android.view.ViewGroup;


public final class Utilities
{
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
