package com.example.lottery_legend;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * This class is the adapter for the recycler view in the AdminUsersFragment.
 * It handles displaying the list of users and their details. It also handles
 * the deletion of users.
 */
public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {

    private Context context;
    private ArrayList<Object> userList;
    private FirebaseFirestore db;
    private String currentCollection = "entrants";

    /**
     * Constructor for AdminUserAdapter.
     *
     * @param context Context of the activity.
     * @param userList List of users to display.
     */

    public AdminUserAdapter(Context context, ArrayList<Object> userList) {
        this.context = context;
        this.userList = userList;
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Sets the current collection being displayed.
     *
     * @param collectionName Name of the collection.
     */
    public void setCurrentCollection(String collectionName) {
        this.currentCollection = collectionName;
    }

    /**
     * Inflates the item_user_layour to create a new ViewHolder
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new UserViewHolder that holds the inflated view.
     */
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_admin, parent, false);
        return new UserViewHolder(view);
    }

    /**
     * Binds the data to the ViewHolder. Also handles the distinction between
     * Entrant and Organizer models.
     *
     * @param holder   The ViewHolder to update
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        Object userObj = userList.get(position);
        String name = "";
        String email = "";
        String phone = "";
        Timestamp joinDate = null;
        String userId = "";

        if (userObj instanceof Entrant) {
            Entrant user = (Entrant) userObj;
            name = user.name;
            email = user.email;
            phone = user.phone;
            joinDate = user.joinDate;
            userId = user.userId;
        } else if (userObj instanceof Organizer) {
            Organizer user = (Organizer) userObj;
            name = user.getName();
            email = user.getEmail();
            phone = user.getPhone();
            joinDate = user.getJoinDate();
            userId = user.getUserId();
        }

        holder.userName.setText(name);
        
        if (joinDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault());
            String formattedDate = sdf.format(joinDate.toDate());
            holder.joinDate.setText("Joined: " + formattedDate);
        } else {
            holder.joinDate.setText("Joined: Unknown");
        }

        final String finalUserId = userId;
        final String finalName = name;
        final String finalEmail = email;
        final String finalPhone = phone;

        // Transition to Full View
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AdminUserDetailActivity.class);
            intent.putExtra("userId", finalUserId);
            intent.putExtra("name", finalName);
            intent.putExtra("email", finalEmail);
            intent.putExtra("phone", finalPhone);
            intent.putExtra("collectionName", currentCollection);
            context.startActivity(intent);
        });

        holder.removeButton.setOnClickListener(v -> showDeleteDialog(finalUserId));
    }

    /**
     * Shows a confirmation dialog before deleting a user.
     * @param userId The ID of the user to delete.
     */
    private void showDeleteDialog(String userId) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_admin_delete, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(context)
                .setView(dialogView)
                .create();

        TextView title = dialogView.findViewById(R.id.dialog_title);
        TextView message = dialogView.findViewById(R.id.dialog_message);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnDelete = dialogView.findViewById(R.id.btn_delete);

        if (currentCollection.equals("entrants")) {
            title.setText("Delete Entrant");
            message.setText("This will permanently remove this entrant.");
        } else {
            title.setText("Delete Organizer");
            message.setText("This will permanently remove this organizer.");
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnDelete.setOnClickListener(v -> {
            if (userId != null) {
                db.collection(currentCollection).document(userId).delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(context, "User removed", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Error removing user", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        });
            }
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
    }

    /**
     * Returns the number of items in the list.
     */
    @Override
    public int getItemCount() {
        return userList.size();
    }

    /**
     * ViewHolder class for the user list.
     */
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userName, joinDate, removeButton;

        /**
         * Constructor for UserViewHolder.
         * @param itemView The view for the ViewHolder.
         */
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name_text);
            joinDate = itemView.findViewById(R.id.user_join_date_text);
            removeButton = itemView.findViewById(R.id.remove_user_button);
        }
    }
}