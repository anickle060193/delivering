package com.adamnickle.delivering;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;


@ParseClassName( "Delivery" )
public class Delivery extends ParseObject
{
    public static final String NAME = "name";

    public static Delivery create( String name )
    {
        final Delivery delivery = new Delivery();
        delivery.setName( name );
        return delivery;
    }

    public static ParseQuery<Delivery> createQuery()
    {
        return new ParseQuery<>( Delivery.class );
    }

    public void setName( String name )
    {
        put( NAME, name );
    }

    public String getName()
    {
        return getString( NAME );
    }
}
