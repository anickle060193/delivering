package com.adamnickle.delivering;

import android.util.Patterns;

import com.parse.ParseClassName;
import com.parse.ParseUser;


@ParseClassName( "_User" )
public class Deliverer extends ParseUser
{
    public static Deliverer getCurrentUser()
    {
        return (Deliverer)ParseUser.getCurrentUser();
    }

    public static Deliverer create( String email, String password )
    {
        final Deliverer user = new Deliverer();
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
