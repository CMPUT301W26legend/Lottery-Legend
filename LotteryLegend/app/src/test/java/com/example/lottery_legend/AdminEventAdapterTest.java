package com.example.lottery_legend;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import android.content.Context;

import com.example.lottery_legend.admin.AdminEventAdapter;
import com.example.lottery_legend.model.Event;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;

public class AdminEventAdapterTest {

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
        ArrayList<Event> eventList = new ArrayList<>();
        eventList.add(new Event());
        AdminEventAdapter adapter = new AdminEventAdapter(context, eventList);
        assertEquals(1, adapter.getItemCount());
    }
}
