package com.adamnickle.delivering;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
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

    public interface DeliveryPaymentSetListener
    {
        void onDeliveryPaymentSet( BigDecimal total, String totalPaymentMethod, BigDecimal tip, String tipPaymentMethod );
    }

    private static final Pattern PLAIN_MONEY_PATTERN = Pattern.compile( "^[1-9]\\d*(?:\\.\\d{2})?$" );
    private static final NumberFormat PLAIN_MONEY_FORMATTER = new DecimalFormat( "0.00" );

    public static void setPayment( Context context, Delivery prefillDelivery, final DeliveryPaymentSetListener listener )
    {
        final AlertDialog dialog = new AlertDialog.Builder( context )
                .setView( R.layout.delivery_payment_dialog_layout )
                .setPositiveButton( "Done", null )
                .show();

        final EditText tipEditText = (EditText)dialog.findViewById( R.id.delivery_payment_dialog_tip );
        final EditText totalEditText = (EditText)dialog.findViewById( R.id.delivery_payment_dialog_total );

        final Spinner tipPaymentMethodSpinner = (Spinner)dialog.findViewById( R.id.delivery_payment_dialog_tip_payment_method );
        final Spinner totalPaymentMethodSpinner = (Spinner)dialog.findViewById( R.id.delivery_payment_dialog_total_payment_method );

        final ArrayAdapter<CharSequence> paymentMethodAdapter = ArrayAdapter.createFromResource( context, R.array.delivery_payment_methods, android.R.layout.simple_spinner_item );
        paymentMethodAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );

        totalPaymentMethodSpinner.setAdapter( paymentMethodAdapter );
        tipPaymentMethodSpinner.setAdapter( paymentMethodAdapter );

        final SwitchCompat tipIncludedSwitch = (SwitchCompat)dialog.findViewById( R.id.delivery_payment_dialog_tip_included );

        final BigDecimal initialTip = prefillDelivery.getTip();
        if( initialTip != null )
        {
            tipEditText.setText( PLAIN_MONEY_FORMATTER.format( initialTip ) );
        }

        final BigDecimal initialTotal = prefillDelivery.getTotal();
        if( initialTotal != null )
        {
            totalEditText.setText( PLAIN_MONEY_FORMATTER.format( initialTotal ) );
        }

        dialog.getButton( DialogInterface.BUTTON_POSITIVE ).setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                View focusView = null;
                boolean hasError = false;

                final String totalString = totalEditText.getText().toString();
                if( !PLAIN_MONEY_PATTERN.matcher( totalString ).matches() )
                {
                    totalEditText.setError( "Invalid total format (e.g. 12.73)" );
                    focusView = totalEditText;
                    hasError = true;
                }

                final String tipString = tipEditText.getText().toString();
                if( !PLAIN_MONEY_PATTERN.matcher( tipString ).matches() )
                {
                    tipEditText.setError( "Invalid tip format (e.g. 4.56)" );
                    focusView = tipEditText;
                    hasError = true;
                }

                if( hasError )
                {
                    focusView.requestFocus();
                    return;
                }

                final BigDecimal tip = new BigDecimal( tipString );
                final String tipPaymentMethod = (String)tipPaymentMethodSpinner.getSelectedItem();

                BigDecimal total = new BigDecimal( totalString );
                final String totalPaymentMethod = (String)totalPaymentMethodSpinner.getSelectedItem();

                if( tipIncludedSwitch.isChecked() )
                {
                    total = total.subtract( tip );
                    if( total.signum() == -1 )
                    {
                        totalEditText.setError( "Total must be greater than tip." );
                        totalEditText.requestFocus();
                        return;
                    }
                }

                listener.onDeliveryPaymentSet( total, totalPaymentMethod, tip, tipPaymentMethod );
                dialog.dismiss();
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
