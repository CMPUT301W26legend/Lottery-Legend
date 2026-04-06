package com.example.lottery_legend.event;

import android.content.Intent;
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
 * Adapter for the RecyclerView that displays a list of events to entrants.
 * It manages the display of event cards, including status updates, image decoding,
 * and handles interactions such as joining or leaving a waiting list.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    /** The list of event models to be displayed. */
    private List<Event> eventList;
    /** The device ID of the current user, used to determine participation status. */
    private String currentDeviceId;

    /**
     * ViewHolder class that holds references to the UI components for an individual event item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView status;
        public TextView locationText;
        public TextView deadline;
        public TextView waitingCount;
        public LinearLayout locationRow;
        public Button joinButton;
        public ImageView posterImage;
        public LinearLayout cardContent;

        /**
         * Initializes UI components from the inflated layout.
         * @param itemView The root view of the item layout.
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
     * Constructs a new EventAdapter.
     * @param eventList       The data set of events.
     * @param currentDeviceId The unique ID of the current device/user.
     */
    public EventAdapter(List<Event> eventList, String currentDeviceId) {
        this.eventList = eventList;
        this.currentDeviceId = currentDeviceId;
    }

    /**
     * Inflates the layout for a single event card.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_content, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds event data to the view holder. Sets text fields, decodes images, 
     * and updates the UI based on the user's current participation status.
     *
     * @param holder   The ViewHolder to update.
     * @param position The position of the event in the list.
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

        // Display current waiting list occupancy and capacity
        int waitingListSize = (event.getWaitingList() != null) ? event.getWaitingList().size() : 0;
        if (event.getMaxWaitingList() != null) {
            holder.waitingCount.setText(String.format(Locale.getDefault(), "%d/%d waiting", waitingListSize, event.getMaxWaitingList()));
        } else {
            holder.waitingCount.setText(String.format(Locale.getDefault(), "%d waiting", waitingListSize));
        }

        // Only show location row if geolocation is required
        holder.locationRow.setVisibility(event.isGeoEnabled() ? View.VISIBLE : View.GONE);

        // Decode the Base64 poster image string if it exists
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
        String participationStatus = null;
        String finalResult = null;
        if (event.getWaitingList() != null) {
            for (Event.WaitingListEntry entry : event.getWaitingList()) {
                if (Objects.equals(entry.getDeviceId(), currentDeviceId)) {
                    isJoined = true;
                    participationStatus = entry.getParticipationStatus();
                    finalResult = entry.getFinalResult();
                    break;
                }
            }
        }

        // Reset alpha to full opacity before potential changes in updateStatusUI
        setAlpha(holder, 1.0f);

        // Update status label and buttons based on participation logic
        updateStatusUI(holder, event, isJoined, participationStatus, finalResult);

        // Clicking the whole card opens the detailed event view
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EventDetailsActivity.class);
            intent.putExtra("eventId", event.getEventId());
            intent.putExtra("deviceId", currentDeviceId);
            v.getContext().startActivity(intent);
        });
    }

    /**
     * Updates the status TextView and the Join/Leave button based on complex event and user states.
     * Handles states such as "Joined", "Selected", "Accepted", "Drawn", and "Closed".
     */
    private void updateStatusUI(ViewHolder holder, Event event, boolean isJoined, String participationStatus, String finalResult) {
        Timestamp now = Timestamp.now();
        String status = event.getStatus() != null ? event.getStatus().toLowerCase() : "open";
        
        holder.joinButton.setVisibility(View.GONE);

        // If user is in the waiting list, participation status takes precedence
        if (isJoined) {
            if ("LOSS".equalsIgnoreCase(finalResult) || "not_selected".equalsIgnoreCase(participationStatus)) {
                holder.status.setText("Not Selected");
                holder.status.setTextColor(Color.parseColor("#9CA3AF"));
                return;
            }
            if ("accepted".equalsIgnoreCase(participationStatus)) {
                holder.status.setText("Accepted");
                holder.status.setTextColor(Color.parseColor("#388E3C"));
                return;
            }
            if ("declined".equalsIgnoreCase(participationStatus) || "cancelled".equalsIgnoreCase(participationStatus)) {
                holder.status.setText("Cancelled/Declined");
                holder.status.setTextColor(Color.parseColor("#EF4444"));
                return;
            }
            if ("selected".equalsIgnoreCase(participationStatus) || "invited".equalsIgnoreCase(participationStatus)) {
                holder.status.setText("Selected!");
                holder.status.setTextColor(Color.parseColor("#2563EB"));
                return;
            }
        }

        // Check if the event has actually passed
        if (event.getEventStartAt() != null && event.getEventStartAt().compareTo(now) < 0) {
            holder.status.setText("Closed");
            holder.status.setTextColor(Color.parseColor("#9CA3AF"));
            setAlpha(holder, 0.5f);
        } else if ("drawn".equals(status)) {
            holder.status.setText("Drawn");
            holder.status.setTextColor(Color.parseColor("#F57C00"));
        } else if ("open".equals(status)) {
            if (isJoined) {
                holder.status.setText("Joined");
                holder.status.setTextColor(Color.parseColor("#F59E0B"));
                holder.joinButton.setVisibility(View.VISIBLE);
                holder.joinButton.setText("Leave Waiting List");
                holder.joinButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#EF4444")));

                holder.joinButton.setOnClickListener(v -> {
                    WaitingListDialogFragment.newInstance(event, currentDeviceId)
                            .show(((AppCompatActivity) v.getContext()).getSupportFragmentManager(), "Leave Waiting List");
                });
            } else {
                holder.status.setText("Active");
                holder.status.setTextColor(Color.parseColor("#388E3C"));
                holder.joinButton.setVisibility(View.VISIBLE);
                holder.joinButton.setText("Join WaitingList");
                holder.joinButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#2563EB")));

                holder.joinButton.setOnClickListener(v -> {
                    WaitingListDialogFragment.newInstance(event, currentDeviceId)
                            .show(((AppCompatActivity) v.getContext()).getSupportFragmentManager(), "Join Waiting List");
                });
            }
        } else {
            // Fallback for unexpected or finalized statuses
            holder.status.setText(status.toUpperCase());
            holder.status.setTextColor(Color.parseColor("#9CA3AF"));
        }
    }

    /**
     * Helper method to set transparency for parts of the card view.
     */
    private void setAlpha(ViewHolder holder, float alpha) {
        if (holder.cardContent.getChildCount() > 1) {
            holder.cardContent.getChildAt(1).setAlpha(alpha);
        }
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }
}
