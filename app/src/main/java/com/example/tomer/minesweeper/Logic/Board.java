package com.example.tomer.minesweeper.Logic;

import java.util.Random;



public class Board {



    private Tile mTiles[][];
    private int mNumberOfMines;
    private int mNumberOfCols;
    private int mNumberOfTiles;
    private int mUnrevealedSafeTiles;


    public Board(int numberOfMines,int numberOfCols) {
        //initialization

        mNumberOfCols=numberOfCols;
        mNumberOfMines=numberOfMines;
        mNumberOfTiles=numberOfCols*numberOfCols;
        mUnrevealedSafeTiles=mNumberOfTiles-mNumberOfMines;
        mTiles = new Tile [mNumberOfCols][mNumberOfCols];

        for(int i=0;i<mNumberOfCols;i++){
            for(int j=0; j<mNumberOfCols;j++){
                mTiles[i][j] = new Tile();
                mTiles[i][j].setRevealed(false);
                mTiles[i][j].setmNumber(0);
                mTiles[i][j].setMined(false);
                mTiles[i][j].setFlagged(false);
            }
        }
        createBoard(mNumberOfMines);
    }
    public void createBoard(int numberOfMines) {
        Random rand = new Random();
        int rowRand = rand.nextInt(mTiles.length);
        int colRand = rand.nextInt(mTiles.length);
        for (int i = 0; i < numberOfMines; i++) {
            if (mTiles[rowRand][colRand].isMined() && i != 0) {
                i--;
            } else {
                mTiles[rowRand][colRand].setMined(true);
            }
            rowRand = rand.nextInt(mTiles.length);
            colRand = rand.nextInt(mTiles.length);
        }

        for (int i = 0; i < mTiles[0].length; i++) {
            for (int j = 0; j < mTiles[0].length; j++) {
                if (!mTiles[i][j].isMined()) {
                    mTiles[i][j].setmNumber(countSurroundedMines(i, j));
                }
            }


        }
    }
    public int getmNumberOfCols() {
        return mTiles[0].length;
    }

    public int getmNumberOfTiles() {
        return mNumberOfTiles;
    }

    public int getmNumberOfMines() {
        return mNumberOfMines;
    }

    public int getmUnrevealedSafeTiles() {
        return mUnrevealedSafeTiles;
    }

    public Tile getTile(int posI, int posJ){
        return mTiles[posI][posJ];
    }

    public boolean setFlagState (boolean flagState){ // ???
        return flagState;
    }

    public boolean revealTile(int posI, int posJ){
        if (!mTiles[posI][posJ].isRevealed()){
            mTiles[posI][posJ].setRevealed(true);
            return true;
        }
        else
            return false;
    }

    public int countSurroundedMines (int posI,int posJ){

        int sumOfMines=0;
        for (int i=Math.max(0,posI-1); i<=Math.min(posI+1,(getmNumberOfCols()-1)); i++){
            for (int j=Math.max(0,posJ-1);j<= Math.min(posJ+1,(getmNumberOfCols()-1)); j++){
                if (i!=posI || j!=posJ){
                    if(mTiles[i][j].isMined()){
                        sumOfMines++;
                    }
                }
            }
        }
        return sumOfMines;
    }


    public boolean addMine(){

        if(getmUnrevealedSafeTiles() == 0 || mNumberOfMines == mNumberOfTiles) {
            return false;
        }

        boolean mineAdded = false;
        Random rnd = new Random();

        while(!mineAdded) {

            int randomRow = rnd.nextInt(mNumberOfCols);
            int randomCol = rnd.nextInt(mNumberOfCols);

            if(!mTiles[randomRow][randomCol].isMined() && !mTiles[randomRow][randomCol].isRevealed()){

                // Setting the mine:
                mTiles[randomRow][randomCol].setMined(true);

                // Update the relevant fields:
                mUnrevealedSafeTiles--;
                mNumberOfMines++;
                mineAdded = true;

                // Hide surrounding tiles:

                for(int i = randomRow-1; i <= randomRow+1; i++){
                    for(int j = randomCol-1; j <= randomCol+1; j++){

                        if(i>=0 && i< mNumberOfCols && j>=0 && j< mNumberOfCols){
                            if(i!=randomRow || j!=randomCol){

                                if(mTiles[i][j].isRevealed()) {
                                    mTiles[i][j].setRevealed(false);
                                    mUnrevealedSafeTiles++;
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }


    public void revealAll(){
        for (int i=0;i<getmNumberOfCols();i++){
            for (int j=0; j<getmNumberOfCols();j++){
                mTiles[i][j].setRevealed(true);
            }
        }
    }


    public void setmUnrevealedSafeTiles(int mUnrevealedSafeTiles) {
        this.mUnrevealedSafeTiles = mUnrevealedSafeTiles;
    }
}
