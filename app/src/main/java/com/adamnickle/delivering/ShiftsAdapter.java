package com.adamnickle.delivering;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.DeleteCallback;
import com.parse.ParseException;

import java.util.Date;


public class ShiftsAdapter extends ParseObjectArrayAdapter<Shift, ShiftsAdapter.ShiftViewHolder>
{
    public interface ShiftsAdapterListener
    {
        void onShiftClicked( Shift shift );
    }

    private final Context mContext;
    private final ItemTouchHelper mTouchHelper;

    private ShiftsAdapterListener mListener;

    public ShiftsAdapter( Context context, ParseQueryFactory<Shift> queryFactory )
    {
        super( queryFactory );

        mContext = context;
        mTouchHelper = new ItemTouchHelper( mItemTouchHelperCallback );
    }

    public void setListener( ShiftsAdapterListener listener )
    {
        mListener = listener;
    }

    @Override
    public ShiftViewHolder onCreateParseObjectViewHolder( ViewGroup parent, int viewType )
    {
        final View view = LayoutInflater.from( parent.getContext() ).inflate( R.layout.shift_item_layout, parent, false );
        return new ShiftViewHolder( view );
    }

    @Override
    public void onBindParseObjectViewHolder( ShiftViewHolder holder, int position )
    {
        holder.Shift = get( position );
        holder.update();
    }

    @Override
    public void onAttachedToRecyclerView( RecyclerView recyclerView )
    {
        super.onAttachedToRecyclerView( recyclerView );

        mTouchHelper.attachToRecyclerView( recyclerView );
    }

    final ItemTouchHelper.SimpleCallback mItemTouchHelperCallback = new ItemTouchHelper.SimpleCallback( 0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT )
    {
        private final Object LOCK = new Object();

        private Shift mLastRemoved;
        private int mLastRemovedIndex;

        @Override
        public boolean onMove( RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target )
        {
            return false;
        }

        @Override
        public int getSwipeDirs( RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder )
        {
            if( viewHolder instanceof ShiftViewHolder )
            {
                return super.getSwipeDirs( recyclerView, viewHolder );
            }
            else
            {
                return 0;
            }
        }

        @Override
        public void onSwiped( RecyclerView.ViewHolder viewHolder, int direction )
        {
            if( viewHolder instanceof ShiftViewHolder )
            {
                synchronized( LOCK )
                {
                    if( mLastRemoved != null )
                    {
                        mLastRemoved.unpinInBackground( new DeleteCallback()
                        {
                            @Override
                            public void done( ParseException e )
                            {
                                if( e == null )
                                {
                                    mLastRemoved.deleteEventually();
                                }
                                else
                                {
                                    Delivering.log( "Could not delete Shift.", e );
                                }
                            }
                        } );
                    }

                    mLastRemoved = ( (ShiftViewHolder)viewHolder ).Shift;
                    mLastRemovedIndex = indexOf( mLastRemoved );
                    remove( mLastRemovedIndex );
                }
                Snackbar.make( viewHolder.itemView, "Shift deleted.", Snackbar.LENGTH_LONG )
                        .setAction( "Undo", new View.OnClickListener()
                        {
                            @Override
                            public void onClick( View v )
                            {
                                synchronized( LOCK )
                                {
                                    if( mLastRemoved != null )
                                    {
                                        add( mLastRemovedIndex, mLastRemoved );
                                    }
                                }
                            }
                        } )
                        .show();
            }
        }
    };

    private void onClockInOutClick( final ShiftViewHolder holder )
    {
        ShiftDialogs.clockInOut( mContext, holder.Shift, new ShiftDialogs.ShiftStatusListener()
        {
            @Override
            public void onShiftClockIn()
            {
                holder.Shift.setStart( new Date() );
                holder.Shift.saveEventually();
                holder.update();
            }

            @Override
            public void onShiftClockOut()
            {
                holder.Shift.setEnd( new Date() );
                holder.Shift.saveEventually();
                holder.update();
            }
        } );
    }

    public class ShiftViewHolder extends ParseObjectArrayAdapter.ViewHolder
    {
        public Shift Shift;
        public final TextView ShiftDate;
        public final TextView ClockInTime;
        public final TextView ClockOutTime;
        public final View ClockInOut;

        public ShiftViewHolder( View itemView )
        {
            super( itemView );

            ShiftDate = findViewById( R.id.shift_item_date );
            ClockInTime = findViewById( R.id.shift_item_clock_in_time );
            ClockOutTime = findViewById( R.id.shift_item_clock_out_time );
            ClockInOut = findViewById( R.id.shift_item_clock_in_out );

            itemView.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v )
                {
                    if( mListener != null )
                    {
                        mListener.onShiftClicked( ShiftViewHolder.this.Shift );
                    }
                }
            } );
            ClockInOut.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v )
                {
                    onClockInOutClick( ShiftViewHolder.this );
                }
            } );
        }

        public void update()
        {
            if( Shift != null )
            {
                final Date start = Shift.getStart();
                final Date end = Shift.getEnd();
                if( start == null )
                {
                    final Date createdAt = Shift.getCreatedAt();
                    ShiftDate.setText( Utilities.DATE_FORMAT.format( createdAt ) );
                    ClockInTime.setText( "[Not clocked-in yet]" );
                }
                else
                {
                    ShiftDate.setText( Utilities.DATE_FORMAT.format( start ) );
                    ClockInTime.setText( Utilities.SHORT_TIME_FORMAT.format( start ) );
                }
                if( end == null )
                {
                    ClockOutTime.setText( "[Not clocked-out yet]" );
                }
                else
                {
                    ClockOutTime.setText( Utilities.SHORT_TIME_FORMAT.format( end ) );
                }
            }
        }
    }
}
