package com.adamnickle.delivering;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.DeleteCallback;
import com.parse.ParseException;

import java.math.BigDecimal;
import java.util.Collections;

import butterknife.Bind;
import butterknife.ButterKnife;


@SuppressLint("ValidFragment")
public class DeliveryFragment extends Fragment
{
    public interface DeliveryFragmentListener
    {
        void onDeliveryEdited( Delivery delivery );
        void onDeliveryDeleted( Delivery delivery );
    }

    private final Delivery mDelivery;

    private DeliveryFragmentListener mListener;

    private View mMainView;
    @Bind( R.id.delivery_fragment_name ) TextView mName;
    @Bind( R.id.delivery_fragment_tip ) TextView mTip;
    @Bind( R.id.delivery_fragment_tip_payment_method ) TextView mTipPaymentMethod;
    @Bind( R.id.delivery_fragment_total ) TextView mTotal;
    @Bind( R.id.delivery_fragment_total_payment_method ) TextView mTotalPaymentMethod;
    @Bind( R.id.delivery_fragment_total_time ) TextView mTotalTime;
    @Bind( R.id.delivery_fragment_start_time ) TextView mStartTime;
    @Bind( R.id.delivery_fragment_end_time ) TextView mEndTime;
    @Bind( R.id.delivery_fragment_total_mileage ) TextView mTotalMileage;
    @Bind( R.id.delivery_fragment_start_mileage ) TextView mStartMileage;
    @Bind( R.id.delivery_fragment_end_mileage ) TextView mEndMileage;
    @Bind( R.id.delivery_fragment_shift_text ) TextView mShiftText;
    @Bind( R.id.delivery_fragment_shift_list ) RecyclerView mShiftsList;

    public static DeliveryFragment newInstance( Delivery delivery, DeliveryFragmentListener listener )
    {
        final DeliveryFragment fragment = new DeliveryFragment( delivery );
        fragment.mListener = listener;
        return fragment;
    }

    public DeliveryFragment( Delivery delivery )
    {
        mDelivery = delivery;
    }

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setRetainInstance( true );
        setHasOptionsMenu( true );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        if( mMainView == null )
        {
            mMainView = inflater.inflate( R.layout.fragment_delivery, container, false );
            ButterKnife.bind( this, mMainView );
        }
        else
        {
            Utilities.removeFromParent( mMainView );
        }
        update();
        return mMainView;
    }

    @Override
    public void onCreateOptionsMenu( Menu menu, MenuInflater inflater )
    {
        inflater.inflate( R.menu.fragment_delivery, menu );
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        switch( item.getItemId() )
        {
            case R.id.delivery_fragment_action_delete:
                onDeleteClick();
                return true;

            case R.id.delivery_fragment_action_edit:
                onEditClick();
                return true;

            default:
                return super.onOptionsItemSelected( item );
        }
    }

    private void onDeleteClick()
    {
        new AlertDialog.Builder( getActivity() )
                .setMessage( "Are you sure you want to delete this Delivery?" )
                .setPositiveButton( "Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick( DialogInterface dialog, int which )
                    {
                        final AlertDialog loading = Dialogs.showLoading( getActivity() );
                        mDelivery.unpinInBackground( new DeleteCallback()
                        {
                            @Override
                            public void done( ParseException e )
                            {
                                loading.dismiss();
                                if( e == null )
                                {
                                    mDelivery.deleteEventually();
                                    getActivity().getSupportFragmentManager()
                                            .popBackStack();
                                    if( mListener != null )
                                    {
                                        mListener.onDeliveryDeleted( mDelivery );
                                    }
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

    }

    private void update()
    {
        mName.setText( mDelivery.getName() );

        final BigDecimal tip = mDelivery.getTip();
        if( tip != null )
        {
            mTip.setText( Utilities.CURRENCY_FORMATTER.format( tip ) );
        }
        else
        {
            mTip.setText( "No tip yet" );
        }
        mTipPaymentMethod.setText( mDelivery.getTipPaymentMethod() );

        final BigDecimal total = mDelivery.getTotal();
        if( total != null )
        {
            mTotal.setText( Utilities.CURRENCY_FORMATTER.format( total ) );
        }
        else
        {
            mTotal.setText( "No total yet" );
        }
        mTotalPaymentMethod.setText( mDelivery.getTotalPaymentMethod() );

        if( mDelivery.isCompleted() )
        {
            final long start = mDelivery.getDeliveryStart().getTime();
            final long end = mDelivery.getDeliveryEnd().getTime();
            final String timeSpan = Utilities.formatTimeSpan( end - start );
            mTotalTime.setText( timeSpan );
            mStartTime.setText( Utilities.SHORT_TIME_FORMAT.format( start ) );
            mEndTime.setText( Utilities.SHORT_TIME_FORMAT.format( end ) );

            final double startMileage = mDelivery.getStartMileage();
            final double endMileage = mDelivery.getEndMileage();
            mTotalMileage.setText( Utilities.MILEAGE_FORMATTER.format( endMileage - startMileage ) + " miles" );
            mStartMileage.setText( Utilities.MILEAGE_FORMATTER.format( startMileage ) );
            mEndMileage.setText( Utilities.MILEAGE_FORMATTER.format( endMileage ) );
        }
        else if( mDelivery.isInProgress() )
        {
            final long start = mDelivery.getDeliveryStart().getTime();
            final String pastTime = Utilities.formatPastTime( start );
            mTotalTime.setText( "Started " + pastTime );
            mStartTime.setText( Utilities.SHORT_TIME_FORMAT.format( start ) );
            mEndTime.setText( "Not yet ended" );

            final double startMileage = mDelivery.getStartMileage();
            mTotalMileage.setText( "--------" );
            mStartMileage.setText( Utilities.MILEAGE_FORMATTER.format( startMileage ) );
            mEndMileage.setText( "Not yet ended" );
        }
        else
        {
            mTotalTime.setText( "--------" );
            mStartTime.setText( "Not yet started" );
            mEndTime.setText( "Not yet started" );

            mTotalMileage.setText( "--------" );
            mStartMileage.setText( "Not yet started" );
            mEndMileage.setText( "Not yet started" );
        }

        final Shift shift = mDelivery.getShift();
        if( shift != null )
        {
            mShiftText.setVisibility( View.GONE );
            mShiftsList.setVisibility( View.VISIBLE );

            final ShiftsAdapter adapter = new ShiftsAdapter( getActivity(), Collections.singletonList( shift ) );
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
