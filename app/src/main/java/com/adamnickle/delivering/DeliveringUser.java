package com.adamnickle.delivering;

import android.util.Patterns;

import com.parse.ParseClassName;
import com.parse.ParseUser;


@ParseClassName( "_User" )
public class DeliveringUser extends ParseUser
{
    public DeliveringUser()
    {
    }

    public static DeliveringUser getCurrentUser()
    {
        return (DeliveringUser)ParseUser.getCurrentUser();
    }

    public static DeliveringUser create( String email, String password )
    {
        final DeliveringUser user = new DeliveringUser();
        user.setUsername( email );
        user.setEmail( email );
        user.setPassword( password );
        return user;
    }

    public static boolean isEmailValid( String email )
    {
        return Patterns.EMAIL_ADDRESS.matcher( email ).matches();
    }

    public static boolean isPasswordValid( String password )
    {
        return password != null && password.length() > 4;
    }
}
