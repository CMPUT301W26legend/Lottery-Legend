package com.example.lottery_legend;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying event posters in the Admin Media section. Displays the event title,
 * organizer name, and a remove button. Also handles the deletion of events.
 */
public class AdminMediaAdapter extends RecyclerView.Adapter<AdminMediaAdapter.MediaViewHolder> {

    private List<Event> eventList;
    private final OnDeleteClickListener deleteClickListener;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface OnDeleteClickListener {
        void onDeleteClick(Event event);
    }

    /**
     * Constructor for the adapter.
     * @param eventList The list of events to display.
     * @param deleteClickListener The click listener for the delete button.
     */
    public AdminMediaAdapter(List<Event> eventList, OnDeleteClickListener deleteClickListener) {
        this.eventList = eventList;
        this.deleteClickListener = deleteClickListener;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_media, parent, false);
        return new MediaViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.tvEventTitle.setText(event.getTitle());
        
        db.collection("organizers").document(event.getOrganizerId()).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                holder.tvOrganizerName.setText("Organizer: " + doc.getString("name"));
            } else {
                holder.tvOrganizerName.setText("Organizer: Unknown");
            }
        });

        if (event.getPosterImage() != null && !event.getPosterImage().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(event.getPosterImage(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.ivPoster.setImageBitmap(bitmap);
            } catch (Exception e) {
                holder.ivPoster.setImageResource(R.drawable.img_poster);
            }
        } else {
            holder.ivPoster.setImageResource(R.drawable.img_poster);
        }

        holder.btnRemove.setOnClickListener(v -> deleteClickListener.onDeleteClick(event));
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * Updates the list of events to display.
     * @param newList The new list of events to display.
     */
    public void updateList(List<Event> newList) {
        this.eventList = newList;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder for the admin media recycler view.
     */
    static class MediaViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPoster;
        TextView tvEventTitle, tvOrganizerName, btnRemove;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPoster = itemView.findViewById(R.id.media_poster_image);
            tvEventTitle = itemView.findViewById(R.id.media_event_title);
            tvOrganizerName = itemView.findViewById(R.id.media_organizer_name);
            btnRemove = itemView.findViewById(R.id.btn_remove_media);
        }
    }
}
