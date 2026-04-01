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
 * Adapter for displaying entrant's event participation history.
 * Binds existing Event model data directly to the card layout.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<Event> eventList;
    private final String currentDeviceId;

    public HistoryAdapter(List<Event> eventList, String deviceId) {
        this.eventList = eventList;
        this.currentDeviceId = deviceId;
    }

    /**
     * Updates the data set and refreshes the RecyclerView.
     */
    public void updateList(List<Event> newList) {
        this.eventList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // history_card_content.xml is the merged layout with MaterialCardView as root
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_card_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = eventList.get(position);
        String entrantStatus = determineEntrantStatus(event);

        holder.textEventTitle.setText(event.getTitle());
        holder.textStatus.setText(entrantStatus);

        // Set status color based on computed status text
        holder.textStatus.setTextColor(getStatusColor(entrantStatus));

        // Format Date Range
        holder.textDateRange.setText(formatEventDateRange(event));

        // Format Lottery Draw Date
        if (event.getDrawAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            String drawDate = "Lottery draw: " + sdf.format(event.getDrawAt().toDate());
            holder.textLotteryDraw.setText(drawDate);
        } else {
            holder.textLotteryDraw.setText("Lottery draw: TBD");
        }

        // Open details on click
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
     * Determines the specific status string for the current entrant in the given event.
     */
    private String determineEntrantStatus(Event event) {
        if (event.getWaitingList() == null) return "Waiting";

        for (Event.WaitingListEntry entry : event.getWaitingList()) {
            if (Objects.equals(entry.getDeviceId(), currentDeviceId)) {
                String fsStatus = entry.getParticipationStatus();
                if (fsStatus == null) return "Waiting";

                switch (fsStatus.toLowerCase()) {
                    case "waiting": return "Waiting";
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

    private int getStatusColor(String displayStatus) {
        switch (displayStatus) {
            case "Waiting":
            case "Waiting Response":
                return Color.parseColor("#F59E0B"); // Amber/Orange
            case "Accepted":
                return Color.parseColor("#10B981"); // Green
            case "Not Selected":
                return Color.parseColor("#6B7280"); // Gray
            case "Cancelled":
            case "Declined":
                return Color.parseColor("#EF4444"); // Red
            default:
                return Color.BLACK;
        }
    }

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

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textEventTitle, textStatus, textDateRange, textLotteryDraw;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textEventTitle = itemView.findViewById(R.id.textEventTitle);
            textStatus = itemView.findViewById(R.id.textStatus);
            textDateRange = itemView.findViewById(R.id.textDateRange);
            textLotteryDraw = itemView.findViewById(R.id.textLotteryDraw);
        }
    }
}
