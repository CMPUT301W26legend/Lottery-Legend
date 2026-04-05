package com.example.lottery_legend.organizer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery_legend.R;
import com.example.lottery_legend.model.Entrant;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InviteSearchAdapter extends RecyclerView.Adapter<InviteSearchAdapter.ViewHolder> {

    private final List<Entrant> entrants;
    private final Map<String, String> entrantStatuses;
    private final Set<String> selectedEntrantIds = new HashSet<>();

    public InviteSearchAdapter(List<Entrant> entrants, Map<String, String> entrantStatuses) {
        this.entrants = entrants;
        this.entrantStatuses = entrantStatuses;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result_entrant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Entrant entrant = entrants.get(position);
        holder.textName.setText(entrant.getName());
        holder.textEmail.setText(entrant.getEmail());
        holder.textPhone.setText(entrant.getPhone());

        String status = entrantStatuses.get(entrant.getDeviceId());

        // Determine if the user is in a "fixed" state (already in the event)
        boolean isAlreadyInEvent = status != null &&
                (status.equalsIgnoreCase("waiting") ||
                        status.equalsIgnoreCase("invited") ||
                        status.equalsIgnoreCase("selected") ||
                        status.equalsIgnoreCase("accepted"));

        if (isAlreadyInEvent) {
            holder.textStatusLabel.setVisibility(View.VISIBLE);
            holder.checkInvite.setVisibility(View.GONE);
            holder.itemView.setEnabled(false);
            holder.itemView.setAlpha(0.6f);

            if (status.equalsIgnoreCase("waiting")) {
                holder.textStatusLabel.setText("In Waitlist");
            } else if (status.equalsIgnoreCase("accepted")) {
                holder.textStatusLabel.setText("Joined");
            } else {
                holder.textStatusLabel.setText("Invited"); // Covers "invited" and "selected"
            }
        } else {
            // Entrant is new, declined, or cancelled - they can be invited
            holder.textStatusLabel.setVisibility(View.GONE);
            holder.checkInvite.setVisibility(View.VISIBLE);
            holder.itemView.setEnabled(true);
            holder.itemView.setAlpha(1.0f);
            
            holder.checkInvite.setChecked(selectedEntrantIds.contains(entrant.getDeviceId()));

            holder.itemView.setOnClickListener(v -> {
                if (selectedEntrantIds.contains(entrant.getDeviceId())) {
                    selectedEntrantIds.remove(entrant.getDeviceId());
                } else {
                    selectedEntrantIds.add(entrant.getDeviceId());
                }
                notifyItemChanged(position);
            });

            holder.checkInvite.setOnClickListener(v -> {
                if (holder.checkInvite.isChecked()) {
                    selectedEntrantIds.add(entrant.getDeviceId());
                } else {
                    selectedEntrantIds.remove(entrant.getDeviceId());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return entrants.size();
    }

    public List<String> getSelectedEntrantIds() {
        return new ArrayList<>(selectedEntrantIds);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textEmail, textPhone, textStatusLabel;
        CheckBox checkInvite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textEmail = itemView.findViewById(R.id.textEmail);
            textPhone = itemView.findViewById(R.id.textPhone);
            textStatusLabel = itemView.findViewById(R.id.textStatusLabel);
            checkInvite = itemView.findViewById(R.id.checkInvite);
        }
    }
}