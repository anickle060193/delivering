package com.adamnickle.delivering;

import android.graphics.Color;
import android.location.Address;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class DeliveryCreatorActivity extends AppCompatActivity
{
    private static final int BASE_STEP = 0;
    private static final int ORIGIN_STEP = 1;
    private static final int DESTINATION_STEP = 2;

    private Button mPrevious;
    private Button mNext;

    private boolean mUseCurrentLocation;
    private int mCurrentStep;

    private DeliveryBaseFragment mBaseFragment;
    private DeliveryOriginFragment mOriginFragment;
    private DeliveryDestinationFragment mDestinationFragment;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_delivery_creator );

        if( BuildConfig.DEBUG )
        {
            getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
        }

        setSupportActionBar( (Toolbar)findViewById( R.id.delivery_creator_activity_toolbar ) );

        mPrevious = (Button)findViewById( R.id.delivery_creator_activity_previous );
        mPrevious.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                onPreviousClicked();
            }
        } );

        mNext = (Button)findViewById( R.id.delivery_creator_activity_next );
        mNext.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                onNextClicked();
            }
        } );

        showBaseFragment();
    }

    @Override
    public void onBackPressed()
    {
        if( mCurrentStep == BASE_STEP )
        {
            finish();
        }
        else
        {
            onPreviousClicked();
        }
    }

    private void showFragment( Fragment fragment )
    {
        getSupportFragmentManager()
                .beginTransaction()
                .replace( R.id.delivery_creator_activity_content_holder, fragment )
                .commit();
    }

    private void showBaseFragment()
    {
        if( mBaseFragment == null )
        {
            mBaseFragment = DeliveryBaseFragment.newInstance();
        }
        showFragment( mBaseFragment );

        mCurrentStep = BASE_STEP;
        mPrevious.setEnabled( false );
        mNext.setText( "Next" );
    }

    private void showOriginFragment()
    {
        if( mOriginFragment == null )
        {
            mOriginFragment = DeliveryOriginFragment.newInstance();
        }
        showFragment( mOriginFragment );

        mCurrentStep = ORIGIN_STEP;
        mPrevious.setEnabled( true );
        mNext.setText( "Next" );
    }

    private void showDestinationFragment()
    {
        if( mDestinationFragment == null )
        {
            mDestinationFragment = DeliveryDestinationFragment.newInstance();
        }
        showFragment( mDestinationFragment );

        mCurrentStep = DESTINATION_STEP;
        mPrevious.setEnabled( true );
        mNext.setText( "Done" );
    }

    private void onPreviousClicked()
    {
        if( mCurrentStep == ORIGIN_STEP )
        {
            showBaseFragment();
        }
        else if( mCurrentStep == DESTINATION_STEP )
        {
            showOriginFragment();
        }
    }

    private void onNextClicked()
    {
        if( mCurrentStep == BASE_STEP )
        {
            if( mUseCurrentLocation )
            {
                showDestinationFragment();
            }
            else
            {
                showOriginFragment();
            }
        }
        else if( mCurrentStep == ORIGIN_STEP )
        {
            showDestinationFragment();
        }
        else
        {
            finish();
        }
    }

    public void setUseCurrentLocation( boolean useCurrentLocation )
    {
        mUseCurrentLocation = useCurrentLocation;
    }

    public boolean getUseCurrentLocation()
    {
        return mUseCurrentLocation;
    }

    public static void setBackground( TextInputLayout layout )
    {
        final int color = ContextCompat.getColor( layout.getContext(), R.color.colorPrimaryDark );
        final int background = Color.argb( 130, Color.red( color ), Color.green( color ), Color.blue( color ) );
        layout.setBackgroundColor( background );
    }

    public static class DeliveryBaseFragment extends Fragment
    {
        private View mMainView;
        private EditText mName;
        private EditText mDistance;

        public static DeliveryBaseFragment newInstance()
        {
            return new DeliveryBaseFragment();
        }

        @Override
        public void onCreate( Bundle savedInstanceState )
        {
            super.onCreate( savedInstanceState );
            setRetainInstance( true );
        }

        @Override
        public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
        {
            if( mMainView == null )
            {
                mMainView = inflater.inflate( R.layout.fragment_delivery_creator_base, container, false );
                mName = (EditText)mMainView.findViewById( R.id.delivery_creator_base_fragment_name );
                mDistance = (EditText)mMainView.findViewById( R.id.delivery_creator_base_fragment_distance );
                mDistance.setOnEditorActionListener( new TextView.OnEditorActionListener()
                {
                    @Override
                    public boolean onEditorAction( TextView v, int actionId, KeyEvent event )
                    {
                        if( actionId == EditorInfo.IME_ACTION_DONE )
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
    }

    public static class DeliveryOriginFragment extends Fragment implements OnMapReadyCallback
    {
        private View mMainView;
        private EditText mOriginAddress;
        private MapView mMapView;

        private GoogleMap mMap;

        private List<Address> mAddresses;

        private final HashMap<String, Address> mMarkerToAddress = new HashMap<>();

        public static DeliveryOriginFragment newInstance()
        {
            return new DeliveryOriginFragment();
        }

        @Override
        public void onCreate( Bundle savedInstanceState )
        {
            super.onCreate( savedInstanceState );
            setRetainInstance( true );
        }

        @Override
        public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
        {
            if( mMainView == null )
            {
                mMainView = inflater.inflate( R.layout.fragment_delivery_creator_origin, container, false );
                mOriginAddress = (EditText)mMainView.findViewById( R.id.delivery_creator_origin_fragment_origin_address );
                mMapView = (MapView)mMainView.findViewById( R.id.delivery_creator_origin_fragment_map );

                DeliveryCreatorActivity.setBackground( (TextInputLayout)mOriginAddress.getParent() );

                mMapView.getMapAsync( this );
                mMapView.onCreate( savedInstanceState );

                mOriginAddress.setOnEditorActionListener( new TextView.OnEditorActionListener()
                {
                    @Override
                    public boolean onEditorAction( TextView v, int actionId, KeyEvent event )
                    {
                        if( actionId == EditorInfo.IME_ACTION_SEARCH )
                        {
                            findAddresses();
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

        @Override
        public void onMapReady( GoogleMap googleMap )
        {
            mMap = googleMap;
            updateMap();
        }

        private void findAddresses()
        {
            final String address = mOriginAddress.getText().toString();
            LocationHelper.decodeAddress( getActivity(), address, new LocationHelper.AddressDecoderListener()
            {
                @Override
                public void onAddressDecoded( List<Address> addresses )
                {
                    mAddresses = addresses;
                    updateMap();
                }
            } );
        }

        private void updateMap()
        {
            if( mMap != null )
            {
                mMap.clear();
                mMarkerToAddress.clear();
                if( mAddresses != null )
                {
                    final List<LatLng> latLngs = new ArrayList<>();
                    for( Address address : mAddresses )
                    {
                        if( address.hasLatitude() && address.hasLatitude() )
                        {
                            final LatLng latLng = new LatLng( address.getLatitude(), address.getLongitude() );
                            final MarkerOptions options = new MarkerOptions()
                                    .position( latLng )
                                    .title( address.getAddressLine( 0 ) + "\n" + address.getAddressLine( 1 ) );
                            final Marker marker = mMap.addMarker( options );
                            mMarkerToAddress.put( marker.getId(), address );
                            latLngs.add( latLng );
                        }
                    }
                    final LatLngBounds.Builder builder = LatLngBounds.builder();
                    for( LatLng latLng : latLngs )
                    {
                        builder.include( latLng );
                    }
                    mMap.animateCamera( CameraUpdateFactory.newLatLngBounds( builder.build(), 50 ) );
                }
            }
        }

        @Override
        public void onSaveInstanceState( Bundle outState )
        {
            super.onSaveInstanceState( outState );

            mMapView.onSaveInstanceState( outState );
        }

        @Override
        public void onResume()
        {
            super.onResume();

            mMapView.onResume();
        }

        @Override
        public void onPause()
        {
            super.onPause();

            mMapView.onPause();
        }

        @Override
        public void onDestroy()
        {
            super.onDestroy();

            mMapView.onDestroy();
        }
    }

    public static class DeliveryDestinationFragment extends Fragment implements OnMapReadyCallback
    {
        private View mMainView;
        private EditText mDestinationAddress;
        private MapView mMapView;

        private GoogleMap mMap;

        public static DeliveryDestinationFragment newInstance()
        {
            return new DeliveryDestinationFragment();
        }

        @Override
        public void onCreate( Bundle savedInstanceState )
        {
            super.onCreate( savedInstanceState );
            setRetainInstance( true );
        }

        @Override
        public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
        {
            if( mMainView == null )
            {
                mMainView = inflater.inflate( R.layout.fragment_delivery_creator_destination, container, false );
                mDestinationAddress = (EditText)mMainView.findViewById( R.id.delivery_creator_destination_fragment_destination_address );
                mMapView = (MapView)mMainView.findViewById( R.id.delivery_creator_destination_fragment_map );

                DeliveryCreatorActivity.setBackground( (TextInputLayout)mDestinationAddress.getParent() );

                mMapView.getMapAsync( this );
                mMapView.onCreate( savedInstanceState );
            }
            else
            {
                Utilities.removeFromParent( mMainView );
            }
            return mMainView;
        }

        @Override
        public void onMapReady( GoogleMap googleMap )
        {
            mMap = googleMap;
        }

        @Override
        public void onSaveInstanceState( Bundle outState )
        {
            super.onSaveInstanceState( outState );

            mMapView.onSaveInstanceState( outState );
        }

        @Override
        public void onResume()
        {
            super.onResume();

            mMapView.onResume();
        }

        @Override
        public void onPause()
        {
            super.onPause();

            mMapView.onPause();
        }

        @Override
        public void onDestroy()
        {
            super.onDestroy();

            mMapView.onDestroy();
        }
    }
}
