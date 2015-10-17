package com.adamnickle.delivering;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


@SuppressWarnings( "ValidFragment" )
public class ShiftFragment extends Fragment
{
    public static ShiftFragment newInstance( Shift shift )
    {
        return new ShiftFragment( shift );
    }

    private final Shift mShift;

    public ShiftFragment( Shift shift )
    {
        mShift = shift;
    }

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        return inflater.inflate( R.layout.fragment_shift, container, false );
    }
}
