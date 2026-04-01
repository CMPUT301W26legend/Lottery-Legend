package com.example.lottery_legend.organizer;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery_legend.R;
import com.example.lottery_legend.model.Entrant;
import com.example.lottery_legend.model.Event;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class WaitingListAdapter extends RecyclerView.Adapter<WaitingListAdapter.ViewHolder> {

    private List<WaitingListActivity.WaitingListUser> users;
    private final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault());
    private final OnEntrantActionListener listener;

    public interface OnEntrantActionListener {
        void onPromote(WaitingListActivity.WaitingListUser user);
        void onCancelSelection(WaitingListActivity.WaitingListUser user);
    }

    public WaitingListAdapter(List<WaitingListActivity.WaitingListUser> users, OnEntrantActionListener listener) {
        this.users = users;
        this.listener = listener;
    }

    public void updateList(List<WaitingListActivity.WaitingListUser> newList) {
        this.users = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_waiting_list_entrant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WaitingListActivity.WaitingListUser user = users.get(position);
        Entrant entrant = user.entrant;
        Event.WaitingListEntry entry = user.entry;

        holder.textName.setText(entrant.getName());
        
        String rawStatus = entry.getParticipationStatus() != null ? entry.getParticipationStatus().toLowerCase() : "waiting";
        updateStatusUI(holder, rawStatus);

        if (entry.getJoinedAt() != null) {
            holder.textJoinedTime.setText("Joined: " + sdf.format(entry.getJoinedAt().toDate()));
        } else {
            holder.textJoinedTime.setText("Joined: Unknown");
        }

        holder.textPromote.setOnClickListener(v -> {
            if ("waiting".equals(rawStatus)) {
                listener.onPromote(user);
            } else if ("invited".equals(rawStatus) || "selected".equals(rawStatus)) {
                listener.onCancelSelection(user);
            }
        });
    }

    private void updateStatusUI(ViewHolder holder, String status) {
        switch (status) {
            case "waiting":
                holder.textStatus.setText("Waiting");
                holder.textStatus.setTextColor(Color.parseColor("#1976D2"));
                holder.textPromote.setVisibility(View.VISIBLE);
                holder.textPromote.setText("Promote to Co-organizer");
                holder.textPromote.setTextColor(Color.parseColor("#1976D2"));
                break;
            case "invited":
            case "selected":
                holder.textStatus.setText("Selected");
                holder.textStatus.setTextColor(Color.parseColor("#F57C00"));
                holder.textPromote.setVisibility(View.VISIBLE);
                holder.textPromote.setText("Cancel Selection");
                holder.textPromote.setTextColor(Color.parseColor("#EF4444"));
                break;
            case "accepted":
            case "enrolled":
                holder.textStatus.setText("Accepted");
                holder.textStatus.setTextColor(Color.parseColor("#388E3C"));
                holder.textPromote.setVisibility(View.GONE);
                break;
            case "cancelled":
                holder.textStatus.setText("Cancelled");
                holder.textStatus.setTextColor(Color.parseColor("#EF4444"));
                holder.textPromote.setVisibility(View.GONE);
                break;
            case "declined":
                holder.textStatus.setText("Declined");
                holder.textStatus.setTextColor(Color.parseColor("#EF4444"));
                holder.textPromote.setVisibility(View.GONE);
                break;
            case "not selected":
                holder.textStatus.setText("Not Selected");
                holder.textStatus.setTextColor(Color.parseColor("#6B7280"));
                holder.textPromote.setVisibility(View.GONE);
                break;
            default:
                holder.textStatus.setText(status.substring(0, 1).toUpperCase() + status.substring(1));
                holder.textStatus.setTextColor(Color.GRAY);
                holder.textPromote.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageProfile;
        TextView textName, textStatus, textJoinedTime, textPromote;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageProfile = itemView.findViewById(R.id.imageEntrantProfile);
            textName = itemView.findViewById(R.id.textEntrantName);
            textStatus = itemView.findViewById(R.id.textStatus);
            if (textStatus == null) {
                textStatus = itemView.findViewById(R.id.textEntrantStatus);
            }
            textJoinedTime = itemView.findViewById(R.id.textJoinedTime);
            textPromote = itemView.findViewById(R.id.textPromoteCoOrganizer);
        }
    }
}
