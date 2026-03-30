package com.example.lottery_legend.event;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery_legend.R;
import com.example.lottery_legend.model.Event;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Adapter for the RecyclerView that displays a list of Events for entrants.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private List<Event> eventList;
    private String currentDeviceId;

    /**
     * ViewHolder class that holds the views for a single event card.
     * Reference: https://developer.android.com/develop/ui/views/layout/recyclerview#java
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView status;
        TextView locationText;
        TextView deadline;
        TextView waitingCount;
        LinearLayout locationRow;
        Button joinButton;
        ImageView posterImage;
        LinearLayout cardContent;

        /**
         * Constructs a ViewHolder and initializes all UI components.
         * @param itemView The view of the individual list item.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            status = itemView.findViewById(R.id.status);
            locationText = itemView.findViewById(R.id.locationText);
            locationRow = itemView.findViewById(R.id.locationRow);
            deadline = itemView.findViewById(R.id.deadline);
            waitingCount = itemView.findViewById(R.id.waitingCount);
            joinButton = itemView.findViewById(R.id.joinButton);
            posterImage = itemView.findViewById(R.id.posterImage);
            cardContent = itemView.findViewById(R.id.cardContent);
        }
    }

    /**
     * Constructs an EventAdapter with the specified data.
     * @param eventList       List of Event objects to display.
     * @param currentDeviceId Device ID of the user viewing the list.
     */
    public EventAdapter(List<Event> eventList, String currentDeviceId) {
        this.eventList = eventList;
        this.currentDeviceId = currentDeviceId;
    }

    /**
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent an item.
     * @param parent   The ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given type.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_content, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * Updates the contents of the {@link ViewHolder#itemView} to reflect the event item at the given position.
     * @param holder   The ViewHolder which should be updated.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Event event = eventList.get(position);

        holder.title.setText(event.getTitle());
        
        Event.EventLocation loc = event.getEventLocation();
        holder.locationText.setText(loc != null ? loc.getName() : "");
        
        Timestamp regEndAt = event.getRegistrationEndAt();
        if (regEndAt != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy", Locale.getDefault());
            holder.deadline.setText(sdf.format(regEndAt.toDate()));
        } else {
            holder.deadline.setText("");
        }

        int waitingListSize = (event.getWaitingList() != null) ? event.getWaitingList().size() : 0;
        holder.waitingCount.setText(String.valueOf(waitingListSize));

        holder.locationRow.setVisibility(event.isGeoEnabled() ? View.VISIBLE : View.GONE);

        // Decode and set the poster image if available
        if (event.getPosterImage() != null && !event.getPosterImage().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(event.getPosterImage(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                if (bitmap != null) {
                    holder.posterImage.setImageBitmap(bitmap);
                } else {
                    holder.posterImage.setImageResource(R.drawable.img_poster);
                }
            } catch (Exception e) {
                holder.posterImage.setImageResource(R.drawable.img_poster);
            }
        } else {
            holder.posterImage.setImageResource(R.drawable.img_poster);
        }

        // Determine if the current user has already joined the waiting list
        boolean isJoined = false;
        if (event.getWaitingList() != null) {
            for (Event.WaitingListEntry entry : event.getWaitingList()) {
                if (Objects.equals(entry.getDeviceId(), currentDeviceId)) {
                    isJoined = true;
                    break;
                }
            }
        }

        // Reset alpha to full opacity by default
        setAlpha(holder, 1.0f);

        // Configure UI based on event status and user's join status
        if (!Objects.equals(event.getStatus(), "open")) {
            holder.status.setText("Closed");
            holder.status.setTextColor(Color.parseColor("#64748B"));
            holder.joinButton.setVisibility(View.GONE);
            setAlpha(holder, 0.5f); // Dim the card content
        } else if (isJoined) {
            // User is already on the waiting list
            holder.status.setText("Joined");
            holder.status.setTextColor(Color.parseColor("#F59E0B"));
            holder.joinButton.setVisibility(View.VISIBLE);
            holder.joinButton.setText("Leave Waiting List");
            holder.joinButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#EF4444")));

            holder.joinButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Open dialog to confirm leaving the list
                    WaitingListDialogFragment.newInstance(event, currentDeviceId)
                            .show(((AppCompatActivity) v.getContext()).getSupportFragmentManager(), "Leave Waiting List");
                }
            });
        } else {
            // Event is open and user hasn't joined yet
            holder.status.setText("Open");
            holder.status.setTextColor(Color.parseColor("#10B981"));
            holder.joinButton.setVisibility(View.VISIBLE);
            holder.joinButton.setText("Join WaitingList");
            holder.joinButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#2563EB")));

            holder.joinButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Open dialog to confirm joining the list
                    WaitingListDialogFragment.newInstance(event, currentDeviceId)
                            .show(((AppCompatActivity) v.getContext()).getSupportFragmentManager(), "Join Waiting List");
                }
            });
        }
    }

    /**
     * Helper method to set the alpha transparency of the card content.
     * @param holder The ViewHolder containing the card.
     * @param alpha  The alpha value to set (0.0 to 1.0).
     */
    private void setAlpha(ViewHolder holder, float alpha) {
        if (holder.cardContent.getChildCount() > 1) {
            holder.cardContent.getChildAt(1).setAlpha(alpha);
        }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return eventList.size();
    }
}
