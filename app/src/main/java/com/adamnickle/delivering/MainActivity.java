package com.adamnickle.delivering;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.parse.LogOutCallback;
import com.parse.ParseException;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
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

        final Toolbar toolbar = (Toolbar)findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );

        final DrawerLayout drawer = (DrawerLayout)findViewById( R.id.drawer_layout );
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle( this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close );
        drawer.setDrawerListener( toggle );
        toggle.syncState();

        final NavigationView navigationView = (NavigationView)findViewById( R.id.nav_view );
        navigationView.setNavigationItemSelectedListener( this );

        navigationView.addOnLayoutChangeListener( new View.OnLayoutChangeListener()
        {
            @Override
            public void onLayoutChange( View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom )
            {
                navigationView.removeOnLayoutChangeListener( this );

                final DeliveringUser user = DeliveringUser.getCurrentUser();

                final TextView fullName = (TextView)navigationView.findViewById( R.id.user_full_name );
                fullName.setText( user.getUsername() );

                final TextView userEmail = (TextView)navigationView.findViewById( R.id.user_email );
                userEmail.setText( user.getEmail() );
            }
        } );

        if( savedInstanceState != null )
        {
            mDeliveriesFragment = (DeliveriesFragment)getSupportFragmentManager()
                    .getFragment( savedInstanceState, DeliveriesFragment.FRAGMENT_TAG );
        }
    }

    @Override
    protected void onSaveInstanceState( Bundle outState )
    {
        super.onSaveInstanceState( outState );

        getSupportFragmentManager().putFragment( outState, DeliveriesFragment.FRAGMENT_TAG, mDeliveriesFragment );
    }

    @Override
    public void onBackPressed()
    {
        final DrawerLayout drawer = (DrawerLayout)findViewById( R.id.drawer_layout );
        if( drawer.isDrawerOpen( GravityCompat.START ) )
        {
            drawer.closeDrawer( GravityCompat.START );
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        getMenuInflater().inflate( R.menu.main, menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        switch( item.getItemId() )
        {
            case R.id.action_logout:
                logout();
                return true;

            case R.id.action_settings:
                Delivering.toast( "Sorry, no settings yet." );
                return true;

            default:
                return super.onOptionsItemSelected( item );
        }
    }

    @Override
    public boolean onNavigationItemSelected( MenuItem item )
    {
        switch( item.getItemId() )
        {
            case R.id.shifts:
                Delivering.toast( "Sorry, no shifts yet." );
                closeDrawer();
                return true;

            case R.id.deliveries:
                openDeliveries();
                closeDrawer();
                return true;

            default:
                return false;
        }
    }

    private void logout()
    {
        DeliveringUser.logOutInBackground( new LogOutCallback()
        {
            @Override
            public void done( ParseException ex )
            {
                if( ex == null )
                {
                    startActivity( new Intent( MainActivity.this, LoginActivity.class ) );
                    finish();
                }
                else
                {
                    Delivering.log( "Could not logout.", ex );
                    Delivering.toast( "Logout failed. Try again." );
                }
            }
        } );
    }

    private void openDeliveries()
    {
        final Fragment fragment = getSupportFragmentManager().findFragmentById( R.id.main_content );
        if( fragment != null && fragment == mDeliveriesFragment )
        {
            return;
        }

        if( mDeliveriesFragment == null )
        {
            mDeliveriesFragment = DeliveriesFragment.newInstance();
        }
        getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack( null )
                .replace( R.id.main_content, mDeliveriesFragment )
                .commit();
    }

    public void closeDrawer()
    {
        final DrawerLayout drawer = (DrawerLayout)findViewById( R.id.drawer_layout );
        drawer.closeDrawer( GravityCompat.START );
    }
}
