package com.example.lottery_legend;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adapter for displaying events in the Organizer History.
 * Directly handles navigation to event details.
 */
public class OrganizerEventAdapter extends RecyclerView.Adapter<OrganizerEventAdapter.ViewHolder> {

    private final List<Event> eventList;
    private final String deviceId;

    /**
     * ViewHolder class that holds references to the UI components for an individual event item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView eventName;
        TextView status;
        TextView eventInfo;
        TextView waiting;
        TextView selected;

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
        }
    }

    /**
     * Constructs an OrganizerEventAdapter.
     * @param eventList List of events to display.
     * @param deviceId  The organizer's device ID.
     */
    public OrganizerEventAdapter(List<Event> eventList, String deviceId) {
        this.eventList = eventList;
        this.deviceId = deviceId;
    }

    /**
     * Inflates the layout for a single event card in the organizer's history.
     * @param parent   The parent view group.
     * @param viewType The type of view (unused).
     * @return A new ViewHolder instance.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.organizer_event_card_content, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds the event data to the views in the ViewHolder.
     * Configures status formatting, summary strings, and click listeners for navigation.
     * @param holder   The ViewHolder to update.
     * @param position The index of the item in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Event event = eventList.get(position);

        holder.eventName.setText(event.getTitle());
        
        String statusText = event.getStatus() != null ? event.getStatus().toUpperCase() : "OPEN";
        holder.status.setText(statusText);
        holder.status.setTextColor(Color.parseColor("#2563EB"));
        
        String info = event.getRegistrationEndDate() + " · " + event.getCapacity() + " capacity";
        holder.eventInfo.setText(info);
        
        int waitingCount = (event.getWaitingList() != null) ? event.getWaitingList().size() : 0;
        holder.waiting.setText(waitingCount + " waiting");
        
        holder.selected.setText("0 selected");

        // Handle item click directly by starting the details activity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), OrganizerEventDetailsActivity.class);
            intent.putExtra("eventId", event.getEventId());
            intent.putExtra("eventTitle", event.getTitle());
            intent.putExtra("deviceId", deviceId);
            v.getContext().startActivity(intent);
        });
    }

    /**
     * Returns the total number of events in the list.
     * @return The size of the event list.
     */
    @Override
    public int getItemCount() {
        return eventList.size();
    }
}
