package com.adamnickle.delivering;

import android.app.Fragment;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.MapView;

import java.io.IOException;
import java.util.List;


public class DeliveryCreatorFragment extends Fragment
{
    private View mMainView;
    private EditText mAddressSearch;
    private MapView mMap;

    private Geocoder mGeocoder;

    public static DeliveryCreatorFragment newInstance()
    {
        DeliveryCreatorFragment fragment = new DeliveryCreatorFragment();
        return fragment;
    }

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setRetainInstance( true );

        mGeocoder = new Geocoder( getActivity() );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        if( mMainView == null )
        {
            mMainView = inflater.inflate( R.layout.fragment_delivery_creator, container, false );
            mAddressSearch = (EditText)mMainView.findViewById( R.id.delivery_creator_address_search );
            mMap = (MapView)mMainView.findViewById( R.id.delivery_creator_fragment_map );

            mAddressSearch.setOnEditorActionListener( new TextView.OnEditorActionListener()
            {
                @Override
                public boolean onEditorAction( TextView v, int actionId, KeyEvent event )
                {
                    if( actionId == EditorInfo.IME_ACTION_SEARCH )
                    {

                        return true;
                    }
                    return false;
                }
            } );
        }
        else
        {
            Utilities.removeFromParent( mMainView );
        }
        return mMainView;
    }

    private void search( String address )
    {
        final AddressSearchTask task = new AddressSearchTask();
        task.execute( address );
    }

    private class AddressSearchTask extends AsyncTask<String, Object, List<Address>>
    {
        @Override
        protected void onPostExecute( List<Address> addresses )
        {
            if( addresses != null )
            {
                
            }
        }

        @Override
        protected List<Address> doInBackground( String... params )
        {
            if( params.length > 0 )
            {
                try
                {
                    return mGeocoder.getFromLocationName( params[ 0 ], 3 );
                }
                catch( IOException ex )
                {
                    Delivering.log( "An error occurred while search for addresses.", ex );
                }
            }
            return null;
        }
    }
}
