package com.adamnickle.delivering;

import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public abstract class ParseObjectArrayAdapter<T extends ParseObject, V extends ParseObjectArrayAdapter.ViewHolder> extends ArrayRecyclerAdapter<T, ParseObjectArrayAdapter.ViewHolder>
{
    public static final int END_ITEM_TYPE = -1;

    public static abstract class ViewHolder extends RecyclerView.ViewHolder
    {
        public ViewHolder( View itemView )
        {
            super( itemView );
        }

        public final <T extends View> T findViewById( @IdRes int id)
        {
            //noinspection unchecked
            return (T)itemView.findViewById( id );
        }
    }

    public static abstract class QueryListener
    {
        public void onQueryStarted() { }
        public void onQueryEnded( boolean successful ) { }
    }

    public static abstract class ParseQueryFactory<Q extends ParseObject>
    {
        public abstract ParseQuery<Q> getQuery();
    }

    private class EndViewHolder extends ViewHolder
    {
        public final TextView LoadMoreItems;

        public EndViewHolder( View itemView )
        {
            super( itemView );

            LoadMoreItems = (TextView)itemView.findViewById( R.id.parse_adapter_end_item_load_more_items );
        }

        public void enable()
        {
            LoadMoreItems.setText( "Load more items..." );
            LoadMoreItems.setEnabled( true );
        }

        public void disable()
        {
            LoadMoreItems.setText( "Querying..." );
            LoadMoreItems.setEnabled( false );
        }
    }

    private final Set<QueryListener> mListeners = new HashSet<>();
    private final Set<RecyclerView> mRecyclerViews = new HashSet<>();
    private final boolean mQueryingEnabled;
    private final ParseQueryFactory<T> mQueryFactory;
    private final int mItemsToLoadPerQuery;

    private EndViewHolder mLastEndViewHolder;

    private int mSkipCount = 0;
    private boolean mQuerying = false;
    private boolean mHasMoreItems = true;

    public ParseObjectArrayAdapter( ParseQueryFactory<T> factory, int itemsToLoadPerQuery )
    {
        mQueryingEnabled = true;
        mQueryFactory = factory;
        mItemsToLoadPerQuery = itemsToLoadPerQuery;
    }

    public ParseObjectArrayAdapter( ParseQueryFactory<T> factory )
    {
        this( factory, 10 );
    }

    public ParseObjectArrayAdapter( Collection<T> items )
    {
        super( items );
        mQueryingEnabled = false;
        mQueryFactory = null;
        mItemsToLoadPerQuery = 0;
    }

    public void addOnQueryListener( QueryListener listener )
    {
        if( mListeners.contains( listener ) )
        {
            throw new IllegalStateException( QueryListener.class.getSimpleName() + listener + " is already registered." );
        }
        mListeners.add( listener );
    }

    public void removeOnQueryListener( QueryListener listener )
    {
        if( !mListeners.contains( listener ) )
        {
            throw new IllegalStateException( QueryListener.class.getSimpleName() + listener + " was never registered." );
        }
        mListeners.remove( listener );
    }

    @Override
    public void onAttachedToRecyclerView( RecyclerView recyclerView )
    {
        super.onAttachedToRecyclerView( recyclerView );

        mRecyclerViews.add( recyclerView );
        if( mQueryingEnabled )
        {
            queryForMore();
        }
    }

    @Override
    public void onDetachedFromRecyclerView( RecyclerView recyclerView )
    {
        super.onDetachedFromRecyclerView( recyclerView );

        mRecyclerViews.remove( recyclerView );
    }

    public void notifyItemUpdated( T object )
    {
        final int index = indexOf( object );
        notifyItemChanged( index );
    }

    @Override
    public void add( int location, T object )
    {
        super.add( location, object );

        for( RecyclerView recyclerView : mRecyclerViews )
        {
            recyclerView.scrollToPosition( location );
        }
    }

    public void refresh()
    {
        if( mQueryingEnabled && !mQuerying )
        {
            mSkipCount = 0;
            mHasMoreItems = true;
            clear();
            queryForMore();
        }
    }

    public boolean hasMoreToQuery()
    {
        return mQueryingEnabled && mHasMoreItems;
    }

    public void queryForMore()
    {
        if( !mQueryingEnabled || mQuerying || !mHasMoreItems )
        {
            return;
        }
        mQuerying = true;
        if( mLastEndViewHolder != null )
        {
            mLastEndViewHolder.disable();
        }
        for( QueryListener listener : mListeners )
        {
            listener.onQueryStarted();
        }
        mQueryFactory
                .getQuery()
                .setSkip( mSkipCount )
                .setLimit( mItemsToLoadPerQuery )
                .findInBackground( new FindCallback<T>()
                {
                    @Override
                    public void done( List<T> objects, ParseException ex )
                    {
                        if( ex == null )
                        {
                            final int size = objects.size();

                            ParseObjectArrayAdapter.this.addAll( objects );

                            if( size < mItemsToLoadPerQuery )
                            {
                                mHasMoreItems = false;
                                notifyDataSetChanged();
                            }
                            for( RecyclerView recyclerView : mRecyclerViews )
                            {
                                recyclerView.scrollToPosition( mSkipCount );
                            }

                            mSkipCount += size;
                        }
                        else
                        {
                            Delivering.log( "An error occurred querying more object.", ex );
                            Delivering.oops( ex );
                        }

                        mQuerying = false;
                        if( mLastEndViewHolder != null )
                        {
                            mLastEndViewHolder.enable();
                        }
                        for( QueryListener listener : mListeners )
                        {
                            listener.onQueryEnded( ex == null );
                        }
                    }
                } );
    }

    @Override
    public final int getItemCount()
    {
        if( hasMoreToQuery() )
        {
            return getParseObjectItemCount() + 1;
        }
        else
        {
            return getParseObjectItemCount();
        }
    }

    public int getParseObjectItemCount()
    {
        return size();
    }

    @Override
    public final int getItemViewType( int position )
    {
        if( hasMoreToQuery() && position == getParseObjectItemCount() )
        {
            return END_ITEM_TYPE;
        }
        else
        {
            return getParseObjectItemViewType( position );
        }
    }

    public int getParseObjectItemViewType( int position )
    {
        return 0;
    }

    @Override
    public final ViewHolder onCreateViewHolder( ViewGroup parent, int viewType )
    {
        if( viewType == END_ITEM_TYPE )
        {
            final View view = LayoutInflater
                    .from( parent.getContext() )
                    .inflate( R.layout.parse_adapter_end_item_layout, parent, false );
            return new EndViewHolder( view );
        }
        else
        {
            return onCreateParseObjectViewHolder( parent, viewType );
        }
    }

    public abstract V onCreateParseObjectViewHolder( ViewGroup parent, int viewType );

    @Override
    public final void onBindViewHolder( ViewHolder holder, int position )
    {
        if( holder.getItemViewType() == END_ITEM_TYPE )
        {
            //noinspection unchecked
            holder.itemView.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v )
                {
                    queryForMore();
                }
            } );
        }
        else
        {
            //noinspection unchecked
            onBindParseObjectViewHolder( (V)holder, position );
        }
    }

    public abstract void onBindParseObjectViewHolder( V holder, int position );

    @Override
    @CallSuper
    public void onViewAttachedToWindow( ViewHolder holder )
    {
        if( EndViewHolder.class.isAssignableFrom( holder.getClass() ) )
        {
            //noinspection unchecked
            mLastEndViewHolder = (EndViewHolder)holder;
            if( mQuerying )
            {
                mLastEndViewHolder.disable();
            }
            else
            {
                mLastEndViewHolder.enable();
            }
        }
    }

    @Override
    @CallSuper
    public void onViewDetachedFromWindow( ViewHolder holder )
    {
        if( holder == mLastEndViewHolder )
        {
            mLastEndViewHolder = null;
        }
    }
}
