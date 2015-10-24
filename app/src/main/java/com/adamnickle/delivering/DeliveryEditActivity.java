package com.adamnickle.delivering;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
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

public class DeliveryEditActivity extends AppCompatActivity
{
    public static final String EXTRA_DELIVERY_EDIT_CONFIG = DeliveryEditActivity.class.getName() + ".extra.delivery_edit_config";

    private DeliveryEditConfig mConfig;

    @Bind( R.id.delivery_edit_activity_name ) EditText mName;
    @Bind( R.id.delivery_edit_activity_tip ) EditText mTip;
    @Bind( R.id.delivery_edit_activity_tip_payment_method ) Spinner mTipPaymentMethod;
    @Bind( R.id.delivery_edit_activity_total ) EditText mTotal;
    @Bind( R.id.delivery_edit_activity_total_payment_method ) Spinner mTotalPaymentMethod;
    @Bind( R.id.delivery_edit_activity_tip_included ) SwitchCompat mTipIncluded;
    @Bind( R.id.delivery_edit_activity_start_time ) EditText mStartTime;
    @Bind( R.id.delivery_edit_activity_end_time ) EditText mEndTime;
    @Bind( R.id.delivery_edit_activity_start_mileage ) EditText mStartMileage;
    @Bind( R.id.delivery_edit_activity_end_mileage ) EditText mEndMileage;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_delivery_edit );
        ButterKnife.bind( this );

        setResult( Activity.RESULT_CANCELED );

        setSupportActionBar( (Toolbar)findViewById( R.id.delivery_edit_activity_toolbar ) );
        final ActionBar actionBar = getSupportActionBar();
        if( actionBar != null )
        {
            actionBar.setDisplayHomeAsUpEnabled( true );
        }

        getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN );

        if( BuildConfig.DEBUG )
        {
            getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
        }

        final ArrayAdapter<CharSequence> paymentMethodAdapter = ArrayAdapter.createFromResource( this, R.array.delivery_payment_methods, android.R.layout.simple_spinner_item );
        paymentMethodAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        mTipPaymentMethod.setAdapter( paymentMethodAdapter );
        mTotalPaymentMethod.setAdapter( paymentMethodAdapter );

        mConfig = getIntent().getParcelableExtra( EXTRA_DELIVERY_EDIT_CONFIG );
        if( mConfig != null )
        {
            setTitle( mConfig.activityTitle() );

            mName.setText( mConfig.name() );
            visibility( (View)mName.getParent(), mConfig.hasName() );

            mTip.setText( Utilities.formatPlainMoney( mConfig.tip() ) );
            visibility( (View)mTip.getParent(), mConfig.hasTip() );

            mTipPaymentMethod.setSelection( paymentMethodAdapter.getPosition( mConfig.tipPaymentMethod() ) );
            visibility( mTipPaymentMethod, mConfig.hasTipPaymentMethod() );

            mTotal.setText( Utilities.formatPlainMoney( mConfig.total() ) );
            visibility( (View)mTotal.getParent(), mConfig.hasTotal() );

            mTotalPaymentMethod.setSelection( paymentMethodAdapter.getPosition( mConfig.totalPaymentMethod() ) );
            visibility( mTotalPaymentMethod, mConfig.hasTotalPaymentMethod() );

            visibility( (View)mTipIncluded.getParent(), mConfig.showTipIncluded() );

            final Date start = mConfig.start();
            mStartTime.setText( Utilities.formatDateTime( start ) );
            mStartTime.setTag( start );
            visibility( (View)mStartTime.getParent(), mConfig.hasStart() );

            final Date end = mConfig.end();
            mEndTime.setText( Utilities.formatDateTime( end ) );
            mEndTime.setTag( end );
            visibility( (View)mEndTime.getParent(), mConfig.hasEnd() );

            mStartMileage.setText( Utilities.formatMileage( mConfig.startMileage() ) );
            visibility( (View)mStartMileage.getParent(), mConfig.hasStartMileage() );

            mEndMileage.setText( Utilities.formatMileage( mConfig.endMileage() ) );
            visibility( (View)mEndMileage.getParent(), mConfig.hasEndMileage() );
        }

        mStartTime.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                final Date start = (Date)mStartTime.getTag();
                showDateTimeDialog( start == null ? null : start, mStartTime );
            }
        } );

        mEndTime.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                final Date end = (Date)mEndTime.getTag();
                showDateTimeDialog( end == null ? null : end, mEndTime );
            }
        } );
    }

    private void visibility( View view, boolean visible )
    {
        view.setVisibility( visible ? View.VISIBLE : View.GONE );
    }

    private void showDateTimeDialog( final Date init, final EditText output )
    {
        new DatePickerDialog( this, new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet( DatePicker view, final int year, final int monthOfYear, final int dayOfMonth )
            {
                new TimePickerDialog( DeliveryEditActivity.this, new TimePickerDialog.OnTimeSetListener()
                {
                    @Override
                    public void onTimeSet( TimePicker view, int hourOfDay, int minute )
                    {
                        final Date date = new Date( year - 1900, monthOfYear, dayOfMonth, hourOfDay, minute );
                        output.setText( Utilities.formatDateTime( date ) );
                        output.setTag( date );
                    }
                }, init.getHours(), init.getMinutes(), android.text.format.DateFormat.is24HourFormat( DeliveryEditActivity.this ) ).show();
            }
        }, init.getYear() + 1900, init.getMonth(), init.getDay() ).show();
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        getMenuInflater().inflate( R.menu.activity_delivery_edit, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        switch( item.getItemId() )
        {
            case R.id.delivery_edit_activity_action_done:
                done();
                return true;

            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected( item );
        }
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
            end = Utilities.getDateTimeFormat().parse( mEndTime.getText().toString() );
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
            start = Utilities.getDateTimeFormat().parse( mStartTime.getText().toString() );
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

        final DeliveryEditConfig config = DeliveryEditConfig.copy( mConfig )
                .name( name )
                .tip( tip )
                .tipPaymentMethod( tipPaymentMethod )
                .total( total )
                .totalPaymentMethod( totalPaymentMethod )
                .start( start )
                .end( end )
                .startMileage( startMileage )
                .endMileage( endMileage )
                .build();

        final Intent intent = new Intent()
                .putExtra( EXTRA_DELIVERY_EDIT_CONFIG, config );
        setResult( Activity.RESULT_OK, intent );
        finish();
    }
}
