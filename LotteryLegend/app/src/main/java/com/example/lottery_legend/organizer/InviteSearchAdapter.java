package com.example.lottery_legend.organizer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
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

/**
 * Adapter for the search results in the invitation process.
 * Displays a list of entrants and allows selecting those who are not already participating in the event.
 */
public class InviteSearchAdapter extends RecyclerView.Adapter<InviteSearchAdapter.ViewHolder> {

    private final List<Entrant> entrants;
    private final Map<String, String> entrantStatuses;
    private final Set<String> selectedEntrantIds = new HashSet<>();

    /**
     * Constructor for InviteSearchAdapter.
     * @param entrants       List of entrants to display as search results.
     * @param entrantStatuses Map of entrant IDs to their current participation statuses for the event.
     */
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

    /**
     * Binds entrant data to the view holder.
     * Checks if the entrant is already in the event and disables selection if they are.
     *
     * @param holder   The ViewHolder to update.
     * @param position The position of the item in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Entrant entrant = entrants.get(position);
        holder.textName.setText(entrant.getName());
        holder.textEmail.setText(entrant.getEmail());
        holder.textPhone.setText(entrant.getPhone());

        // Load profile image
        if (entrant.getProfileImage() != null && !entrant.getProfileImage().isEmpty()) {
            displayBase64Image(entrant.getProfileImage(), holder.imageProfile);
        } else {
            holder.imageProfile.setImageResource(R.drawable.ic_profile_avatar);
        }

        String status = entrantStatuses.get(entrant.getDeviceId());

        // Determine if the user is in a state where they cannot be invited again
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
            // Entrant can be invited
            holder.textStatusLabel.setVisibility(View.GONE);
            holder.checkInvite.setVisibility(View.VISIBLE);
            holder.itemView.setEnabled(true);
            holder.itemView.setAlpha(1.0f);
            
            holder.checkInvite.setChecked(selectedEntrantIds.contains(entrant.getDeviceId()));

            // Toggle selection on item click
            holder.itemView.setOnClickListener(v -> {
                if (selectedEntrantIds.contains(entrant.getDeviceId())) {
                    selectedEntrantIds.remove(entrant.getDeviceId());
                } else {
                    selectedEntrantIds.add(entrant.getDeviceId());
                }
                notifyItemChanged(position);
            });

            // Handle explicit checkbox click
            holder.checkInvite.setOnClickListener(v -> {
                if (holder.checkInvite.isChecked()) {
                    selectedEntrantIds.add(entrant.getDeviceId());
                } else {
                    selectedEntrantIds.remove(entrant.getDeviceId());
                }
            });
        }
    }

    /**
     * Decodes a Base64 string and displays it in an ImageView.
     * @param base64    The Base64 encoded image string.
     * @param imageView The target ImageView.
     */
    private void displayBase64Image(String base64, ImageView imageView) {
        try {
            byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            if (bitmap != null && imageView != null) {
                imageView.setImageBitmap(bitmap);
            } else if (imageView != null) {
                imageView.setImageResource(R.drawable.ic_profile_avatar);
            }
        } catch (Exception ignored) {
            if (imageView != null) {
                imageView.setImageResource(R.drawable.ic_profile_avatar);
            }
        }
    }

    @Override
    public int getItemCount() {
        return entrants.size();
    }

    /**
     * Returns a list of unique identifiers for all currently selected entrants.
     * @return List of selected device IDs.
     */
    public List<String> getSelectedEntrantIds() {
        return new ArrayList<>(selectedEntrantIds);
    }

    /**
     * ViewHolder class for entrant search results.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textEmail, textPhone, textStatusLabel;
        CheckBox checkInvite;
        ImageView imageProfile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textEmail = itemView.findViewById(R.id.textEmail);
            textPhone = itemView.findViewById(R.id.textPhone);
            textStatusLabel = itemView.findViewById(R.id.textStatusLabel);
            checkInvite = itemView.findViewById(R.id.checkInvite);
            imageProfile = itemView.findViewById(R.id.imageProfile);
        }
    }
}
