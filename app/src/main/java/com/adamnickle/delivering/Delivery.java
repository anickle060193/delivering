package com.adamnickle.delivering;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Date;


@ParseClassName( "Delivery" )
public class Delivery extends ParseObject
{
    public static final String NAME = "name";
    public static final String DELIVERED_AT = "delivered_at";
    public static final String CREATED_AT = "createdAt";
    public static final String UPDATED_AT = "updatedAt";

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

    public void setDeliveredAt( Date date )
    {
        put( DELIVERED_AT, date );
    }

    public Date getDeliveredAt()
    {
        return getDate( DELIVERED_AT );
    }
}
