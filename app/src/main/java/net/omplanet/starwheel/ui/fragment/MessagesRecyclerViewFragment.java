package net.omplanet.starwheel.ui.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import net.omplanet.starwheel.R;
import net.omplanet.starwheel.model.api.ApiManager;
import net.omplanet.starwheel.model.api.MessageData;
import net.omplanet.starwheel.model.domain.Message;
import net.omplanet.starwheel.ui.adapter.MessageAdapter;

import java.util.List;

/**
 * Using {@link android.support.v7.widget.RecyclerView} with a {@link android.support.v7.widget.LinearLayoutManager} and a
 * {@link android.support.v7.widget.GridLayoutManager}.
 */
public class MessagesRecyclerViewFragment extends Fragment {

    private final static String TAG = MessagesRecyclerViewFragment.class.getName();
    private static final String LAYOUT_MANAGER_KEY = "layoutManager";
    private static final int SPAN_COUNT = 2;
    private static final int DATASET_COUNT = 60;

    private enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }

    protected LayoutManagerType mCurrentLayoutManagerType;

    protected RecyclerView mRecyclerView;
    protected MessageAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected Message[] mDataset;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_msg_recycler_view, container, false);
        rootView.setTag(TAG);

        // BEGIN_INCLUDE(initializeRecyclerView)
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

        // LinearLayoutManager is used here, this will layout the elements in a similar fashion
        // to the way ListView would layout elements. The RecyclerView.LayoutManager defines how
        // elements are laid out.
        mLayoutManager = new LinearLayoutManager(getActivity());

        mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;

        if (savedInstanceState != null) {
            // Restore saved layout manager type.
            mCurrentLayoutManagerType = (LayoutManagerType) savedInstanceState
                    .getSerializable(LAYOUT_MANAGER_KEY);
        }
        setRecyclerViewLayoutManager(mCurrentLayoutManagerType);
        if(mRecyclerView.getAdapter() == null) {
            try {
                ApiManager.getInstance().getMessageData(createMyReqSuccessListener(), createMyReqErrorListener(), "Bob", DATASET_COUNT, null, ApiManager.REQUEST_TAG_MAIN_MESSAGES);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), R.string.unknown_error, Toast.LENGTH_LONG).show();
            }
        }
        // END_INCLUDE(initializeRecyclerView)

        return rootView;
    }

    /**
     * Set RecyclerView's LayoutManager to the one given.
     *
     * @param layoutManagerType Type of layout manager to switch to.
     */
    public void setRecyclerViewLayoutManager(LayoutManagerType layoutManagerType) {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position.
        if (mRecyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) mRecyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        switch (layoutManagerType) {
            case GRID_LAYOUT_MANAGER:
                mLayoutManager = new GridLayoutManager(getActivity(), SPAN_COUNT);
                mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
                break;
            case LINEAR_LAYOUT_MANAGER:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
                break;
            default:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        }

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.scrollToPosition(scrollPosition);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save currently selected layout manager.
        savedInstanceState.putSerializable(LAYOUT_MANAGER_KEY, mCurrentLayoutManagerType);
        super.onSaveInstanceState(savedInstanceState);
    }

    private Response.Listener<MessageData> createMyReqSuccessListener() {
        return new Response.Listener<MessageData>() {
            @Override
            public void onResponse(MessageData response) {
                Log.v(TAG, "Messages data loaded");

                //TODO avoid casting the list into an array
                List<Message> list = response.getMessages();
                Message[] array = new Message[list.size()];
                mDataset = list.toArray(array);
                mAdapter = new MessageAdapter(mDataset);
                // Set the adapter for RecyclerView.
                mRecyclerView.setAdapter(mAdapter);
            }
        };
    }

    private Response.ErrorListener createMyReqErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Messages data failed to load");
                if(getActivity() != null) Toast.makeText(getActivity(), R.string.connection_error, Toast.LENGTH_LONG).show();
            }
        };
    }
}