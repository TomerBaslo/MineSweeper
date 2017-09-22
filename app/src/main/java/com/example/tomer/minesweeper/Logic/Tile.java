package com.example.tomer.minesweeper.Logic;

public class Tile {

    private int mNumber;
    private boolean mIsFlagged;
    private boolean mIsMined;
    private boolean mIsRevealed;

    public int getmNumber() {
        return mNumber;
    }
    public void setmNumber(int mNumber) {
        this.mNumber = mNumber;
    }

    public boolean isMined() {
        return mIsMined;
    }

    public void setMined(boolean mIsMined) {
        this.mIsMined = mIsMined;
    }

    public boolean isRevealed() {
        return mIsRevealed;
    }

    public void setRevealed(boolean revealed) {
        mIsRevealed = revealed;
    }

    public void setFlagged(boolean flagged) {
        mIsFlagged = flagged;
    }

    public boolean isFlagged() {
        return mIsFlagged;
    }

    @Override
    public String toString() {
         return " isNumbered "+mNumber + " isFlagged: " + mIsFlagged + " isMined: " + mIsMined +
                 " isRevealed "+ mIsRevealed;
    }


}
