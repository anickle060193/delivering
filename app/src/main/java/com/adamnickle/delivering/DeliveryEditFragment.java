package com.adamnickle.delivering;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


@SuppressLint("ValidFragment")
public class DeliveryEditFragment extends Fragment
{
    public interface DeliveryEditFragmentListener
    {
        void onDeliveryEdited( Delivery delivery );
    }

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
}
