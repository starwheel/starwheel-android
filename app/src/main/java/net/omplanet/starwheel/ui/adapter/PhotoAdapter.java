package net.omplanet.starwheel.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import net.omplanet.starwheel.R;
import net.omplanet.starwheel.model.domain.Message;
import net.omplanet.starwheel.model.imagecache.ImageCacheManager;

/**
 * Provide views to RecyclerView with data from mDataSet.
 * TODO: implement loading more data on scroll down or refreshing on scroll up (shouldLoadMoreData)
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {
    private static final String TAG = PhotoAdapter.class.getName();

    private Message[] mDataSet;

    // BEGIN_INCLUDE(recyclerViewSampleViewHolder)
    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final NetworkImageView photoView;

        public ViewHolder(View v) {
            super(v);
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View itemView) {
                    //TODO
                    Log.d(TAG, "Element " + getPosition() + " clicked.");
                }
            });

            textView = (TextView) v.findViewById(R.id.itemPhotoTextView);
            photoView = (NetworkImageView) v.findViewById(R.id.itemPhotoImageView);
        }

        public TextView getTextView() {
            return textView;
        }

        public NetworkImageView getPhotoView() {
            return photoView;
        }
    }
    // END_INCLUDE(recyclerViewSampleViewHolder)

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used by RecyclerView.
     */
    public PhotoAdapter(Message[] dataSet) {
        mDataSet = dataSet;
    }

    // BEGIN_INCLUDE(recyclerViewOnCreateViewHolder)
    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.photo_row_item, viewGroup, false);

        return new ViewHolder(v);
    }
    // END_INCLUDE(recyclerViewOnCreateViewHolder)

    // BEGIN_INCLUDE(recyclerViewOnBindViewHolder)
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        //Log.d(TAG, "Element " + position + " set.");

        // Get element from dataset at this position and replace the contents of the view with that element
        Message message = mDataSet[position];
        if(message != null){
            String url = message.getImage();
            if(url != null && !url.isEmpty()) {
                viewHolder.getPhotoView().setImageUrl(url, ImageCacheManager.getInstance().getImageLoader());
            } else {
                //Solving the bug,
                viewHolder.getPhotoView().setDefaultImageResId(R.drawable.no_profile_image);
                //if url == null, Volley will not load any image to replace the default
                viewHolder.getPhotoView().setImageUrl(null, ImageCacheManager.getInstance().getImageLoader());
            }
            viewHolder.getTextView().setText("@" + message.getUserName());
        }
    }
    // END_INCLUDE(recyclerViewOnBindViewHolder)

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataSet.length;
    }
}