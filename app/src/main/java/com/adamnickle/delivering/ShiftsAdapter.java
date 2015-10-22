package com.adamnickle.delivering;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collection;
import java.util.Date;


public class ShiftsAdapter extends ParseObjectArrayAdapter<Shift, ShiftsAdapter.ShiftViewHolder>
{
    public interface ShiftsAdapterListener
    {
        void onShiftClick( Shift shift );
    }

    private final Context mContext;

    private ShiftsAdapterListener mListener;

    public ShiftsAdapter( Context context, ParseQueryFactory<Shift> queryFactory )
    {
        super( queryFactory );

        mContext = context;
    }

    public ShiftsAdapter( Context context, Collection<Shift> items )
    {
        super( items );
        mContext = context;
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
                        mListener.onShiftClick( ShiftViewHolder.this.Shift );
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
