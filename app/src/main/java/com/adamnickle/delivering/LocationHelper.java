package com.adamnickle.delivering;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;


public abstract class LocationHelper
{
    private LocationHelper() { }

    public interface AddressDecoderListener
    {
        void onAddressDecoded( List<Address> addresses );
    }

    public static void decodeAddress( final Context context, String address, final AddressDecoderListener listener )
    {
        new AsyncTask<String, Integer, List<Address>>()
        {
            @Override
            protected void onPostExecute( List<Address> addresses )
            {
                listener.onAddressDecoded( addresses );
            }

            @Override
            protected List<Address> doInBackground( String... params )
            {
                final String address = params[ 0 ];
                try
                {
                    final Geocoder geocoder = new Geocoder( context );
                    return geocoder.getFromLocationName( address, 3 );
                }
                catch( IOException ex )
                {
                    Delivering.log( "An error occurred looking up the address: \"" + address + "\"", ex );
                }
                return null;
            }
        }.execute( address );
    }

    public interface LocationDecoderListener
    {
        void onLocationDecoded( Address address );
    }

    public static void decodeLocation( final Context context, Location location, final LocationDecoderListener listener )
    {
        new AsyncTask<Location, Integer, Address>()
        {
            @Override
            protected void onPostExecute( Address address )
            {
                listener.onLocationDecoded( address );
            }

            @Override
            protected Address doInBackground( Location... params )
            {
                final Location location = params[ 0 ];
                final Geocoder geocoder = new Geocoder( context );
                try
                {
                    final List<Address> addresses = geocoder.getFromLocation( location.getLatitude(), location.getLongitude(), 1 );
                    if( addresses.size() > 0 )
                    {
                        return addresses.get( 0 );
                    }
                }
                catch( IOException ex )
                {
                    Delivering.log( "An error occurred decoding the location: " + location, ex );
                }
                return null;
            }
        }.execute( location );
    }

    public interface LocationFinderListener
    {
        void onLocationFound( Location location );
        void onConnectionSuspended( int cause );
        void onConnectionFailed( ConnectionResult result );
    }

    private static class LocationFinderHelper
    {
        private final Context mContext;
        private final LocationFinderListener mListener;

        private GoogleApiClient mClient;

        public LocationFinderHelper( Context context, LocationFinderListener listener )
        {
            mContext = context;
            mListener = listener;
        }

        public void findCurrentLocation()
        {
            mClient = new GoogleApiClient.Builder( mContext )
                    .addConnectionCallbacks( new GoogleApiClient.ConnectionCallbacks()
                    {
                        @Override
                        public void onConnected( Bundle bundle )
                        {
                            final Location location = LocationServices.FusedLocationApi.getLastLocation( mClient );
                            mListener.onLocationFound( location );
                        }

                        @Override
                        public void onConnectionSuspended( int cause )
                        {
                            mListener.onConnectionSuspended( cause );
                        }
                    } )
                    .addOnConnectionFailedListener( new GoogleApiClient.OnConnectionFailedListener()
                    {
                        @Override
                        public void onConnectionFailed( ConnectionResult result )
                        {
                            mListener.onConnectionFailed( result );
                        }
                    } )
                    .addApi( LocationServices.API )
                    .build();
            mClient.connect();
        }
    }

    public static void findCurrentLocation( Context context, LocationFinderListener listener )
    {
        final LocationFinderHelper helper = new LocationFinderHelper( context, listener );
        helper.findCurrentLocation();
    }
}
