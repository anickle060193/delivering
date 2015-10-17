package com.adamnickle.delivering;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.regex.Pattern;


public abstract class DeliveryDialogs
{
    private DeliveryDialogs() { }

    public interface DeliveryCreatorListener
    {
        void onDeliveryCreated( String deliveryName );
    }

    public static void create( final Context context, final DeliveryCreatorListener listener )
    {
        final AlertDialog dialog = new AlertDialog.Builder( context )
                .setView( R.layout.delivery_creator_dialog_layout )
                .setPositiveButton( "Create Delivery", null )
                .setNegativeButton( "Cancel", null )
                .show();
        final EditText distanceEditText = (EditText)dialog.findViewById( R.id.delivery_creator_distance );
        distanceEditText.setOnEditorActionListener( new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction( TextView v, int actionId, KeyEvent event )
            {
                if( actionId == EditorInfo.IME_ACTION_DONE )
                {
                    // This is gross
                    dialog.getButton( DialogInterface.BUTTON_POSITIVE ).callOnClick();
                    return true;
                }
                return false;
            }
        } );
        final EditText deliveryNameEditText = (EditText)dialog.findViewById( R.id.delivery_creator_name );
        dialog.getButton( DialogInterface.BUTTON_POSITIVE ).setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                final String deliveryName = deliveryNameEditText.getText().toString();
                listener.onDeliveryCreated( deliveryName );
                dialog.dismiss();
            }
        } );
    }

    public interface DeliveryTipSetListener
    {
        void onDeliveryTipSet( BigDecimal tip );
    }

    private static final Pattern TIP_PATTERN = Pattern.compile( "^[1-9]\\d*(?:\\.\\d{2})?$" );

    public static void setTip( Context context, BigDecimal initialTip, final DeliveryTipSetListener listener )
    {
        final AlertDialog dialog = new AlertDialog.Builder( context )
                .setView( R.layout.delivery_tip_dialog_layout )
                .setPositiveButton( "Tip", null )
                .show();
        final EditText tipEditText = (EditText)dialog.findViewById( R.id.delivery_tip_dialog_tip );
        if( initialTip != null )
        {
            tipEditText.setText( initialTip.toString() );
        }
        tipEditText.setOnEditorActionListener( new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction( TextView v, int actionId, KeyEvent event )
            {
                if( actionId == EditorInfo.IME_ACTION_DONE )
                {
                    // This is gross
                    dialog.getButton( DialogInterface.BUTTON_POSITIVE ).callOnClick();
                    return true;
                }
                return false;
            }
        } );
        dialog.getButton( DialogInterface.BUTTON_POSITIVE ).setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                final String tipString = tipEditText.getText().toString();
                if( TIP_PATTERN.matcher( tipString ).matches() )
                {
                    listener.onDeliveryTipSet( new BigDecimal( tipString ) );
                    dialog.dismiss();
                }
                else
                {
                    tipEditText.setError( "Invalid tip format (e.g. 4.56)" );
                    tipEditText.requestFocus();
                }
            }
        } );
    }

    public interface DeliveryCompleteListener
    {
        void onDeliveryComplete();
    }

    public static void completeDelivery( Context context, final DeliveryCompleteListener listener )
    {
        new AlertDialog.Builder( context )
                .setMessage( "Are you sure you want to complete this delivery?" )
                .setPositiveButton( "Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick( DialogInterface dialog, int which )
                    {
                        listener.onDeliveryComplete();
                    }
                } )
                .setNegativeButton( "No", null )
                .show();
    }

    public interface DeliveryStartListener
    {
        void onDeliveryStarted();
    }

    public static void startDelivery( Context context, final DeliveryStartListener listener )
    {
        new AlertDialog.Builder( context )
                .setMessage( "Are you sure you want to start this delivery?" )
                .setPositiveButton( "Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick( DialogInterface dialog, int which )
                    {
                        listener.onDeliveryStarted();
                    }
                } )
                .setNegativeButton( "No", null )
                .show();
    }
}
