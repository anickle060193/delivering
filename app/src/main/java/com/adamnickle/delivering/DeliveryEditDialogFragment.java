package com.adamnickle.delivering;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;


public class DeliveryEditDialogFragment extends AppCompatDialogFragment
{
    private static final int NAME = 0;
    private static final int TIP = 1;
    private static final int TIP_PAYMENT_METHOD = 2;
    private static final int TOTAL = 3;
    private static final int TOTAL_PAYMENT_METHOD = 4;
    private static final int TIP_INCLUDED = 5;
    private static final int START_TIME = 6;
    private static final int END_TIME = 7;
    private static final int START_MILEAGE = 8;
    private static final int END_MILEAGE = 9;
    private static final int FIELDS = 10;

    @Bind( R.id.delivery_edit_dialog_fragment_name ) EditText mName;
    @Bind( R.id.delivery_edit_dialog_fragment_tip ) EditText mTip;
    @Bind( R.id.delivery_edit_dialog_fragment_tip_payment_method ) Spinner mTipPaymentMethod;
    @Bind( R.id.delivery_edit_dialog_fragment_total ) EditText mTotal;
    @Bind( R.id.delivery_edit_dialog_fragment_total_payment_method ) Spinner mTotalPaymentMethod;
    @Bind( R.id.delivery_edit_dialog_fragment_tip_included ) SwitchCompat mTipIncluded;
    @Bind( R.id.delivery_edit_dialog_fragment_start_time ) EditText mStartTime;
    @Bind( R.id.delivery_edit_dialog_fragment_end_time ) EditText mEndTime;
    @Bind( R.id.delivery_edit_dialog_fragment_start_mileage ) EditText mStartMileage;
    @Bind( R.id.delivery_edit_dialog_fragment_end_mileage ) EditText mEndMileage;

    final String[] mOriginalText = new String[ FIELDS ];

    private Delivery mDelivery;
    private DeliveryEditConfig mConfig;

    public static DeliveryEditDialogFragment newInstance( Delivery delivery, DeliveryEditConfig config )
    {
        final DeliveryEditDialogFragment fragment = new DeliveryEditDialogFragment();
        fragment.mDelivery = delivery;
        fragment.mConfig = config;
        return fragment;
    }

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        final View view = inflater.inflate( R.layout.dialog_fragment_delivery_edit, container, false );
        ButterKnife.bind( this, view );
        init();
        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog( Bundle savedInstanceState )
    {
        final Dialog dialog = super.onCreateDialog( savedInstanceState );
        dialog.requestWindowFeature( Window.FEATURE_NO_TITLE );
        return dialog;
    }

    @Override
    public void onCreateOptionsMenu( Menu menu, MenuInflater inflater )
    {
        inflater.inflate( R.menu.activity_delivery_edit, menu );
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        switch( item.getItemId() )
        {
            case R.id.delivery_edit_activity_action_done:
                done();
                //TODO Listener
                return true;

            case android.R.id.home:
                getFragmentManager().popBackStack();
                //TODO Listener
                return true;

            default:
                return super.onOptionsItemSelected( item );
        }
    }

    private void init()
    {
        final ArrayAdapter<CharSequence> paymentMethodAdapter = ArrayAdapter.createFromResource( getActivity(), R.array.delivery_payment_methods, android.R.layout.simple_spinner_item );
        paymentMethodAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        mTipPaymentMethod.setAdapter( paymentMethodAdapter );
        mTotalPaymentMethod.setAdapter( paymentMethodAdapter );

        // Name
        final String name = mDelivery.getName();
        mName.setText( name );
        mOriginalText[ NAME ] = name;

        // Tip
        final String tipString = Formatter.plainMoney( mDelivery.getTip() );
        mTip.setText( tipString );
        visibility( (View)mTip.getParent(), mConfig.hasTip() );
        mOriginalText[ TIP ] = tipString;

        // Tip Payment Method
        final String tipPaymentMethod = mDelivery.getTipPaymentMethod();
        mTipPaymentMethod.setSelection( paymentMethodAdapter.getPosition( tipPaymentMethod ) );
        visibility( mTipPaymentMethod, mConfig.hasTipPaymentMethod() );
        mOriginalText[ TIP_PAYMENT_METHOD ] = tipPaymentMethod;

        // Total
        final String totalString = Formatter.plainMoney( mDelivery.getTotal() );
        mTotal.setText( totalString );
        visibility( (View)mTotal.getParent(), mConfig.hasTotal() );
        mOriginalText[ TOTAL ] = totalString;

        // Total Payment Method
        final String totalPaymentMethod = mDelivery.getTotalPaymentMethod();
        mTotalPaymentMethod.setSelection( paymentMethodAdapter.getPosition( totalPaymentMethod ) );
        visibility( mTotalPaymentMethod, mConfig.hasTotalPaymentMethod() );
        mOriginalText[ TOTAL_PAYMENT_METHOD ] = totalPaymentMethod;

        // Tip Included
        final String tipIncluded = String.valueOf( mConfig.showTipIncluded() );
        visibility( (View)mTipIncluded.getParent(), mConfig.showTipIncluded() );
        mOriginalText[ TIP_INCLUDED ] = tipIncluded;

        // Start Time
        final Date start = mDelivery.getDeliveryStart();
        final String startString = Formatter.dateTime( start );
        mStartTime.setText( startString );
        mStartTime.setTag( start );
        visibility( (View)mStartTime.getParent(), mConfig.hasStart() );
        mStartTime.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                final Date start = (Date)mStartTime.getTag();
                showDateTimeDialog( start == null ? null : start, mStartTime );
            }
        } );
        mOriginalText[ START_TIME ] = startString;

        // End Time
        final Date end = mDelivery.getDeliveryEnd();
        final String endString = Formatter.dateTime( end );
        mEndTime.setText( endString );
        mEndTime.setTag( end );
        visibility( (View)mEndTime.getParent(), mConfig.hasEnd() );
        mEndTime.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                final Date end = (Date)mEndTime.getTag();
                showDateTimeDialog( end == null ? null : end, mEndTime );
            }
        } );
        mOriginalText[ END_TIME ] = endString;

        // Start Mileage
        final String startMileage = Formatter.mileage( mDelivery.getStartMileage() );
        mStartMileage.setText( startMileage );
        visibility( (View)mStartMileage.getParent(), mConfig.hasStartMileage() );
        mOriginalText[ START_MILEAGE ] = startMileage;

        // End Mileage
        final String endMileage = Formatter.mileage( mDelivery.getEndMileage() );
        mEndMileage.setText( endMileage );
        visibility( (View)mEndMileage.getParent(), mConfig.hasEndMileage() );
        mOriginalText[ END_MILEAGE ] = endMileage;
    }

    private void visibility( View view, boolean visible )
    {
        view.setVisibility( visible ? View.VISIBLE : View.GONE );
    }

    private void showDateTimeDialog( final Date init, final EditText output )
    {
        new DatePickerDialog( getActivity(), new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet( DatePicker view, final int year, final int monthOfYear, final int dayOfMonth )
            {
                new TimePickerDialog( getActivity(), new TimePickerDialog.OnTimeSetListener()
                {
                    @Override
                    public void onTimeSet( TimePicker view, int hourOfDay, int minute )
                    {
                        final Date date = new Date( year - 1900, monthOfYear, dayOfMonth, hourOfDay, minute );
                        output.setText( Formatter.dateTime( date ) );
                        output.setTag( date );
                    }
                }, init.getHours(), init.getMinutes(), android.text.format.DateFormat.is24HourFormat( getActivity() ) ).show();
            }
        }, init.getYear() + 1900, init.getMonth(), init.getDay() ).show();
    }

    private void done()
    {
        View focusView = null;
        boolean cancel = false;

        mEndMileage.setError( null );
        double endMileage = -1.0;
        try
        {
            endMileage = Double.valueOf( mEndMileage.getText().toString() );
        }
        catch( NumberFormatException ex )
        {
            cancel = true;
            focusView = mEndMileage;
            mEndMileage.setError( "Invalid mileage format" );
        }

        mStartMileage.setError( null );
        double startMileage = -1.0;
        try
        {
            startMileage = Double.valueOf( mStartMileage.getText().toString() );
        }
        catch( NumberFormatException ex )
        {
            cancel = true;
            focusView = mStartMileage;
            mStartMileage.setError( "Invalid mileage format" );
        }

        if( endMileage != -1.0 && startMileage != -1.0 && endMileage < startMileage )
        {
            cancel = true;
            focusView = mEndMileage;
            mEndMileage.setError( "End mileage must be greater than start mileage" );
        }

        mEndTime.setError( null );
        Date end = null;
        try
        {
            end = Formatter.getDateTimeFormat().parse( mEndTime.getText().toString() );
        }
        catch( ParseException ex )
        {
            cancel = true;
            focusView = mEndTime;
            mEndTime.setError( "Invalid date/time format" );
        }

        mStartTime.setError( null );
        Date start = null;
        try
        {
            start = Formatter.getDateTimeFormat().parse( mStartTime.getText().toString() );
        }
        catch( ParseException ex )
        {
            cancel = true;
            focusView = mStartTime;
            mStartTime.setError( "Invalid date/time format" );
        }

        if( start != null && end != null && start.after( end ) )
        {
            cancel = true;
            focusView = mEndTime;
            mEndTime.setError( "Delivery end time must be later than start time" );
        }

        String totalPaymentMethod = (String)mTotalPaymentMethod.getSelectedItem();

        BigDecimal total = null;
        final String totalString = mTotal.getText().toString();
        if( Validator.money( totalString ) )
        {
            try
            {
                total = new BigDecimal( totalString );
            }
            catch( NumberFormatException ex ){ }
        }
        if( total == null )
        {
            cancel = true;
            focusView = mTotal;
            mTotal.setError( "Invalid total format" );
        }

        String tipPaymentMethod = (String)mTipPaymentMethod.getSelectedItem();

        BigDecimal tip = null;
        final String tipString = mTip.getText().toString();
        if( Validator.money( tipString ) )
        {
            try
            {
                tip = new BigDecimal( tipString );
            }
            catch( NumberFormatException ex ) { }
        }
        if( tip == null )
        {
            cancel = true;
            focusView = mTip;
            mTip.setError( "Invalid tip format" );
        }

        final boolean tipIncluded = mTipIncluded.isChecked();
        if( tipIncluded && tip != null && total != null )
        {
            total = total.subtract( tip );
            if( total.signum() < 0 )
            {
                cancel = true;
                focusView = mTip;
                mTip.setError( "Tip cannot be greater than the total when included" );
            }
        }

        String name = mName.getText().toString();
        if( TextUtils.isEmpty( name ) )
        {
            cancel = true;
            focusView = mName;
            mName.setError( "Delivery name cannot be empty" );
        }

        if( cancel )
        {
            focusView.requestFocus();
            return;
        }

        mDelivery.setName( name );
        mDelivery.setTip( tip );
        mDelivery.setTipPaymentMethod( tipPaymentMethod );
        mDelivery.setTotal( total );
        mDelivery.setTotalPaymentMethod( totalPaymentMethod );
        mDelivery.setDeliveryStart( start );
        mDelivery.setDeliveryEnd( end );
        mDelivery.setStartMileage( startMileage );
        mDelivery.setEndMileage( endMileage );
        mDelivery.saveEventually();

        getFragmentManager().popBackStack();
        //TODO Listener
    }
}
