package com.adamnickle.delivering;

import android.content.Context;
import android.support.v7.app.AlertDialog;


public abstract class Dialogs
{
    private Dialogs() { }

    public static AlertDialog showLoading( Context context )
    {
        return new AlertDialog.Builder( context )
                .setView( R.layout.loading_dialog_layout )
                .setCancelable( false )
                .show();
    }
}
