package com.example.lottery_legend;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.example.lottery_legend.admin.AdminMediaAdapter;
import com.example.lottery_legend.model.Event;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;

public class AdminMediaAdapterTest {

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
        List<Event> eventList = new ArrayList<>();
        eventList.add(new Event());
        AdminMediaAdapter adapter = new AdminMediaAdapter(eventList, event -> {});
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void testUpdateList() {
        List<Event> eventList = new ArrayList<>();
        AdminMediaAdapter adapter = new AdminMediaAdapter(eventList, event -> {});
        
        List<Event> newList = new ArrayList<>();
        newList.add(new Event());

        // Use spy to bypass the NPE in notifyDataSetChanged() which can happen in unit tests
        AdminMediaAdapter spyAdapter = spy(adapter);
        doNothing().when(spyAdapter).notifyDataSetChanged();

        spyAdapter.updateList(newList);
        
        assertEquals(1, spyAdapter.getItemCount());
        verify(spyAdapter).notifyDataSetChanged();
    }
}
