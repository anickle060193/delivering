package com.adamnickle.delivering;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

public class DeliveryCreatorActivity extends AppCompatActivity
{
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

        getSupportFragmentManager().addOnBackStackChangedListener( new FragmentManager.OnBackStackChangedListener()
        {
            @Override
            public void onBackStackChanged()
            {
                final Fragment fragment = getSupportFragmentManager().findFragmentById( R.id.delivery_creator_activity_content_holder );
                if( fragment instanceof DeliveryBaseFragment )
                {
                    mCurrentStep = 0;

                    mPrevious.setEnabled( false );
                    mNext.setText( "Next" );
                }
                else if( fragment instanceof DeliveryOriginFragment )
                {
                    mCurrentStep = 1;

                    mPrevious.setEnabled( true );
                    mNext.setText( "Next" );
                }
                else if( fragment instanceof DeliveryDestinationFragment )
                {
                    mCurrentStep = 2;

                    mPrevious.setEnabled( true );
                    mNext.setText( "Done" );
                }
            }
        } );

        getSupportFragmentManager()
                .beginTransaction()
                .add( R.id.delivery_creator_activity_content_holder, DeliveryBaseFragment.newInstance() )
                .commit();
    }

    private void onPreviousClicked()
    {
        getSupportFragmentManager().popBackStack();
    }

    private void onNextClicked()
    {
        if( mCurrentStep == 0 )
        {
            if( mUseCurrentLocation )
            {
                if( mDestinationFragment == null )
                {
                    mDestinationFragment = DeliveryDestinationFragment.newInstance();
                }
                getSupportFragmentManager()
                        .beginTransaction()
                        .addToBackStack( null )
                        .replace( R.id.delivery_creator_activity_content_holder, mDestinationFragment )
                        .commit();
            }
            else
            {
                if( mOriginFragment == null )
                {
                    mOriginFragment = DeliveryOriginFragment.newInstance();
                }
                getSupportFragmentManager()
                        .beginTransaction()
                        .addToBackStack( null )
                        .replace( R.id.delivery_creator_activity_content_holder, mOriginFragment )
                        .commit();
            }
        }
        else if( mCurrentStep == 1 )
        {
            if( mDestinationFragment == null )
            {
                mDestinationFragment = DeliveryDestinationFragment.newInstance();
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .addToBackStack( null )
                    .replace( R.id.delivery_creator_activity_content_holder, mDestinationFragment )
                    .commit();
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
