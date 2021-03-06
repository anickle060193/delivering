package com.adamnickle.delivering;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;


public class Delivering extends Application
{
    private static final String TAG = "Delivering";

    private static Context sContext;

    @Override
    public void onCreate()
    {
        super.onCreate();

        sContext = this;

        ParseUser.registerSubclass( Deliverer.class );
        ParseObject.registerSubclass( Delivery.class );
        ParseObject.registerSubclass( Shift.class );

        Parse.enableLocalDatastore( this );

        Parse.initialize( this, getString( R.string.applicationId ), getString( R.string.clientKey ) );
    }

    public static void toast( String message )
    {
        Toast.makeText( sContext, message, Toast.LENGTH_LONG ).show();
    }

    public static void log( String message )
    {
        if( BuildConfig.DEBUG )
        {
            Log.d( TAG, message );
        }
    }

    public static void log( String message, Exception ex )
    {
        if( BuildConfig.DEBUG )
        {
            Log.e( TAG, message, ex );
        }
    }

    public static void oops( ParseException ex )
    {
        final int errorCode = ex.getCode();
        final String errorMessage = ex.getMessage();
        final String message = String.format( "Something went wrong! Try again. Error %d: %s", errorCode, errorMessage );
        Delivering.toast( message );
    }
}
