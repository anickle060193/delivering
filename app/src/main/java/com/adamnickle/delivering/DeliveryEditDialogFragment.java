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
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;


public class DeliveryEditDialogFragment extends AppCompatDialogFragment
{
    @Bind( R.id.delivery_edit_dialog_fragment_name ) EditText mNameView;
    @Bind( R.id.delivery_edit_dialog_fragment_tip ) EditText mTipView;
    @Bind( R.id.delivery_edit_dialog_fragment_tip_payment_method ) Spinner mTipPaymentMethodView;
    @Bind( R.id.delivery_edit_dialog_fragment_total ) EditText mTotalView;
    @Bind( R.id.delivery_edit_dialog_fragment_total_payment_method ) Spinner mTotalPaymentMethodView;
    @Bind( R.id.delivery_edit_dialog_fragment_tip_included ) SwitchCompat mTipIncludedView;
    @Bind( R.id.delivery_edit_dialog_fragment_start_time ) EditText mStartTimeView;
    @Bind( R.id.delivery_edit_dialog_fragment_end_time ) EditText mEndTimeView;
    @Bind( R.id.delivery_edit_dialog_fragment_start_mileage ) EditText mStartMileageView;
    @Bind( R.id.delivery_edit_dialog_fragment_end_mileage ) EditText mEndMileageView;

    private Delivery mDelivery;
    private DeliveryEditConfig mConfig;

    private String mName;
    private BigDecimal mTip;
    private String mTipPaymentMethod;
    private BigDecimal mTotal;
    private String mTotalPaymentMethod;
    private boolean mTipIncluded;
    private Date mStartTime;
    private Date mEndTime;
    private double mStartMileage;
    private double mEndMileage;

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
        mTipPaymentMethodView.setAdapter( paymentMethodAdapter );
        mTotalPaymentMethodView.setAdapter( paymentMethodAdapter );

        // Name
        mName = mDelivery.getName();
        mNameView.setText( mName );

        // Tip
        mTip = mDelivery.getTip();
        mTipView.setText( Formatter.plainMoney( mTip ) );
        visibility( (View)mTipView.getParent(), mConfig.hasTip() );

        // Tip Payment Method
        mTipPaymentMethod = mDelivery.getTipPaymentMethod();
        mTipPaymentMethodView.setSelection( paymentMethodAdapter.getPosition( mTipPaymentMethod ) );
        visibility( mTipPaymentMethodView, mConfig.hasTipPaymentMethod() );

        // Total
        mTotal = mDelivery.getTotal();
        mTotalView.setText( Formatter.plainMoney( mTotal ) );
        visibility( (View)mTotalView.getParent(), mConfig.hasTotal() );

        // Total Payment Method
        mTotalPaymentMethod = mDelivery.getTotalPaymentMethod();
        mTotalPaymentMethodView.setSelection( paymentMethodAdapter.getPosition( mTotalPaymentMethod ) );
        visibility( mTotalPaymentMethodView, mConfig.hasTotalPaymentMethod() );

        // Tip Included
        mTipIncluded = false;
        visibility( (View)mTipIncludedView.getParent(), mConfig.showTipIncluded() );

        // Start Time
        mStartTime = mDelivery.getDeliveryStart();
        mStartTimeView.setText( Formatter.dateTime( mStartTime ) );
        mStartTimeView.setTag( mStartTime );
        visibility( (View)mStartTimeView.getParent(), mConfig.hasStart() );
        mStartTimeView.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                final Date start = (Date)mStartTimeView.getTag();
                showDateTimeDialog( start == null ? null : start, mStartTimeView );
            }
        } );

        // End Time
        mEndTime = mDelivery.getDeliveryEnd();
        mEndTimeView.setText( Formatter.dateTime( mEndTime ) );
        mEndTimeView.setTag( mEndTime );
        visibility( (View)mEndTimeView.getParent(), mConfig.hasEnd() );
        mEndTimeView.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                final Date end = (Date)mEndTimeView.getTag();
                showDateTimeDialog( end == null ? null : end, mEndTimeView );
            }
        } );

        // Start Mileage
        mStartMileage = mDelivery.getStartMileage();
        mStartMileageView.setText( Formatter.mileage( mStartMileage ) );
        visibility( (View)mStartMileageView.getParent(), mConfig.hasStartMileage() );

        // End Mileage
        mEndMileage = mDelivery.getEndMileage();
        mEndMileageView.setText( Formatter.mileage( mEndMileage ) );
        visibility( (View)mEndMileageView.getParent(), mConfig.hasEndMileage() );
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

    private void clearErrors()
    {
        mEndMileageView.setError( null );
        mStartMileageView.setError( null );
        mEndTimeView.setError( null );
        mStartTimeView.setError( null );
        mTotalView.setError( null );
        mTipView.setError( null );
        mNameView.setError( null );
    }

    private void done()
    {
        clearErrors();

        View focusView = null;
        boolean cancel = false;

        // End Mileage
        boolean endMileageModified = false;
        double endMileage = -1.0;
        if( mConfig.hasEndMileage() )
        {
            final String endMileageString = mEndMileageView.getText().toString();
            if( !TextUtils.isEmpty( endMileageString ) && !mDelivery.hasEndMileage() )
            {
                try
                {
                    endMileage = Double.valueOf( endMileageString );
                    endMileageModified = endMileage != mEndMileage;
                }
                catch( NumberFormatException ex )
                {
                    cancel = true;
                    focusView = mEndMileageView;
                    mEndMileageView.setError( "Invalid mileage format" );
                }
            }
        }

        // Start Mileage
        boolean startMileageModified = false;
        double startMileage = -1.0;
        if( mConfig.hasStartMileage() )
        {
            final String startMileageString = mStartMileageView.getText().toString();
            if( !TextUtils.isEmpty( startMileageString ) && mDelivery.hasStartMileage() )
            {
                try
                {
                    startMileage = Double.valueOf( startMileageString );
                    startMileageModified = startMileage != mStartMileage;
                }
                catch( NumberFormatException ex )
                {
                    cancel = true;
                    focusView = mStartMileageView;
                    mStartMileageView.setError( "Invalid mileage format" );
                }
            }
        }

        // Start vs End Mileage Check
        if( endMileage != -1.0 )
        {
            if( mStartMileage == -1.0 )
            {
                cancel = true;
                focusView = mStartMileageView;
                mStartMileageView.setError( "Must specify start mileage with end mileage" );
            }
            else if( endMileage < startMileage )
            {
                cancel = true;
                focusView = mEndMileageView;
                mEndMileageView.setError( "End mileage cannot be less than start mileage" );
            }
        }

        // End Time
        boolean endTimeModified = false;
        Date endTime = null;
        if( mConfig.hasEnd() )
        {
            endTime = (Date)mEndTimeView.getTag();
            if( endTime != null )
            {
                endTimeModified = !endTime.equals( mEndTime );
            }
            else
            {
                cancel = true;
                focusView = mEndTimeView;
                mEndTimeView.setError( "Invalid end date/time" );
            }
        }

        // Start Time
        boolean startTimeModified = false;
        Date startTime = null;
        if( mConfig.hasStart() )
        {
            startTime = (Date)mStartTimeView.getTag();
            if( startTime != null )
            {
                startTimeModified = !startTime.equals( mStartTime );
            }
            else
            {
                cancel = true;
                focusView = mStartTimeView;
                mStartTimeView.setError( "Invalid start date/time" );
            }
        }

        // Start vs End Time Check
        if( endTime != null )
        {
            if( startTime == null )
            {
                cancel = true;
                focusView = mStartTimeView;
                mStartTimeView.setError( "Must specify start time with end time" );
            }
            else if( startTime.after( endTime ) )
            {
                cancel = true;
                focusView = mEndTimeView;
                mEndTimeView.setError( "Delivery end time must be later than start time" );
            }
        }

        // Total Payment Method
        String totalPaymentMethod = (String)mTotalPaymentMethodView.getSelectedItem();
        boolean totalPaymentMethodModified = !totalPaymentMethod.equals( mTotalPaymentMethod );

        // Total
        boolean totalModified = false;
        BigDecimal total = null;
        if( mConfig.hasTotal() )
        {
            final String totalString = mTotalView.getText().toString();
            if( Validator.money( totalString ) )
            {
                try
                {
                    total = new BigDecimal( totalString );
                }
                catch( NumberFormatException ex ) { }
            }
            if( total != null )
            {
                totalModified = !total.equals( mTotal );
            }
            else
            {
                cancel = true;
                focusView = mTotalView;
                mTotalView.setError( "Invalid total format" );
            }
        }

        // Tip Payment Method
        String tipPaymentMethod = (String)mTipPaymentMethodView.getSelectedItem();
        boolean tipPaymentMethodModified = !tipPaymentMethod.equals( mTipPaymentMethod );

        // Tip
        boolean tipModified = false;
        BigDecimal tip = null;
        if( mConfig.hasTip() )
        {
            final String tipString = mTipView.getText().toString();
            if( Validator.money( tipString ) )
            {
                try
                {
                    tip = new BigDecimal( tipString );
                }
                catch( NumberFormatException ex ) { }
            }
            if( tip != null )
            {
                tipModified = !tip.equals( mTip );
            }
            else
            {
                cancel = true;
                focusView = mTipView;
                mTipView.setError( "Invalid tip format" );
            }
        }

        // Tip Included
        final boolean tipIncluded = mTipIncludedView.isChecked();
        if( tipIncluded && tip != null && total != null )
        {
            total = total.subtract( tip );
            if( total.signum() < 0 )
            {
                cancel = true;
                focusView = mTipView;
                mTipView.setError( "Tip cannot be greater than the total when included" );
            }
        }

        // Name
        boolean nameModified = false;
        String name = null;
        if( mConfig.hasName() )
        {
            name = mNameView.getText().toString();
            nameModified = !name.equals( mName );
            if( TextUtils.isEmpty( name ) )
            {
                cancel = true;
                focusView = mNameView;
                mNameView.setError( "Delivery name cannot be empty" );
            }
        }

        // Cancel
        if( cancel )
        {
            focusView.requestFocus();
            return;
        }

        // Update Delivery
        boolean modified = false;
        if( nameModified )
        {
            mDelivery.setName( name );
            modified = true;
        }
        if( tipModified )
        {
            mDelivery.setTip( tip );
            modified = true;
        }
        if( tipPaymentMethodModified )
        {
            mDelivery.setTipPaymentMethod( tipPaymentMethod );
            modified = true;
        }
        if( totalModified )
        {
            mDelivery.setTotal( total );
            modified = true;
        }
        if( totalPaymentMethodModified )
        {
            mDelivery.setTotalPaymentMethod( totalPaymentMethod );
            modified = true;
        }
        if( startTimeModified )
        {
            mDelivery.setDeliveryStart( startTime );
            modified = true;
        }
        if( endTimeModified )
        {
            mDelivery.setDeliveryEnd( endTime );
            modified = true;
        }
        if( startMileageModified )
        {
            mDelivery.setStartMileage( startMileage );
            modified = true;
        }
        if( endMileageModified )
        {
            mDelivery.setEndMileage( endMileage );
            modified = true;
        }

        // Save Delivery
        if( modified )
        {
            mDelivery.saveEventually();
        }

        // Finish
        getFragmentManager().popBackStack();
        //TODO Listener
    }
}
