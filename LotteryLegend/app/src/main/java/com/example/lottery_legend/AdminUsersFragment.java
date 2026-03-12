package com.example.lottery_legend;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class AdminUsersFragment extends Fragment {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private AdminUserAdapter adapter;
    private ArrayList<Object> userList;
    private ListenerRegistration listenerRegistration;
    private String currentCollection = "entrants";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_users, container, false);

        db = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.admin_users_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userList = new ArrayList<>();
        adapter = new AdminUserAdapter(getContext(), userList);
        recyclerView.setAdapter(adapter);

        TabLayout tabLayout = view.findViewById(R.id.admin_users_tabs);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    currentCollection = "entrants";
                } else {
                    currentCollection = "organizers";
                }
                fetchUsers(currentCollection);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        fetchUsers(currentCollection);

        return view;
    }

    private void fetchUsers(String collectionName) {
        // Remove previous listener if exists
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }

        adapter.setCurrentCollection(collectionName);

        listenerRegistration = db.collection(collectionName).addSnapshotListener((value, error) -> {
            if (error != null) {
                return;
            }

            if (value != null) {
                userList.clear();
                for (QueryDocumentSnapshot doc : value) {
                    Object user;
                    if (collectionName.equals("entrants")) {
                        Entrant entrant = doc.toObject(Entrant.class);
                        if (entrant.getUserId() == null) {
                            entrant.setUserId(doc.getId());
                        }
                        user = entrant;
                    } else {
                        Organizer organizer = doc.toObject(Organizer.class);
                        if (organizer.getUserId() == null) {
                            organizer.setUserId(doc.getId());
                        }
                        user = organizer;
                    }
                    userList.add(user);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}
