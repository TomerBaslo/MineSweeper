package com.example.tomer.minesweeper;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.example.tomer.minesweeper.Logic.Board;
import com.example.tomer.minesweeper.Logic.Tile;


public class TileAdapter extends BaseAdapter {

   private Board mBoard;
    private Context mContext;

    public TileAdapter(Context context, Board board) {

        mBoard = board;
        mContext = context;

    }

    @Override
    public int getCount() {
       return mBoard.getmNumberOfTiles();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        TileView tileView;
        tileView = (TileView)convertView;
        if(tileView == null) {
            tileView = new TileView(mContext);
        }
        tileView.setBackgroundColor(Color.GRAY);
        tileView.text.setText("");
        //If not revealed:
        if(!getItem(position).isRevealed()){

            //if this tile isn't revealed, all we have to do is check if its flagged:
            if(getItem(position).isFlagged()){
                tileView.setBackgroundResource(R.drawable.flag32);
            }

        }
        else { //The tile is revealed:

            tileView.setBackgroundColor(Color.WHITE);

            if(!getItem(position).isFlagged()){

                if(getItem(position).isMined()){
                    tileView.setBackgroundResource(R.drawable.mine32); // revealed and mined.
                }
                else{ //Revealed, but not mined and not flagged:
                    int surroundingMines = mBoard.countSurroundedMines(position/mBoard.getmNumberOfCols(), position%mBoard.getmNumberOfCols());
                    if(surroundingMines > 0) { //The tile should show the number of surrounding mines if there are any.
                        tileView.text.setText("" + surroundingMines);
                    }
                }

            }
            else{
                tileView.setBackgroundResource(R.drawable.flag32);
            }

        }


        return tileView;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public Tile getItem(int position) {
        return mBoard.getTile(position/mBoard.getmNumberOfCols(),position%mBoard.getmNumberOfCols());
    }
}