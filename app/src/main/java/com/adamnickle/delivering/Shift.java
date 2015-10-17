package com.adamnickle.delivering;

import com.parse.ParseACL;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Date;


@ParseClassName( "Shift" )
public class Shift extends ParseObject
{
    public static final String DELIVERER = "deliverer";
    public static final String START = "start";
    public static final String END = "end";

    public static final String CREATED_AT = "createdAt";
    public static final String UPDATED_AT = "updatedAt";

    public static Shift createShift( Deliverer deliverer )
    {
        final Shift shift = new Shift();
        shift.setDeliverer( deliverer );
        shift.setACL( new ParseACL( deliverer ) );
        return shift;
    }

    public static ParseQuery<Shift> createQuery()
    {
        return new ParseQuery<>( Shift.class );
    }

    public static Shift getCurrentShift()
    {
        try
        {
            return Shift.createQuery().whereDoesNotExist( END ).getFirst();
        }
        catch( ParseException ex )
        {
            Delivering.log( "Could not retrieve current Shift.", ex );
        }
        return null;
    }

    public boolean isCompleted()
    {
        return getStart() != null && getEnd() != null;
    }

    public boolean isInProgress()
    {
        return !isCompleted() && getStart() != null;
    }

    public void setDeliverer( Deliverer deliverer )
    {
        put( DELIVERER, deliverer );
    }

    public Deliverer getDeliverer()
    {
        return (Deliverer)get( DELIVERER );
    }

    public void setStart( Date start )
    {
        put( START, start );
    }

    public Date getStart()
    {
        return getDate( START );
    }

    public void setEnd( Date end )
    {
        put( END, end );
    }

    public Date getEnd()
    {
        return getDate( END );
    }
}
