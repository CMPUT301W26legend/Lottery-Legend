package com.example.lottery_legend.admin;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lottery_legend.R;
import com.example.lottery_legend.model.Event;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment for the Admin Media section. Displays a list of event posters and allows deletion.
 */
public class AdminMediaFragment extends Fragment {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private AdminMediaAdapter adapter;
    private List<Event> allEvents = new ArrayList<>();
    private Map<String, String> organizerNames = new HashMap<>();
    private EditText searchBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_media, container, false);

        db = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.admin_media_recycler);
        searchBar = view.findViewById(R.id.search_bar_media);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdminMediaAdapter(new ArrayList<>(), this::showDeleteDialog);
        recyclerView.setAdapter(adapter);

        fetchEventsWithImages();
        setupSearch();

        return view;
    }

    /**
     * Fetches all events from the database and updates the list of events with images.
     */
    private void fetchEventsWithImages() {
        db.collection("events")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        allEvents.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Event event = doc.toObject(Event.class);
                            if (event.getPosterImage() != null && !event.getPosterImage().isEmpty()) {
                                allEvents.add(event);
                                fetchOrganizerName(event.getOrganizerId());
                            }
                        }
                        filter(searchBar.getText().toString());
                    }
                });
    }

    /**
     * Fetches and caches the organizer name for a given ID.
     */
    private void fetchOrganizerName(String organizerId) {
        if (organizerNames.containsKey(organizerId)) return;

        db.collection("organizers").document(organizerId).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String name = doc.getString("name");
                if (name != null) {
                    organizerNames.put(organizerId, name);
                    String currentQuery = searchBar.getText().toString();
                    if (!currentQuery.isEmpty()) {
                        filter(currentQuery);
                    }
                }
            }
        });
    }

    /**
     * Sets up the search bar to filter the list of events.
     */
    private void setupSearch() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Filters the list of events based on the search query (event title or organizer name).
     * @param query The search query.
     */
    private void filter(String query) {
        List<Event> filteredList = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (Event event : allEvents) {
            String eventTitle = event.getTitle().toLowerCase();
            String organizerName = organizerNames.getOrDefault(event.getOrganizerId(), "").toLowerCase();

            if (eventTitle.contains(lowerQuery) || organizerName.contains(lowerQuery)) {
                filteredList.add(event);
            }
        }
        adapter.updateList(filteredList);
    }

    /**
     * Shows a dialog to confirm the deletion of an event poster.
     * @param event The event whose poster is to be deleted.
     */
    private void showDeleteDialog(Event event) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_admin_delete_image, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(getContext())
                .setView(dialogView)
                .create();

        ImageView ivPreview = dialogView.findViewById(R.id.dialog_image_preview);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnDelete = dialogView.findViewById(R.id.btn_delete);

        // Set preview image
        try {
            byte[] decodedString = Base64.decode(event.getPosterImage(), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            ivPreview.setImageBitmap(bitmap);
        } catch (Exception e) {
            ivPreview.setImageResource(R.drawable.img_poster);
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnDelete.setOnClickListener(v -> {
            db.collection("events").document(event.getEventId())
                    .update("posterImage", null)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Image removed", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
        });

        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }
}
