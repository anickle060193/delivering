package com.adamnickle.delivering;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.SignUpCallback;

public class RegisterActivity extends AppCompatActivity
{
    public static final String EXTRA_USERNAME = BuildConfig.APPLICATION_ID + ".extra.username";

    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mVerifyPasswordView;
    private View mProgressView;
    private View mRegisterFormView;

    private boolean mRegistering;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_register );

        setResult( Activity.RESULT_CANCELED );

        final String username = getIntent().getStringExtra( EXTRA_USERNAME );

        mEmailView = (AutoCompleteTextView)findViewById( R.id.register_email );

        mPasswordView = (EditText)findViewById( R.id.register_password );

        mVerifyPasswordView = (EditText)findViewById( R.id.register_password_verify );
        mVerifyPasswordView.setOnEditorActionListener( new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction( TextView textView, int id, KeyEvent keyEvent )
            {
                if( id == R.id.login || id == EditorInfo.IME_NULL )
                {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        } );

        if( username != null )
        {
            mEmailView.setText( username );
            mPasswordView.requestFocus();
        }
        else
        {
            populateAutoComplete();
        }

        findViewById( R.id.register_button ).setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View view )
            {
                attemptRegister();
            }
        } );

        mRegisterFormView = findViewById( R.id.register_form );
        mProgressView = findViewById( R.id.register_progress );
    }

    private void populateAutoComplete()
    {
        //TODO Save past entered usernames
    }

    private void attemptRegister()
    {
        if( mRegistering )
        {
            return;
        }

        // Reset errors.
        mEmailView.setError( null );
        mPasswordView.setError( null );
        mVerifyPasswordView.setError( null );

        // Store values at the time of the login attempt.
        final String email = mEmailView.getText().toString();
        final String password = mPasswordView.getText().toString();
        final String verifyPassword = mVerifyPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password verification.
        if( !verifyPassword.equals( password ) )
        {
            mPasswordView.setError( getString( R.string.error_non_matching_passwords ) );
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if( TextUtils.isEmpty( password ) )
        {
            mPasswordView.setError( getString( R.string.error_field_required ) );
            focusView = mPasswordView;
            cancel = true;
        }
        else if( !Deliverer.isPasswordValid( password ) )
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
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress( true );

            mRegistering = true;
            Deliverer.create( email, password ).signUpInBackground( new SignUpCallback()
            {
                @Override
                public void done( ParseException ex )
                {
                    mRegistering = false;
                    if( ex == null )
                    {
                        setResult( Activity.RESULT_OK );
                        finish();
                    }
                    else
                    {
                        showProgress( false );
                        Delivering.log( "Could not register user.", ex );
                        mEmailView.requestFocus();
                        Delivering.toast( "Registration failed. Try again." );
                    }
                }
            } );
        }
    }

    private void showProgress( final boolean show )
    {
        final int shortAnimTime = getResources().getInteger( android.R.integer.config_shortAnimTime );

        mRegisterFormView.setVisibility( show ? View.GONE : View.VISIBLE );
        mRegisterFormView.animate()
                .setDuration( shortAnimTime )
                .alpha( show ? 0 : 1 )
                .setListener( new AnimatorListenerAdapter()
                {
                    @Override
                    public void onAnimationEnd( Animator animation )
                    {
                        mRegisterFormView.setVisibility( show ? View.GONE : View.VISIBLE );
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
