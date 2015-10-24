package com.adamnickle.delivering;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.parse.DeleteCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseObject;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    private SummaryFragment mSummaryFragment;
    private ShiftsFragment mShiftsFragment;
    private DeliveriesFragment mDeliveriesFragment;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        if( BuildConfig.DEBUG )
        {
            getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
        }

        final Toolbar toolbar = (Toolbar)findViewById( R.id.main_activity_toolbar );
        setSupportActionBar( toolbar );

        final DrawerLayout drawer = (DrawerLayout)findViewById( R.id.main_activity_drawer_layout );
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle( this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close );
        drawer.setDrawerListener( toggle );
        toggle.syncState();

        final NavigationView navigationView = (NavigationView)findViewById( R.id.main_activity_nav_layout );
        navigationView.setNavigationItemSelectedListener( this );

        navigationView.addOnLayoutChangeListener( new View.OnLayoutChangeListener()
        {
            @Override
            public void onLayoutChange( View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom )
            {
                navigationView.removeOnLayoutChangeListener( this );

                final Deliverer user = Deliverer.getCurrentUser();

                final TextView fullName = (TextView)navigationView.findViewById( R.id.main_activity_drawer_header_user_full_name );
                fullName.setText( user.getUsername() );

                final TextView userEmail = (TextView)navigationView.findViewById( R.id.main_activity_drawer_header_user_email );
                userEmail.setText( user.getEmail() );
            }
        } );

        if( savedInstanceState != null )
        {
            mSummaryFragment = (SummaryFragment)getSupportFragmentManager()
                    .getFragment( savedInstanceState, SummaryFragment.FRAGMENT_TAG );
            mShiftsFragment = (ShiftsFragment)getSupportFragmentManager()
                    .getFragment( savedInstanceState, ShiftsFragment.FRAGMENT_TAG );
            mDeliveriesFragment = (DeliveriesFragment)getSupportFragmentManager()
                    .getFragment( savedInstanceState, DeliveriesFragment.FRAGMENT_TAG );
        }
        else
        {
            navigationView.setCheckedItem( R.id.main_drawer_action_summary );
            openSummary();
        }

        getSupportFragmentManager().addOnBackStackChangedListener( new FragmentManager.OnBackStackChangedListener()
        {
            @Override
            public void onBackStackChanged()
            {
                final Fragment fragment = getCurrentFragment();
                if( fragment instanceof SummaryFragment )
                {
                    setTitle( "Summary" );
                }
                else if( fragment instanceof ShiftsFragment )
                {
                    setTitle( "Shifts" );
                }
                else if( fragment instanceof ShiftFragment )
                {
                    setTitle( "Shift" );
                }
                else if( fragment instanceof DeliveriesFragment )
                {
                    setTitle( "Deliveries" );
                }
            }
        } );
    }

    @Override
    public void onBackPressed()
    {
        final DrawerLayout drawer = (DrawerLayout)findViewById( R.id.main_activity_drawer_layout );
        if( drawer.isDrawerOpen( GravityCompat.START ) )
        {
            drawer.closeDrawer( GravityCompat.START );
        }
        else
        {
            if( getSupportFragmentManager().getBackStackEntryCount() == 1 )
            {
                finish();
            }
            else
            {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected( MenuItem item )
    {
        switch( item.getItemId() )
        {
            case R.id.main_drawer_action_summary:
                openSummary();
                closeDrawer();
                return true;

            case R.id.main_drawer_action_shifts:
                openShifts();
                closeDrawer();
                return true;

            case R.id.main_drawer_action_deliveries:
                openDeliveries();
                closeDrawer();
                return true;

            case R.id.main_drawer_action_logout:
                new AlertDialog.Builder( this )
                        .setMessage( "Are you sure you want to logout?" )
                        .setPositiveButton( "Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick( DialogInterface dialog, int which )
                            {
                                logout();
                            }
                        } )
                        .setNegativeButton( "No", null )
                        .show();
                closeDrawer();
                return false;

            case R.id.main_drawer_action_settings:
                Delivering.toast( "Sorry, no settings yet." );
                closeDrawer();
                return false;

            default:
                return false;
        }
    }

    private void logout()
    {
        Deliverer.logOutInBackground( new LogOutCallback()
        {
            @Override
            public void done( ParseException ex )
            {
                if( ex == null )
                {
                    ParseObject.unpinAllInBackground( new DeleteCallback()
                    {
                        @Override
                        public void done( ParseException e )
                        {
                            if( e != null )
                            {
                                Delivering.log( "Could not unpin all objects", e );
                            }
                        }
                    } );

                    startActivity( new Intent( MainActivity.this, LoginActivity.class ) );
                    finish();
                }
                else
                {
                    Delivering.log( "Could not logout.", ex );
                    Delivering.oops( ex );
                }
            }
        } );
    }

    private Fragment getCurrentFragment()
    {
        return getSupportFragmentManager().findFragmentById( R.id.main_activity_content_holder );
    }

    private void addFragment( String fragmentTag, Fragment fragment )
    {
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack( fragmentTag )
                .replace( R.id.main_activity_content_holder, fragment )
                .commit();
    }

    private void popOrAdd( String fragmentTag, Fragment fragment )
    {
        final Fragment currentFragment = getCurrentFragment();
        if( currentFragment != null && currentFragment == fragment )
        {
            return;
        }
        if( fragmentTag == null || !getSupportFragmentManager().popBackStackImmediate( fragmentTag, 0 ) )
        {
            addFragment( fragmentTag, fragment );
        }
    }

    private void openSummary()
    {
        if( mSummaryFragment == null )
        {
            mSummaryFragment = SummaryFragment.newInstance();
        }
        popOrAdd( SummaryFragment.FRAGMENT_TAG, mSummaryFragment );
    }

    private void openShifts()
    {
        if( mShiftsFragment == null )
        {
            mShiftsFragment = ShiftsFragment.newInstance();
        }
        popOrAdd( ShiftsFragment.FRAGMENT_TAG, mShiftsFragment );
    }

    private void openDeliveries()
    {
        if( mDeliveriesFragment == null )
        {
            mDeliveriesFragment = DeliveriesFragment.newInstance();
        }
        popOrAdd( DeliveriesFragment.FRAGMENT_TAG, mDeliveriesFragment );
    }

    public void closeDrawer()
    {
        final DrawerLayout drawer = (DrawerLayout)findViewById( R.id.main_activity_drawer_layout );
        drawer.closeDrawer( GravityCompat.START );
    }
}
