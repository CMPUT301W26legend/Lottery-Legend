package com.example.lottery_legend.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery_legend.R;
import com.example.lottery_legend.model.Event;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Adapter for the admin events recycler view.
 */
public class AdminEventAdapter extends RecyclerView.Adapter<AdminEventAdapter.EventViewHolder> {

    private Context context;
    private ArrayList<Event> eventList;
    private FirebaseFirestore db;

    /**
     * Constructor for the adapter.
     * @param context The context of the activity.
     * @param eventList The list of events to display.
     */
    public AdminEventAdapter(Context context, ArrayList<Event> eventList) {
        this.context = context;
        this.eventList = eventList;
        this.db = FirebaseFirestore.getInstance();
    }


    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event_admin, parent, false);
        return new EventViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);

        holder.title.setText(event.getTitle());
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        String dateStr = event.getEventStartAt() != null ? sdf.format(event.getEventStartAt().toDate()) : "N/A";
        String info = String.format(Locale.getDefault(), "%s • %d capacity", dateStr, event.getCapacity());
        holder.info.setText(info);
        
        int waiting = (event.getWaitingList() != null) ? event.getWaitingList().size() : 0;
        holder.waitingCount.setText(String.format(Locale.getDefault(), "%d waiting", waiting));

        holder.removeButton.setOnClickListener(v -> showDeleteDialog(event));
        
        holder.itemView.setOnClickListener(v -> {
            if (context instanceof AdminActivity) {
                ((AdminActivity) context).showEventDetail(event.getEventId());
            }
        });
    }

    /**
     * Shows a dialog to confirm the deletion of an event. If the admin confirms, the event is
     * removed from the database.
     * @param event The event to delete.
     */
    private void showDeleteDialog(Event event) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_admin_delete, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setView(dialogView)
                .create();

        TextView title = dialogView.findViewById(R.id.dialog_title);
        TextView message = dialogView.findViewById(R.id.dialog_message);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnDelete = dialogView.findViewById(R.id.btn_delete);

        title.setText("Delete Event");
        message.setText("This will permanently remove this event");

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnDelete.setOnClickListener(v -> {
            db.collection("events").document(event.getEventId()).delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Event removed", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Error removing event", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
    }


    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * ViewHolder for the admin events recycler view.
     */
    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView title, info, waitingCount, removeButton;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.event_title);
            info = itemView.findViewById(R.id.event_info);
            waitingCount = itemView.findViewById(R.id.waiting_count);
            removeButton = itemView.findViewById(R.id.remove_button);
        }
    }
}
