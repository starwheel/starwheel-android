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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Provide views to RecyclerView with data from mDataSet.
 * TODO: implement loading more data on scroll down or refreshing on scroll up (shouldLoadMoreData)
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private static final String TAG = MessageAdapter.class.getName();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("M/d/yy h:mm a", Locale.US);

    private Message[] mDataSet;

    // BEGIN_INCLUDE(recyclerViewSampleViewHolder)
    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final NetworkImageView userImageView;
        private final TextView userNameTextView;
        private final TextView messageTextView;
        private final TextView timeTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            // Define click listener for the ViewHolder's View.
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View itemView) {
                    //TODO
                    Log.d(TAG, "Element " + getPosition() + " clicked.");
                }
            });

            userImageView = (NetworkImageView) itemView.findViewById(R.id.itemUserImageView);
//            userImageView.setDefaultImageResId(R.drawable.no_profile_image);
            userImageView.setErrorImageResId(R.drawable.error_profile_image);
            userNameTextView = (TextView) itemView.findViewById(R.id.itemUsernameTextView);
            messageTextView = (TextView) itemView.findViewById(R.id.itemMessageTextView);
            timeTextView = (TextView) itemView.findViewById(R.id.itemTimeTextView);
        }

        public NetworkImageView getUserImageView() { return userImageView; }
        public TextView getUserNameTextView() { return userNameTextView; }
        public TextView getMessageTextView() {
            return messageTextView;
        }
        public TextView getTimeTextView() { return timeTextView; }
    }
    // END_INCLUDE(recyclerViewSampleViewHolder)

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used by RecyclerView.
     */
    public MessageAdapter(Message[] dataSet) {
        mDataSet = dataSet;
    }

    // BEGIN_INCLUDE(recyclerViewOnCreateViewHolder)
    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.text_row_item, viewGroup, false);

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
                viewHolder.getUserImageView().setImageUrl(url, ImageCacheManager.getInstance().getImageLoader());
            } else {
                //Solving the bug,
                viewHolder.getUserImageView().setDefaultImageResId(R.drawable.no_profile_image);
                //if url == null, Volley will not load any image to replace the default
                viewHolder.getUserImageView().setImageUrl(null, ImageCacheManager.getInstance().getImageLoader());
            }
            viewHolder.getUserNameTextView().setText("@" + message.getUserName());
            viewHolder.getMessageTextView().setText(message.getMessage());
            viewHolder.getTimeTextView().setText(formatDisplayDate(message.getCreatedDate()));
        }
    }
    // END_INCLUDE(recyclerViewOnBindViewHolder)

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataSet.length;
    }

    private String formatDisplayDate(Date date) {
        if(date != null){
            return sdf.format(date);
        }
        return "";
    }
}