package com.example.lottery_legend;

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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.UserViewHolder> {

    private Context context;
    private ArrayList<Object> userList;
    private FirebaseFirestore db;
    private String currentCollection = "entrants";

    public AdminUserAdapter(Context context, ArrayList<Object> userList) {
        this.context = context;
        this.userList = userList;
        this.db = FirebaseFirestore.getInstance();
    }

    public void setCurrentCollection(String collectionName) {
        this.currentCollection = collectionName;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_admin, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        Object userObj = userList.get(position);
        String name = "";
        Timestamp joinDate = null;
        String userId = "";

        if (userObj instanceof Entrant) {
            Entrant user = (Entrant) userObj;
            name = user.name;
            joinDate = user.joinDate;
            userId = user.userId;
        } else if (userObj instanceof Organizer) {
            Organizer user = (Organizer) userObj;
            name = user.getName();
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
        holder.removeButton.setOnClickListener(v -> showDeleteDialog(finalUserId));
    }

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

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userName, joinDate, removeButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_name_text);
            joinDate = itemView.findViewById(R.id.user_join_date_text);
            removeButton = itemView.findViewById(R.id.remove_user_button);
        }
    }
}