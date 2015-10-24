package com.adamnickle.delivering;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;

import java.util.Collections;

import butterknife.Bind;
import butterknife.ButterKnife;


public class DeliveryActivity extends AppCompatActivity
{
    public static final String EXTRA_DELIVERY_OBJECT_ID = DeliveryActivity.class.getName() + ".extra.delivery_object_id";

    public static final int RESULT_EDITED = Activity.RESULT_FIRST_USER;
    public static final int RESULT_DELETED = RESULT_EDITED + 1;

    public static final int REQUEST_EDIT_DELIVERY = 1001;

    @Bind( R.id.delivery_activity_name ) TextView mName;
    @Bind( R.id.delivery_activity_tip ) TextView mTip;
    @Bind( R.id.delivery_activity_tip_payment_method ) TextView mTipPaymentMethod;
    @Bind( R.id.delivery_activity_total ) TextView mTotal;
    @Bind( R.id.delivery_activity_total_payment_method ) TextView mTotalPaymentMethod;
    @Bind( R.id.delivery_activity_total_time ) TextView mTotalTime;
    @Bind( R.id.delivery_activity_start_time ) TextView mStartTime;
    @Bind( R.id.delivery_activity_end_time ) TextView mEndTime;
    @Bind( R.id.delivery_activity_total_mileage ) TextView mTotalMileage;
    @Bind( R.id.delivery_activity_start_mileage ) TextView mStartMileage;
    @Bind( R.id.delivery_activity_end_mileage ) TextView mEndMileage;
    @Bind( R.id.delivery_activity_shift_text ) TextView mShiftText;
    @Bind( R.id.delivery_activity_shift_list ) RecyclerView mShiftsList;

    private Delivery mDelivery;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_delivery );
        ButterKnife.bind( this );
        setResult( Activity.RESULT_CANCELED );

        if( BuildConfig.DEBUG )
        {
            getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
        }

        setSupportActionBar( (Toolbar)findViewById( R.id.delivery_activity_toolbar ) );
        final ActionBar actionBar = getSupportActionBar();
        if( actionBar != null )
        {
            actionBar.setDisplayHomeAsUpEnabled( true );
        }

        final String objectId = getIntent().getStringExtra( EXTRA_DELIVERY_OBJECT_ID );
        Delivery.createQuery()
                .whereEqualTo( Delivery.OBJECT_ID, objectId )
                .getFirstInBackground( new GetCallback<Delivery>()
                {
                    @Override
                    public void done( Delivery object, ParseException e )
                    {
                        if( e == null )
                        {
                            mDelivery = object;
                            update();
                        }
                        else
                        {
                            Delivering.log( "Could not find Delivery with object ID: " + objectId, e );
                            finish();
                            Delivering.oops( e );
                        }
                    }
                } );
    }

    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent data )
    {
        if( requestCode == REQUEST_EDIT_DELIVERY )
        {
            if( resultCode == Activity.RESULT_OK )
            {
                final DeliveryEditConfig config = data.getParcelableExtra( DeliveryEditActivity.EXTRA_DELIVERY_EDIT_CONFIG );
                if( config != null )
                {
                    config.updateDelivery( mDelivery );
                    mDelivery.saveEventually();

                    update();
                    setResult( RESULT_EDITED );
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        getMenuInflater().inflate( R.menu.activity_delivery, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        switch( item.getItemId() )
        {
            case R.id.delivery_activity_action_delete:
                onDeleteClick();
                return true;

            case R.id.delivery_activity_action_edit:
                onEditClick();
                return true;

            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected( item );
        }
    }

    private void onDeleteClick()
    {
        new AlertDialog.Builder( this )
                .setMessage( "Are you sure you want to delete this Delivery?" )
                .setPositiveButton( "Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick( DialogInterface dialog, int which )
                    {
                        final AlertDialog loading = Dialogs.showLoading( DeliveryActivity.this );
                        mDelivery.unpinInBackground( new DeleteCallback()
                        {
                            @Override
                            public void done( ParseException e )
                            {
                                loading.dismiss();
                                if( e == null )
                                {
                                    mDelivery.deleteEventually();
                                    setResult( RESULT_DELETED );
                                    finish();
                                }
                                else
                                {
                                    Delivering.log( "Could not unpin Delivery", e );
                                    Delivering.oops( e );
                                }
                            }
                        } );
                    }
                } )
                .setNegativeButton( "No", null )
                .show();
    }

    private void onEditClick()
    {
        final DeliveryEditConfig config = DeliveryEditConfig.editing( mDelivery ).build();
        final Intent intent = new Intent( this, DeliveryEditActivity.class )
                .putExtra( DeliveryEditActivity.EXTRA_DELIVERY_EDIT_CONFIG, config );
        startActivityForResult( intent, REQUEST_EDIT_DELIVERY );
    }

    private void update()
    {
        mName.setText( mDelivery.getName() );
        Formatter.deliveryStartEndTimes( mDelivery, mTotalTime, mStartTime, mEndTime );
        Formatter.deliveryStartEndMileage( mDelivery, mTotalMileage, mStartMileage, mEndMileage );
        Formatter.deliveryTipTotal( mDelivery, mTip, mTipPaymentMethod, mTotal, mTotalPaymentMethod );

        final Shift shift = mDelivery.getShift();
        if( shift != null )
        {
            mShiftText.setVisibility( View.GONE );
            mShiftsList.setVisibility( View.VISIBLE );

            final ShiftsAdapter adapter = new ShiftsAdapter( this, Collections.singletonList( shift ) );
            mShiftsList.setAdapter( adapter );

            mShiftsList.setLayoutFrozen( true );
        }
        else
        {
            mShiftText.setVisibility( View.VISIBLE );
            mShiftsList.setVisibility( View.GONE );

            mShiftText.setText( "No Shift set" );
        }
    }
}
