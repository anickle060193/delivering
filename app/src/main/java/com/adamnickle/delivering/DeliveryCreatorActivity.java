package com.adamnickle.delivering;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

public class DeliveryCreatorActivity extends AppCompatActivity
{
    private DeliveryCreatorPageAdapter mDeliveryCreatorPageAdapter;

    private ViewPager mViewPager;

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

        mDeliveryCreatorPageAdapter = new DeliveryCreatorPageAdapter( getSupportFragmentManager() );

        mViewPager = (ViewPager)findViewById( R.id.delivery_creator_activity_pager );
        mViewPager.setAdapter( mDeliveryCreatorPageAdapter );

        TabLayout tabLayout = (TabLayout)findViewById( R.id.delivery_creator_activity_tabs );
        tabLayout.setupWithViewPager( mViewPager );
    }

    public class DeliveryCreatorPageAdapter extends FragmentPagerAdapter
    {
        public DeliveryCreatorPageAdapter( FragmentManager fragmentManager )
        {
            super( fragmentManager );
        }

        @Override
        public Fragment getItem( int position )
        {
            switch( position )
            {
                case 0:
                    return DeliveryBaseFragment.newInstance();
                case 1:
                    return DeliveryOriginFragment.newInstance();
                case 2:
                    return DeliveryDestinationFragment.newInstance();
            }
            return null;
        }

        @Override
        public int getCount()
        {
            return 3;
        }

        @Override
        public CharSequence getPageTitle( int position )
        {
            switch( position )
            {
                case 0:
                    return "Delivery";
                case 1:
                    return "Origin";
                case 2:
                    return "Destination";
            }
            return null;
        }
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
            mMap.animateCamera( CameraUpdateFactory.newLatLngZoom( new LatLng( 0, 0 ), 20 ) );
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
            mMap.animateCamera( CameraUpdateFactory.newLatLngZoom( new LatLng( 0, 0 ), 20 ) );
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
