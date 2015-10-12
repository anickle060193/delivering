package com.adamnickle.delivering;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.parse.Parse;


public class DeliveringApplication extends Application
{
    private static Context sContext;

    @Override
    public void onCreate()
    {
        super.onCreate();

        sContext = this;

        Parse.initialize( this, getString( R.string.applicationId ), getString( R.string.clientKey ) );
    }

    public static void toast( String message )
    {
        Toast.makeText( sContext, message, Toast.LENGTH_LONG ).show();
    }
}
