package com.example.tomer.minesweeper.Logic;


import java.io.Serializable;

public class Game {

    final static int EASY_MINES_NUMBER=5;
    final static int EASY_COLS_NUMBER=10;
    final static int MEDIUM_MINES_NUMBER=10;
    final static int MEDIUM_COLS_NUMBER=10;
    final static int HARD_MINES_NUMBER=10;
    final static int HARD_COLS_NUMBER=5;

    public enum Difficulty implements Serializable {EASY,MEDIUM,HARD} ;
    public enum GameState implements Serializable {NOT_STARTED,STARTED,WON,LOST};
    private Difficulty mDifficulty;
    private GameState mGameState;
    private Board mBoard;
    private boolean mFlagState;

    public Game(Difficulty difficulty){
        mFlagState=false;
        mDifficulty=difficulty;
        mGameState=GameState.NOT_STARTED;
        switch (mDifficulty){
            case EASY:
                mBoard = new Board(EASY_MINES_NUMBER,EASY_COLS_NUMBER);
                break;
            case MEDIUM:
                mBoard = new Board(MEDIUM_MINES_NUMBER,MEDIUM_COLS_NUMBER);
                break;
            case HARD:
                mBoard = new Board(HARD_MINES_NUMBER,HARD_COLS_NUMBER);

        }
    }


    public void playTile(int posI, int posJ){

            if (getmFlagState()) {
                if(!mBoard.getTile(posI,posJ).isFlagged() && !mBoard.getTile(posI,posJ).isRevealed())
                    mBoard.getTile(posI, posJ).setFlagged(true);
                else
                    mBoard.getTile(posI, posJ).setFlagged(false);

            } else {
                if(mBoard.getTile(posI,posJ).isFlagged() || mBoard.getTile(posI,posJ).isRevealed())
                    return;
                if (mBoard.getTile(posI, posJ).isMined()) {
                    mGameState = GameState.LOST;
                    mBoard.revealAll();
                } else {
                    if (mGameState == GameState.NOT_STARTED)
                        mGameState = GameState.STARTED;
                    revealNumbers(posI, posJ);
                    if(mBoard.getmUnrevealedSafeTiles()==0){
                        mGameState=GameState.WON;
                    }
                }
            }
    }


    public void revealNumbers(int posI,int posJ){
        if(!mBoard.getTile(posI,posJ).isMined() && mBoard.getTile(posI,posJ).getmNumber()!=0 && !mBoard.getTile(posI,posJ).isFlagged() && !mBoard.getTile(posI,posJ).isRevealed()){
            mBoard.getTile(posI, posJ).setRevealed(true);
            mBoard.setmUnrevealedSafeTiles(mBoard.getmUnrevealedSafeTiles()-1);
        }
        else if(!mBoard.getTile(posI,posJ).isMined() && mBoard.getTile(posI,posJ).getmNumber()==0 && !mBoard.getTile(posI,posJ).isFlagged() && !mBoard.getTile(posI,posJ).isRevealed()) {
            mBoard.getTile(posI, posJ).setRevealed(true);
            mBoard.setmUnrevealedSafeTiles(mBoard.getmUnrevealedSafeTiles()-1);
            for (int i = Math.max(0, posI - 1); i <= Math.min(posI + 1, (mBoard.getmNumberOfCols() - 1)); i++) {
                for (int j = Math.max(0, posJ - 1); j <= Math.min(posJ + 1, (mBoard.getmNumberOfCols() - 1)); j++) {
                    if (i != posI || j != posJ) {
                            revealNumbers(i, j);

                    }
                }
            }
        }
    }

    public void punishPlayer() {
        if(mGameState == GameState.NOT_STARTED || mGameState == GameState.STARTED) {
            if(mBoard.addMine()) {
                if(mBoard.getmUnrevealedSafeTiles() == 0 ||
                        mBoard.getmNumberOfMines() == mBoard.getmNumberOfTiles()) {
                    mGameState = GameState.LOST;
                    mBoard.revealAll();
                }
            }
        }
    }

    public Difficulty getmDifficulty() {
        return mDifficulty;
    }

    public GameState getmGameState() {
        return mGameState;
    }

    public Board getmBoard() {
        return mBoard;
    }
    public boolean getmFlagState(){
        return mFlagState;
    }
    public void setmFlagState(boolean mFlagState) {
        this.mFlagState = mFlagState;
    }
}
