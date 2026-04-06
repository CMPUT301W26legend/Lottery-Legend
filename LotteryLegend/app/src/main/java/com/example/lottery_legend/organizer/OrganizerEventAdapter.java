package com.example.lottery_legend.organizer;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery_legend.R;
import com.example.lottery_legend.model.Event;
import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying events in the Organizer's event list or history.
 * Handles the display of event summary information such as title, status, 
 * capacity, and entrant counts.
 */
public class OrganizerEventAdapter extends RecyclerView.Adapter<OrganizerEventAdapter.ViewHolder> {

    /** List of events to be displayed. */
    private final List<Event> eventList;
    /** The device ID of the current organizer. */
    private final String deviceId;

    /**
     * ViewHolder class that holds references to the UI components for an individual event card.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        /** TextView for the event title. */
        TextView eventName;
        /** TextView for the event's current status (e.g., ACTIVE, DRAWN, CLOSED). */
        TextView status;
        /** TextView for general event info like date and capacity. */
        TextView eventInfo;
        /** TextView for the number of entrants currently on the waiting list. */
        TextView waiting;
        /** TextView for the number of entrants who have been selected. */
        TextView selected;
        /** Tag displayed if the current user is a co-organizer rather than the owner. */
        TextView tagCoOrganizer;
        /** Tag displayed if the event is marked as private. */
        TextView tagPrivate;

        /**
         * Initializes the UI components from the inflated layout.
         * @param itemView The root view of the item layout.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.eventName);
            status = itemView.findViewById(R.id.status);
            eventInfo = itemView.findViewById(R.id.eventInfo);
            waiting = itemView.findViewById(R.id.waiting);
            selected = itemView.findViewById(R.id.selected);
            tagCoOrganizer = itemView.findViewById(R.id.tagCoOrganizer);
            tagPrivate = itemView.findViewById(R.id.tagPrivate);
        }
    }

    /**
     * Constructs a new OrganizerEventAdapter.
     * @param eventList List of events to display.
     * @param deviceId  The unique identifier for the current organizer.
     */
    public OrganizerEventAdapter(List<Event> eventList, String deviceId) {
        this.eventList = eventList;
        this.deviceId = deviceId;
    }

    /**
     * Inflates the layout for an event card.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.organizer_event_card_content, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds event data to the view holder.
     * Configures status labels, co-organizer tags, and click listeners for details navigation.
     *
     * @param holder   The ViewHolder to update.
     * @param position The position of the event in the data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Event event = eventList.get(position);

        holder.eventName.setText(event.getTitle());
        
        updateStatusUI(holder.status, event);

        // Display a special tag if the user is a co-organizer for this event
        if (event.getOrganizerId() != null && !event.getOrganizerId().equals(deviceId)) {
            holder.tagCoOrganizer.setVisibility(View.VISIBLE);
        } else {
            holder.tagCoOrganizer.setVisibility(View.GONE);
        }

        // Display a tag if the event is private
        if (event.isIsPrivateEvent()) {
            holder.tagPrivate.setVisibility(View.VISIBLE);
        } else {
            holder.tagPrivate.setVisibility(View.GONE);
        }
        
        String deadlineStr = "";
        Timestamp registrationEndAt = event.getRegistrationEndAt();
        if (registrationEndAt != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy", Locale.getDefault());
            deadlineStr = sdf.format(registrationEndAt.toDate());
        }
        
        String info = deadlineStr + " · " + event.getCapacity() + " capacity";
        holder.eventInfo.setText(info);
        
        int waitingCount = (event.getWaitingList() != null) ? event.getWaitingList().size() : 0;
        if (event.getMaxWaitingList() != null) {
            holder.waiting.setText(String.format(Locale.getDefault(), "%d/%d waiting", waitingCount, event.getMaxWaitingList()));
        } else {
            holder.waiting.setText(String.format(Locale.getDefault(), "%d waiting", waitingCount));
        }
        
        holder.selected.setText(event.getSelectedCount() + " selected");

        // Navigate to the event details activity on click
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), OrganizerEventDetailsActivity.class);
            intent.putExtra("eventId", event.getEventId());
            intent.putExtra("eventTitle", event.getTitle());
            intent.putExtra("deviceId", deviceId);
            v.getContext().startActivity(intent);
        });
    }

    /**
     * Updates the status TextView with a human-readable label and color based on the event's state.
     * @param statusView The TextView to update.
     * @param event      The event model.
     */
    private void updateStatusUI(TextView statusView, Event event) {
        Timestamp now = Timestamp.now();
        String status = event.getStatus() != null ? event.getStatus().toLowerCase() : "open";
        
        if (event.getEventStartAt() != null && event.getEventStartAt().compareTo(now) < 0) {
            statusView.setText("CLOSED");
            statusView.setTextColor(Color.parseColor("#9CA3AF"));
        } else if ("drawn".equals(status)) {
            statusView.setText("DRAWN");
            statusView.setTextColor(Color.parseColor("#F57C00"));
        } else {
            statusView.setText("ACTIVE");
            statusView.setTextColor(Color.parseColor("#388E3C"));
        }
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }
}
