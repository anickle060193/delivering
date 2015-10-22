package com.adamnickle.delivering;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.PreferenceManager;
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
import java.util.regex.Pattern;


public final class DeliveryDialogs
{
    private static final String PREF_LAST_MILEAGE = BuildConfig.APPLICATION_ID + ".preference.last_mileage";

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
        final EditText deliveryNameEditText = (EditText)dialog.findViewById( R.id.delivery_creator_name );
        deliveryNameEditText.setOnEditorActionListener( new TextView.OnEditorActionListener()
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
            tipEditText.setText( Utilities.formatPlainMoney( initialTip ) );
        }

        final BigDecimal initialTotal = prefillDelivery.getTotal();
        if( initialTotal != null )
        {
            totalEditText.setText( Utilities.formatPlainMoney( initialTotal ) );
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
        void onDeliveryComplete( double endMileage );
    }

    private static double getLastEnteredMileage( Context context )
    {
        return PreferenceManager.getDefaultSharedPreferences( context )
                .getFloat( PREF_LAST_MILEAGE, 0.0f );
    }

    private static void setLastEnteredMileage( Context context, double mileage )
    {
        PreferenceManager.getDefaultSharedPreferences( context )
                .edit()
                .putFloat( PREF_LAST_MILEAGE, (float)mileage )
                .apply();
    }

    public static void completeDelivery( final Context context, final Delivery delivery, final DeliveryCompleteListener listener )
    {
        final AlertDialog dialog = new AlertDialog.Builder( context )
                .setView( R.layout.delivery_completed_dialog_layout )
                .setPositiveButton( "Complete", null )
                .show();

        final EditText startMileageEditText = (EditText)dialog.findViewById( R.id.delivery_completed_dialog_start_mileage );
        startMileageEditText.setText( Utilities.formatMileage( delivery.getStartMileage() ) );

        final EditText endMileageEditText = (EditText)dialog.findViewById( R.id.delivery_completed_dialog_end_mileage );
        endMileageEditText.setOnEditorActionListener( new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction( TextView v, int actionId, KeyEvent event )
            {
                if( actionId == EditorInfo.IME_ACTION_DONE )
                {
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
                final String endMileageString = endMileageEditText.getText().toString();
                try
                {
                    final double endMileage = Double.valueOf( endMileageString );
                    if( endMileage < 0 )
                    {
                        endMileageEditText.setError( "Mileage must be non-negative" );
                        endMileageEditText.requestFocus();
                    }
                    else if( endMileage < delivery.getStartMileage() )
                    {
                        endMileageEditText.setError( "End mileage must be greater than start mileage" );
                        endMileageEditText.requestFocus();
                    }
                    else
                    {
                        DeliveryDialogs.setLastEnteredMileage( context, endMileage );
                        dialog.dismiss();

                        listener.onDeliveryComplete( endMileage );
                    }
                }
                catch( NumberFormatException ex )
                {
                    endMileageEditText.setError( "Invalid mileage format" );
                    endMileageEditText.requestFocus();
                }
            }
        } );
    }

    public interface DeliveryStartListener
    {
        void onDeliveryStarted( double startMileage );
    }

    public static void startDelivery( Context context, final DeliveryStartListener listener )
    {
        final AlertDialog dialog = new AlertDialog.Builder( context )
                .setView( R.layout.delivery_in_progress_dialog_layout )
                .setPositiveButton( "Start", null )
                .show();

        final EditText startMileageEditText = (EditText)dialog.findViewById( R.id.delivery_in_progress_dialog_start_mileage );
        startMileageEditText.setOnEditorActionListener( new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction( TextView v, int actionId, KeyEvent event )
            {
                if( actionId == EditorInfo.IME_ACTION_DONE )
                {
                    dialog.getButton( DialogInterface.BUTTON_POSITIVE ).callOnClick();
                    return true;
                }
                return false;
            }
        } );

        final double lastEnteredMileage = DeliveryDialogs.getLastEnteredMileage( context );
        startMileageEditText.setText( Utilities.formatMileage( lastEnteredMileage ) );

        dialog.getButton( DialogInterface.BUTTON_POSITIVE ).setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                final String startMileageString = startMileageEditText.getText().toString();
                try
                {
                    final double startMileage = Double.valueOf( startMileageString );
                    if( startMileage < 0 )
                    {
                        startMileageEditText.setError( "Mileage must be non-negative" );
                        startMileageEditText.requestFocus();
                    }
                    else
                    {
                        listener.onDeliveryStarted( startMileage );
                        dialog.dismiss();
                    }
                }
                catch( NumberFormatException ex )
                {
                    startMileageEditText.setError( "Invalid mileage format" );
                    startMileageEditText.requestFocus();
                }
            }
        } );
    }
}
