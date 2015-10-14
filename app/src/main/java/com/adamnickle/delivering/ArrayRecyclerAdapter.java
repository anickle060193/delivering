package com.adamnickle.delivering;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by Adam Nickle on 4/17/2015.
 */
public abstract class ArrayRecyclerAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> implements List<T>
{
    protected final List<T> mElements;

    public ArrayRecyclerAdapter( Collection<T> elements )
    {
        mElements = Collections.synchronizedList( elements == null ? new ArrayList<T>() : new ArrayList<>( elements ) );
    }

    public ArrayRecyclerAdapter()
    {
        this( null );
    }

    @Override
    public int getItemCount()
    {
        return size();
    }

    @Override
    public void add( int location, T object )
    {
        mElements.add( location, object );
        notifyItemInserted( location );
    }

    @Override
    public boolean add( T object )
    {
        final int size = mElements.size();
        final boolean added = mElements.add( object );
        if( added )
        {
            notifyItemInserted( size );
        }
        return added;
    }

    @Override
    public boolean addAll( int location, @NonNull Collection<? extends T> collection )
    {
        final boolean added = mElements.addAll( location, collection );
        if( added )
        {
            notifyItemRangeInserted( location, collection.size() );
        }
        return added;
    }

    @Override
    public boolean addAll( @NonNull Collection<? extends T> collection )
    {
        final int size = mElements.size();
        final boolean added = mElements.addAll( collection );
        if( added )
        {
            notifyItemRangeInserted( size, collection.size() );
        }
        return added;
    }

    @Override
    public void clear()
    {
        final int size = mElements.size();
        mElements.clear();
        notifyItemRangeRemoved( 0, size );
    }

    @Override
    public boolean contains( Object object )
    {
        return mElements.contains( object );
    }

    @Override
    public boolean containsAll( @NonNull Collection<?> collection )
    {
        return mElements.containsAll( collection );
    }

    @Override
    public T get( int location )
    {
        return mElements.get( location );
    }

    @Override
    public int indexOf( Object object )
    {
        return mElements.indexOf( object );
    }

    @Override
    public boolean isEmpty()
    {
        return mElements.isEmpty();
    }

    @NonNull
    @Override
    public Iterator<T> iterator()
    {
        return new Iterator<T>()
        {
            final Iterator<T> iterator = mElements.iterator();

            @Override
            public boolean hasNext()
            {
                return iterator.hasNext();
            }

            @Override
            public T next()
            {
                return iterator.next();
            }

            @Override
            @Deprecated
            public void remove()
            {
                throw new UnsupportedOperationException( "ArrayRecyclerAdapter.Iterator does not support remove()." );
            }
        };
    }

    @Override
    public int lastIndexOf( Object object )
    {
        return mElements.lastIndexOf( object );
    }

    @NonNull
    @Override
    public ListIterator<T> listIterator()
    {
        return listIterator( 0 );
    }

    @NonNull
    @Override
    public ListIterator<T> listIterator( final int location )
    {
        return new ListIterator<T>()
        {
            final ListIterator<T> listIterator = mElements.listIterator( location );

            @Override
            @Deprecated
            public void add( T object )
            {
                throw new UnsupportedOperationException( "ArrayRecyclerAdapter.ListIterator does not support add()." );
            }

            @Override
            public boolean hasNext()
            {
                return listIterator.hasNext();
            }

            @Override
            public boolean hasPrevious()
            {
                return listIterator.hasPrevious();
            }

            @Override
            public T next()
            {
                return listIterator.next();
            }

            @Override
            public int nextIndex()
            {
                return listIterator.nextIndex();
            }

            @Override
            public T previous()
            {
                return listIterator.previous();
            }

            @Override
            public int previousIndex()
            {
                return listIterator.previousIndex();
            }

            @Override
            @Deprecated
            public void remove()
            {
                throw new UnsupportedOperationException( "ArrayRecyclerAdapter.ListIterator does not support remove()." );
            }

            @Override
            @Deprecated
            public void set( T object )
            {
                throw new UnsupportedOperationException( "ArrayRecyclerAdapter.ListIterator does not support set()." );
            }
        };
    }

    @Override
    public T remove( int location )
    {
        final T object = mElements.remove( location );
        notifyItemRemoved( location );
        return object;
    }

    @Override
    public boolean remove( Object object )
    {
        //noinspection SuspiciousMethodCalls
        final int index = mElements.indexOf( object );
        if( index != -1 )
        {
            mElements.remove( index );
            notifyItemRemoved( index );
            return true;
        }
        return false;
    }

    @Override
    public boolean removeAll( @NonNull Collection<?> collection )
    {
        boolean modified = false;
        for( Object obj : collection )
        {
            if( remove( obj ) )
            {
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean retainAll( @NonNull Collection<?> collection )
    {
        boolean modified = false;
        int i = 0;
        while( i < mElements.size() )
        {
            if( !collection.contains( mElements.get( i ) ) )
            {
                remove( i );
                modified = true;
            }
            else
            {
                i++;
            }
        }
        return modified;
    }

    @Override
    public T set( int location, T object )
    {
        final T setted = mElements.set( location, object );
        notifyItemChanged( location );
        return setted;
    }

    @Override
    public int size()
    {
        return mElements.size();
    }

    @NonNull
    @Override
    public List<T> subList( int start, int end )
    {
        return mElements.subList( start, end );
    }

    @NonNull
    @Override
    public Object[] toArray()
    {
        return mElements.toArray();
    }

    @NonNull
    @Override
    public <T1> T1[] toArray( @NonNull T1[] array )
    {
        //noinspection SuspiciousToArrayCall
        return mElements.toArray( array );
    }
}