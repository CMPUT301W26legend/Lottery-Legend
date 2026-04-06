package com.example.lottery_legend;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import android.content.Context;

import com.example.lottery_legend.admin.AdminUserAdapter;
import com.example.lottery_legend.model.Entrant;
import com.example.lottery_legend.model.Organizer;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;

public class AdminUserAdapterTest {

    private MockedStatic<FirebaseFirestore> mockedFirestore;

    @Before
    public void setUp() {
        mockedFirestore = mockStatic(FirebaseFirestore.class);
        mockedFirestore.when(FirebaseFirestore::getInstance).thenReturn(mock(FirebaseFirestore.class));
    }

    @After
    public void tearDown() {
        mockedFirestore.close();
    }

    @Test
    public void testGetItemCount() {
        Context context = mock(Context.class);
        ArrayList<Object> userList = new ArrayList<>();
        userList.add(new Entrant());
        AdminUserAdapter adapter = new AdminUserAdapter(context, userList);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void testSetCurrentCollection() {
        Context context = mock(Context.class);
        AdminUserAdapter adapter = new AdminUserAdapter(context, new ArrayList<>());
        adapter.setCurrentCollection("organizers");
        // Verify it doesn't crash
    }

    @Test
    public void testMultipleUserTypes() {
        Context context = mock(Context.class);
        ArrayList<Object> userList = new ArrayList<>();
        
        Entrant e = new Entrant();
        e.setName("Entrant");
        userList.add(e);
        
        Organizer o = new Organizer();
        o.setName("Organizer");
        userList.add(o);
        
        AdminUserAdapter adapter = new AdminUserAdapter(context, userList);
        assertEquals(2, adapter.getItemCount());
    }
}
