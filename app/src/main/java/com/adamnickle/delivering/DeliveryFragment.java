package com.adamnickle.delivering;

import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.MapView;

import java.io.IOException;


public class DeliveryFragment extends Fragment
{
    private GoogleApiClient mGoogleApiClient;

    private Location mLastLocation;
    private Geocoder mGeocoder;

    private View mMainView;
    private MapView mMap;

    public static DeliveryFragment newInstance()
    {
        final DeliveryFragment fragment = new DeliveryFragment();
        return fragment;
    }

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setRetainInstance( true );

        mGeocoder = new Geocoder( getActivity() );
        try
        {
            mGeocoder.getFromLocationName( "11725 S Shannan St Apt 805", 10 );
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        if( mMainView == null )
        {
            mMainView = inflater.inflate( R.layout.fragment_delivery, container, false );
            mMap = (MapView)mMainView.findViewById( R.id.delivery_fragment_map );

        }
        else
        {
            Utilities.removeFromParent( mMainView );
        }
        return mMainView;
    }

    private void buildGoogleApiClient()
    {
        mGoogleApiClient = new GoogleApiClient.Builder( getActivity() )
                .addConnectionCallbacks( mConnectionCallbacks )
                .addOnConnectionFailedListener( mConnectionFailedListener )
                .addApi( LocationServices.API )
                .build();
    }

    private final GoogleApiClient.ConnectionCallbacks mConnectionCallbacks = new GoogleApiClient.ConnectionCallbacks()
    {
        @Override
        public void onConnected( Bundle bundle )
        {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation( mGoogleApiClient );
            if( mLastLocation != null )
            {

            }
        }

        @Override
        public void onConnectionSuspended( int i )
        {

        }
    };

    private final GoogleApiClient.OnConnectionFailedListener mConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener()
    {
        @Override
        public void onConnectionFailed( ConnectionResult connectionResult )
        {

        }
    };
}