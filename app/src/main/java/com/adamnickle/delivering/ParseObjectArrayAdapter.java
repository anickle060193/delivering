package com.adamnickle.delivering;

import android.support.annotation.CallSuper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;


public abstract class ParseObjectArrayAdapter<T extends ParseObject, V extends ParseObjectArrayAdapter.ViewHolder> extends ArrayRecyclerAdapter<T, ParseObjectArrayAdapter.ViewHolder>
{
    public static final int END_ITEM_TYPE = -1;

    public static abstract class ViewHolder extends RecyclerView.ViewHolder
    {
        public ViewHolder( View itemView )
        {
            super( itemView );
        }
    }

    private class EndViewHolder extends ViewHolder
    {
        public final TextView LoadMoreItems;

        public EndViewHolder( View itemView )
        {
            super( itemView );

            LoadMoreItems = (TextView)itemView.findViewById( R.id.load_more_items );
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

    public static abstract class ParseQueryFactory<Q extends ParseObject>
    {
        public abstract ParseQuery<Q> getQuery();
    }

    private final ParseQueryFactory<T> mQueryFactory;
    private final int mItemsToLoadPerQuery;

    private EndViewHolder mLastEndViewHolder;

    private int mSkipCount = 0;
    private boolean mQuerying = false;
    private boolean mHasMoreItems = true;

    public ParseObjectArrayAdapter( ParseQueryFactory<T> factory )
    {
        this( factory, 10 );
    }

    public ParseObjectArrayAdapter( ParseQueryFactory<T> factory, int itemsToLoadPerQuery )
    {
        mQueryFactory = factory;
        mItemsToLoadPerQuery = itemsToLoadPerQuery;
    }

    @Override
    public void onAttachedToRecyclerView( RecyclerView recyclerView )
    {
        super.onAttachedToRecyclerView( recyclerView );

        startQuery();
    }

    private void startQuery()
    {
        if( mQuerying || !mHasMoreItems )
        {
            return;
        }
        mQuerying = true;
        if( mLastEndViewHolder != null )
        {
            mLastEndViewHolder.disable();
        }
        mQueryFactory
                .getQuery()
                .setSkip( mSkipCount )
                .setLimit( 10 )
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

                            mSkipCount += size;
                        }
                        else
                        {
                            Delivering.log( "An error occurred querying more object.", ex );
                            Delivering.toast( "More items could not be loaded at this time. Try again later." );
                        }

                        mQuerying = false;
                        if( mLastEndViewHolder != null )
                        {
                            mLastEndViewHolder.enable();
                        }
                    }
                } );
    }

    @Override
    public final int getItemCount()
    {
        if( mHasMoreItems )
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
        if( mHasMoreItems && position == getParseObjectItemCount() )
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
                    .inflate( R.layout.parse_object_array_adapter_end_item_layout, parent, false );
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
                    startQuery();
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
