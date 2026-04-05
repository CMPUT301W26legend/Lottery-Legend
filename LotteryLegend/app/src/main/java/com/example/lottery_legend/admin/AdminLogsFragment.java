package com.example.lottery_legend.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.lottery_legend.R;
import com.example.lottery_legend.model.Notification;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for the Admin Logs section. Displays a list of notification logs and allows searching.
 */
public class AdminLogsFragment extends Fragment {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private AdminLogsAdapter adapter;
    private List<Notification> allLogs = new ArrayList<>();
    private EditText searchBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_logs, container, false);

        db = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.admin_logs_recycler);
        searchBar = view.findViewById(R.id.search_bar_logs);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdminLogsAdapter(new ArrayList<>(), this::showLogDetail);
        recyclerView.setAdapter(adapter);

        fetchNotificationLogs();
        setupSearch();

        return view;
    }

    private void fetchNotificationLogs() {
        db.collection("notifications")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        allLogs.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Notification log = doc.toObject(Notification.class);
                            allLogs.add(log);
                        }
                        filter(searchBar.getText().toString());
                    }
                });
    }

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

    private void filter(String query) {
        List<Notification> filteredList = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (Notification log : allLogs) {
            String eventTitle = (log.getEventTitle() != null ? log.getEventTitle() : "").toLowerCase();
            String senderName = (log.getSenderName() != null ? log.getSenderName() : "").toLowerCase();
            if (eventTitle.contains(lowerQuery) || senderName.contains(lowerQuery)) {
                filteredList.add(log);
            }
        }
        adapter.updateList(filteredList);
    }

    private void showLogDetail(Notification log) {
        NotificationDetailFragment detailFragment = NotificationDetailFragment.newInstance(log);
        detailFragment.show(getChildFragmentManager(), "NotificationDetail");
    }
}
