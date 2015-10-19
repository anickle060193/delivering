package com.adamnickle.delivering;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.SignUpCallback;

public class RegisterActivity extends AppCompatActivity
{
    public static final String EXTRA_USERNAME = BuildConfig.APPLICATION_ID + ".extra.username";

    private EditText mEmailView;
    private EditText mPasswordView;
    private EditText mVerifyPasswordView;

    private AlertDialog mLoadingDialog;

    private boolean mRegistering;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_register );

        if( BuildConfig.DEBUG )
        {
            getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
        }

        setResult( Activity.RESULT_CANCELED );

        final String username = getIntent().getStringExtra( EXTRA_USERNAME );

        mEmailView = (EditText)findViewById( R.id.register_email );

        mPasswordView = (EditText)findViewById( R.id.register_password );

        mVerifyPasswordView = (EditText)findViewById( R.id.register_password_verify );
        mVerifyPasswordView.setOnEditorActionListener( new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction( TextView textView, int id, KeyEvent keyEvent )
            {
                if( id == EditorInfo.IME_ACTION_DONE )
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

        findViewById( R.id.register_button ).setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View view )
            {
                attemptRegister();
            }
        } );
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
            mPasswordView.setError( "Passwords do not match" );
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if( TextUtils.isEmpty( password ) )
        {
            mPasswordView.setError( "This field is required" );
            focusView = mPasswordView;
            cancel = true;
        }
        else if( !Deliverer.isPasswordValid( password ) )
        {
            mPasswordView.setError( "Invalid password" );
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if( TextUtils.isEmpty( email ) )
        {
            mEmailView.setError( "This field is required" );
            focusView = mEmailView;
            cancel = true;
        }
        else if( !Deliverer.isEmailValid( email ) )
        {
            mEmailView.setError( "Invalid email" );
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
                    if( ex == null )
                    {
                        setResult( Activity.RESULT_OK );
                        finish();
                    }
                    else
                    {
                        mRegistering = false;
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

                            case ParseException.EMAIL_TAKEN:
                            case ParseException.USERNAME_TAKEN:
                                mEmailView.setError( "Email address already in use" );
                                mEmailView.requestFocus();
                                break;

                            case ParseException.PASSWORD_MISSING:
                                mPasswordView.setError( "Password missing" );
                                mPasswordView.requestFocus();
                                break;

                            default:
                                Delivering.oops( ex );
                                mEmailView.requestFocus();
                                break;
                        }

                        Delivering.log( "Could not register user.", ex );
                    }
                }
            } );
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
