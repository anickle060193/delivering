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
                .setNeutralButton( "Cancel", null )
                .show();
    }

    public interface ShiftStatusListener
    {
        void onShiftClockIn();
        void onShiftClockOut();
    }

    public static void clockInOut( Context context, Shift shift, final ShiftStatusListener listener )
    {
        if( shift.isCompleted() )
        {
            Delivering.toast( "Already clocked out" );
        }
        else if( shift.isInProgress() )
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
}
