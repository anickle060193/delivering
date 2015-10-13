package com.adamnickle.delivering;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;


public class LoginActivity extends AppCompatActivity
{
    private static final int REQUEST_REGISTER_USER = 1001;

    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private boolean mLoggingIn;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        if( DeliveringUser.getCurrentUser() != null )
        {
            loginDone();
        }

        setContentView( R.layout.activity_login );
        setTitle( R.string.title_activity_login );

        mEmailView = (AutoCompleteTextView)findViewById( R.id.email );
        populateAutoComplete();

        mPasswordView = (EditText)findViewById( R.id.password );
        mPasswordView.setOnEditorActionListener( new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction( TextView textView, int id, KeyEvent keyEvent )
            {
                if( id == R.id.login || id == EditorInfo.IME_NULL )
                {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        } );

        findViewById( R.id.sign_in_button ).setOnClickListener( new OnClickListener()
        {
            @Override
            public void onClick( View view )
            {
                attemptLogin();
            }
        } );

        mLoginFormView = findViewById( R.id.login_form );
        mProgressView = findViewById( R.id.login_progress );
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

    private void populateAutoComplete()
    {
        //TODO Save past entered usernames
    }

    private void loginDone()
    {
        startActivity( new Intent( this, MainActivity.class ) );
        finish();
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
        if( !possiblyRegistering && !DeliveringUser.isPasswordValid( password ) )
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
        else if( !DeliveringUser.isEmailValid( email ) )
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
                DeliveringUser.logInInBackground( email, password, new LogInCallback()
                {
                    @Override
                    public void done( ParseUser user, ParseException ex )
                    {
                        if( ex == null )
                        {
                            loginDone();
                        }
                        else
                        {
                            mLoggingIn = false;
                            showProgress( false );

                            Delivering.log( "Could not login user.", ex );
                            mPasswordView.setError( getString( R.string.error_incorrect_password ) );
                            mPasswordView.requestFocus();
                        }
                    }
                } );
            }
        }
    }

    private void showProgress( final boolean show )
    {
        final int shortAnimTime = getResources().getInteger( android.R.integer.config_shortAnimTime );

        mLoginFormView.setVisibility( show ? View.GONE : View.VISIBLE );
        mLoginFormView.animate()
                .setDuration( shortAnimTime )
                .alpha( show ? 0 : 1 )
                .setListener( new AnimatorListenerAdapter()
                {
                    @Override
                    public void onAnimationEnd( Animator animation )
                    {
                        mLoginFormView.setVisibility( show ? View.GONE : View.VISIBLE );
                    }
                } );

        mProgressView.setVisibility( show ? View.VISIBLE : View.GONE );
        mProgressView.animate()
                .setDuration( shortAnimTime )
                .alpha( show ? 1 : 0 )
                .setListener( new AnimatorListenerAdapter()
                {
                    @Override
                    public void onAnimationEnd( Animator animation )
                    {
                        mProgressView.setVisibility( show ? View.VISIBLE : View.GONE );
                    }
                } );
    }
}

