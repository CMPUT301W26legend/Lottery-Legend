package com.example.lottery_legend.entrant;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery_legend.R;
import com.example.lottery_legend.event.EventDetailsActivity;
import com.example.lottery_legend.model.Event;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Adapter for the RecyclerView displaying an entrant's event history.
 * Binds event data to card views and handles navigation to event details.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<Event> eventList;
    private final String currentDeviceId;

    /**
     * Constructs a new HistoryAdapter.
     * @param eventList       List of events to display.
     * @param deviceId        Unique identifier for the user's device.
     */
    public HistoryAdapter(List<Event> eventList, String deviceId) {
        this.eventList = eventList;
        this.currentDeviceId = deviceId;
    }

    /**
     * Updates the data set and refreshes the RecyclerView.
     * @param newList The new list of events.
     */
    public void updateList(List<Event> newList) {
        this.eventList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_card_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = eventList.get(position);
        String entrantStatus = determineEntrantStatus(event);

        holder.textEventTitle.setText(event.getTitle());
        holder.textStatus.setText(entrantStatus);

        // Apply specific text color based on participation status
        holder.textStatus.setTextColor(getStatusColor(entrantStatus));

        holder.textDateRange.setText(formatEventDateRange(event));

        // Display lottery draw date or TBD if not set
        if (event.getDrawAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            String drawDate = "Lottery draw: " + sdf.format(event.getDrawAt().toDate());
            holder.textLotteryDraw.setText(drawDate);
        } else {
            holder.textLotteryDraw.setText("Lottery draw: TBD");
        }

        // Toggle visibility of the "Private" tag based on event visibility
        if (event.isIsPrivateEvent()) {
            holder.tagPrivate.setVisibility(View.VISIBLE);
        } else {
            holder.tagPrivate.setVisibility(View.GONE);
        }

        // Navigate to details page when an item is clicked
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EventDetailsActivity.class);
            intent.putExtra("eventId", event.getEventId());
            intent.putExtra("deviceId", currentDeviceId);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * Computes the display status for an entrant within a specific event.
     * Maps internal participation codes to user-friendly strings.
     *
     * @param event The event to check.
     * @return A localized status string (e.g., "Waiting", "Accepted").
     */
    private String determineEntrantStatus(Event event) {
        if (event.getWaitingList() == null) return "Waiting";

        for (Event.WaitingListEntry entry : event.getWaitingList()) {
            if (Objects.equals(entry.getDeviceId(), currentDeviceId)) {
                if ("LOSS".equalsIgnoreCase(entry.getFinalResult())) {
                    return "Not Selected";
                }

                String fsStatus = entry.getParticipationStatus();
                if (fsStatus == null) return "Waiting";

                switch (fsStatus.toLowerCase()) {
                    case "waiting": return "Waiting";
                    case "selected":
                    case "invited": return "Waiting Response";
                    case "accepted":
                    case "enrolled": return "Accepted";
                    case "not_selected":
                    case "rejected": return "Not Selected";
                    case "cancelled": return "Cancelled";
                    case "declined": return "Declined";
                    default: return "Waiting";
                }
            }
        }
        return "Waiting";
    }

    /**
     * Returns the appropriate Color for a given status string.
     */
    private int getStatusColor(String displayStatus) {
        switch (displayStatus) {
            case "Waiting":
            case "Waiting Response":
                return Color.parseColor("#F59E0B"); // Amber
            case "Accepted":
                return Color.parseColor("#10B981"); // Green
            case "Not Selected":
                return Color.parseColor("#9CA3AF"); // Gray
            case "Cancelled":
            case "Declined":
                return Color.parseColor("#EF4444"); // Red
            default:
                return Color.BLACK;
        }
    }

    /**
     * Formats the event's start and end dates into a readable range string.
     */
    private String formatEventDateRange(Event event) {
        Timestamp start = event.getEventStartAt();
        Timestamp end = event.getEventEndAt();

        if (start == null) return "Date: TBD";

        SimpleDateFormat sdfMonthDay = new SimpleDateFormat("MMMM d", Locale.getDefault());
        SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy", Locale.getDefault());

        String startStr = sdfMonthDay.format(start.toDate());
        String yearStr = sdfYear.format(start.toDate());

        String result;
        if (end != null) {
            String endStr = sdfMonthDay.format(end.toDate());
            result = startStr + " - " + endStr + ", " + yearStr;
        } else {
            result = startStr + ", " + yearStr;
        }

        if (event.getCapacity() > 0) {
            result += " · " + event.getCapacity() + " capacity";
        }

        return result;
    }

    /**
     * ViewHolder for history event items.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textEventTitle, textStatus, textDateRange, textLotteryDraw, tagPrivate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textEventTitle = itemView.findViewById(R.id.textEventTitle);
            textStatus = itemView.findViewById(R.id.textStatus);
            textDateRange = itemView.findViewById(R.id.textDateRange);
            textLotteryDraw = itemView.findViewById(R.id.textLotteryDraw);
            tagPrivate = itemView.findViewById(R.id.tagPrivate);
        }
    }
}
