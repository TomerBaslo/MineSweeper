package com.example.tomer.minesweeper;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.tomer.minesweeper.Logic.Game.Difficulty;

public class LeaderboardFragment extends Fragment {

    public static final String LEADERBOARD_FRAGMENT_TAG = "LeaderboardFragmentTag";

    public OnGameSelectedListener mCallback;
    private ListView mListView;
    private Difficulty mMode;
    private int mSelectedGame;

    public static LeaderboardFragment newInstance() {
        return new LeaderboardFragment();
    }

    public LeaderboardFragment() {
    }

    // Container must implement this interface
    public interface OnGameSelectedListener {
        public void onGameSelected(int position);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // seeing if the container has implemented the callback interface.
        try {
            mCallback = (OnGameSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnGameSelectedListener");
        }

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMode = Difficulty.EASY;
        mSelectedGame = 0;

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_leaderboard, container, false);

        //Preparing the listView:

        mListView = (ListView)rootView.findViewById(R.id.leaderboard_list);

        mListView.setAdapter(new RecordAdapter(getActivity(), mMode));

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                int gamePicked = position + 1;

                if(mSelectedGame != gamePicked){
                    mSelectedGame = gamePicked;
                    ((RecordAdapter) mListView.getAdapter()).setSelectedGame(gamePicked);
                    ((RecordAdapter) mListView.getAdapter()).notifyDataSetChanged();

                    // Send the event to the host activity:
                    mCallback.onGameSelected(position);

                }

            }
        });

        return rootView;

    }




    public void updateSelectedGame(int position) {
        mSelectedGame = position;
        ((RecordAdapter) mListView.getAdapter()).setSelectedGame(position);
        ((RecordAdapter) mListView.getAdapter()).notifyDataSetChanged();
        mListView.smoothScrollToPosition(position);

    }

    public void updateMode(Difficulty mode) {
        mMode = mode;
        ((RecordAdapter) mListView.getAdapter()).setDifficulty(mode);
        mSelectedGame = 0;
        ((RecordAdapter) mListView.getAdapter()).setSelectedGame(0);
        ((RecordAdapter) mListView.getAdapter()).notifyDataSetChanged();
        mListView.smoothScrollToPosition(0);

    }

}

