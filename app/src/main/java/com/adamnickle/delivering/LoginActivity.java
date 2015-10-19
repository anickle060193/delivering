package com.adamnickle.delivering;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;


public class LoginActivity extends AppCompatActivity
{
    private static final int REQUEST_REGISTER_USER = 1001;

    private EditText mEmailView;
    private EditText mPasswordView;

    private AlertDialog mLoadingDialog;

    private boolean mLoggingIn;

    private boolean mDeliveriesFetched;
    private boolean mShiftsFetched;

    private boolean mSuccessful = true;
    private boolean mDone;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        if( Deliverer.getCurrentUser() != null )
        {
            loginDone();
        }

        setContentView( R.layout.activity_login );

        if( BuildConfig.DEBUG )
        {
            getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
        }

        setTitle( R.string.title_activity_login );

        mEmailView = (EditText)findViewById( R.id.login_email );

        mPasswordView = (EditText)findViewById( R.id.login_password );
        mPasswordView.setOnEditorActionListener( new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction( TextView textView, int id, KeyEvent keyEvent )
            {
                if( id == EditorInfo.IME_ACTION_DONE )
                {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        } );

        findViewById( R.id.login_sign_in_button ).setOnClickListener( new OnClickListener()
        {
            @Override
            public void onClick( View view )
            {
                attemptLogin();
            }
        } );
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data )
    {
        if( requestCode == REQUEST_REGISTER_USER )
        {
            if( resultCode == Activity.RESULT_OK )
            {
                loginDone();
            }
        }
        else
        {
            super.onActivityResult( requestCode, resultCode, data );
        }
    }

    private void loginDone()
    {
        startActivity( new Intent( this, MainActivity.class ) );
        finish();
    }

    private void finishLogin()
    {
        new AlertDialog.Builder( this )
                .setMessage( "Fetching data..." )
                .setCancelable( false )
                .show();

        final Deliverer deliverer = Deliverer.getCurrentUser();

        ParseQuery.getQuery( Delivery.class )
                .whereEqualTo( Delivery.DELIVERER, deliverer )
                .findInBackground( new FindCallback<Delivery>()
                {
                    @Override
                    public void done( List<Delivery> objects, ParseException e )
                    {
                        if( e == null )
                        {
                            Delivery.pinAllInBackground( objects, new SaveCallback()
                            {
                                @Override
                                public void done( ParseException ex )
                                {
                                    mDeliveriesFetched = true;
                                    if( ex != null )
                                    {
                                        Delivering.log( "Could not pin all Deliveries", ex );
                                        mSuccessful = false;
                                    }
                                    checkForFinish();
                                }
                            } );
                        }
                        else
                        {
                            Delivering.log( "Could not find all Deliveries.", e );
                            mDeliveriesFetched = true;
                            mSuccessful = false;
                            checkForFinish();
                        }
                    }
                } );
        ParseQuery.getQuery( Shift.class )
                .whereEqualTo( Shift.DELIVERER, deliverer )
                .findInBackground( new FindCallback<Shift>()
                {
                    @Override
                    public void done( List<Shift> objects, ParseException e )
                    {
                        if( e == null )
                        {
                            Shift.pinAllInBackground( objects, new SaveCallback()
                            {
                                @Override
                                public void done( ParseException ex )
                                {
                                    mShiftsFetched = true;
                                    if( ex != null )
                                    {
                                        Delivering.log( "Could not pin all Shifts.", ex );
                                        mSuccessful = false;
                                    }
                                    checkForFinish();
                                }
                            } );
                        }
                        else
                        {
                            Delivering.log( "Could not find all Shifts.", e );
                            mShiftsFetched = true;
                            mSuccessful = false;
                            checkForFinish();
                        }
                    }
                } );
    }

    private synchronized void checkForFinish()
    {
        if( !mDone && mDeliveriesFetched && mShiftsFetched )
        {
            mDone = true;

            if( !mSuccessful )
            {
                Delivering.toast( "There was a problem fetching your data.\nSome may be unavailable until you login again." );
            }

            loginDone();
        }
    }

    private void attemptLogin()
    {
        if( mLoggingIn )
        {
            return;
        }

        // Reset errors.
        mEmailView.setError( null );
        mPasswordView.setError( null );

        // Store values at the time of the login attempt.
        final String email = mEmailView.getText().toString();
        final String password = mPasswordView.getText().toString();

        final boolean possiblyRegistering = TextUtils.isEmpty( password );

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if( !possiblyRegistering && !Deliverer.isPasswordValid( password ) )
        {
            mPasswordView.setError( getString( R.string.error_invalid_password ) );
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if( TextUtils.isEmpty( email ) )
        {
            mEmailView.setError( getString( R.string.error_field_required ) );
            focusView = mEmailView;
            cancel = true;
        }
        else if( !Deliverer.isEmailValid( email ) )
        {
            mEmailView.setError( getString( R.string.error_invalid_email ) );
            focusView = mEmailView;
            cancel = true;
        }

        if( cancel )
        {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        }
        else
        {
            if( possiblyRegistering )
            {
                final Intent intent = new Intent( this, RegisterActivity.class )
                        .putExtra( RegisterActivity.EXTRA_USERNAME, email );
                startActivityForResult( intent, REQUEST_REGISTER_USER );
            }
            else
            {
                // Show a progress spinner, and kick off a background task to
                // perform the user login attempt.
                showProgress( true );

                mLoggingIn = true;
                Deliverer.logInInBackground( email, password, new LogInCallback()
                {
                    @Override
                    public void done( ParseUser user, ParseException ex )
                    {
                        if( ex == null )
                        {
                            finishLogin();
                            return;
                        }
                        mLoggingIn = false;
                        showProgress( false );

                        switch( ex.getCode() )
                        {
                            case ParseException.EMAIL_MISSING:
                            case ParseException.USERNAME_MISSING:
                                mEmailView.setError( "Email address missing" );
                                mEmailView.requestFocus();
                                break;

                            case ParseException.INVALID_EMAIL_ADDRESS:
                                mEmailView.setError( "Invalid email address" );
                                mEmailView.requestFocus();
                                break;

                            case ParseException.PASSWORD_MISSING:
                                mPasswordView.setError( "Password missing" );
                                mPasswordView.requestFocus();
                                break;

                            case ParseException.OBJECT_NOT_FOUND:
                            case ParseException.EMAIL_NOT_FOUND:
                                Delivering.toast( "Invalid login" );
                                mEmailView.requestFocus();
                                break;

                            default:
                                Delivering.oops( ex );
                                mEmailView.requestFocus();
                                break;
                        }

                        Delivering.log( "Could not login user.", ex );
                    }
                } );
            }
        }
    }

    private void showProgress( boolean show )
    {
        if( show )
        {
            mLoadingDialog = Dialogs.showLoading( this );
        }
        else
        {
            mLoadingDialog.dismiss();
        }
    }
}

