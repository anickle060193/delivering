package com.adamnickle.delivering;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;


public abstract class ShiftDialogs
{
    private ShiftDialogs() { }

    public interface ShiftCreatorListener
    {
        void OnShiftCreated( boolean clockIn );
    }

    public static void create( Context context, final ShiftCreatorListener listener )
    {
        if( Shift.getCurrentShift() != null )
        {
            new AlertDialog.Builder( context )
                    .setMessage( "You cannot create a new shift while still clocked-in to another." )
                    .setPositiveButton( "OK", null )
                    .show();
        }
        else
        {
            new AlertDialog.Builder( context )
                    .setMessage( "Automatically clock-in to new shift?" )
                    .setPositiveButton( "Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick( DialogInterface dialog, int which )
                        {
                            listener.OnShiftCreated( true );
                        }
                    } )
                    .setNegativeButton( "No", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick( DialogInterface dialog, int which )
                        {
                            listener.OnShiftCreated( false );
                        }
                    } )
                    .show();
        }
    }

    public interface ShiftClockInListener
    {
        void onShiftClockIn();
    }

    public static void clockIn( Context context, Shift shift, final ShiftClockInListener listener )
    {
        if( shift.isCompleted() )
        {
            Delivering.toast( "Already clocked out" );
        }
        else if( shift.isInProgress() )
        {
            Delivering.toast( "Already clocked-in" );
        }
        else
        {
            new AlertDialog.Builder( context )
                    .setMessage( "Clock in?" )
                    .setPositiveButton( "Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick( DialogInterface dialog, int which )
                        {
                            listener.onShiftClockIn();
                        }
                    } )
                    .setNegativeButton( "No", null )
                    .show();
        }
    }

    public interface ShiftClockOutListener
    {
        void onShiftClockOut();
    }

    public static void clockOut( Context context, Shift shift, final ShiftClockOutListener listener )
    {
        if( shift.isCompleted() )
        {
            Delivering.toast( "Already clocked out" );
        }
        else
        {
            new AlertDialog.Builder( context )
                    .setMessage( "Clock out?" )
                    .setPositiveButton( "Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick( DialogInterface dialog, int which )
                        {
                            listener.onShiftClockOut();
                        }
                    } )
                    .setNegativeButton( "No", null )
                    .show();
        }
    }
}
