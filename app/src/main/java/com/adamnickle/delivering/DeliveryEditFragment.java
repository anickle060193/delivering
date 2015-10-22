package com.adamnickle.delivering;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


@SuppressLint("ValidFragment")
public class DeliveryEditFragment extends Fragment
{
    public interface DeliveryEditFragmentListener
    {
        void onDeliveryEdited( Delivery delivery );
    }

    @Bind( R.id.delivery_edit_fragment_name ) EditText mName;
    @Bind( R.id.delivery_edit_fragment_tip ) EditText mTip;
    @Bind( R.id.delivery_edit_fragment_tip_payment_method ) Spinner mTipPaymentMethod;
    @Bind( R.id.delivery_edit_fragment_total ) EditText mTotal;
    @Bind( R.id.delivery_edit_fragment_total_payment_method ) Spinner mTotalPaymentMethod;
    @Bind( R.id.delivery_edit_fragment_start_time ) EditText mStartTime;
    @Bind( R.id.delivery_edit_fragment_end_time ) EditText mEndTime;
    @Bind( R.id.delivery_edit_fragment_start_mileage ) EditText mStartMileage;
    @Bind( R.id.delivery_edit_fragment_end_mileage ) EditText mEndMileage;

    private final Delivery mDelivery;

    private DeliveryEditFragmentListener mListener;

    private View mMainView;

    public static DeliveryEditFragment newInstance( Delivery delivery, DeliveryEditFragmentListener listener )
    {
        DeliveryEditFragment fragment = new DeliveryEditFragment( delivery );
        fragment.mListener = listener;
        return fragment;
    }

    public DeliveryEditFragment( Delivery delivery )
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
            mMainView = inflater.inflate( R.layout.fragment_delivery_edit, container, false );
            ButterKnife.bind( this, mMainView );
            init();
        }
        else
        {
            Utilities.removeFromParent( mMainView );
        }
        return mMainView;
    }

    @Override
    public void onCreateOptionsMenu( Menu menu, MenuInflater inflater )
    {
        inflater.inflate( R.menu.fragment_delivery_edit, menu );
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        switch( item.getItemId() )
        {
            case R.id.delivery_edit_fragment_action_done:
                onDoneClick();
                return true;

            default:
                return super.onOptionsItemSelected( item );
        }
    }

    private void onDoneClick()
    {

    }

    private void init()
    {
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource( getActivity(), R.array.delivery_payment_methods, android.R.layout.simple_spinner_item );
        adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        final List<String> paymentMethods = Arrays.asList( getResources().getStringArray( R.array.delivery_payment_methods ) );

        mName.setText( mDelivery.getName() );

        mTip.setText( Utilities.formatPlainMoney( mDelivery.getTip() ) );

        mTipPaymentMethod.setAdapter( adapter );
        mTipPaymentMethod.setSelection( paymentMethods.indexOf( mDelivery.getTipPaymentMethod() ) );

        mTotal.setText( Utilities.formatPlainMoney( mDelivery.getTotal() ) );

        mTotalPaymentMethod.setAdapter( adapter );
        mTotalPaymentMethod.setSelection( paymentMethods.indexOf( mDelivery.getTotalPaymentMethod() ) );

        mStartTime.setText( Utilities.formatShortDate( mDelivery.getDeliveryStart() ) );
        mEndTime.setText( Utilities.formatShortDate( mDelivery.getDeliveryEnd() ) );

        mStartMileage.setText( Utilities.formatMileage( mDelivery.getStartMileage() ) );
        mEndMileage.setText( Utilities.formatMileage( mDelivery.getEndMileage() ) );
    }
}
