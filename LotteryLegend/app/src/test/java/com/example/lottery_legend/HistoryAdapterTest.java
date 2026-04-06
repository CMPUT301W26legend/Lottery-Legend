package com.example.lottery_legend;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import android.graphics.Color;

import com.example.lottery_legend.entrant.HistoryAdapter;
import com.example.lottery_legend.model.Event;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class HistoryAdapterTest {

    private MockedStatic<Color> mockedColor;
    private HistoryAdapter adapter;
    private List<Event> eventList;
    private String deviceId = "testDevice";

    @Before
    public void setUp() {
        mockedColor = mockStatic(Color.class);
        mockedColor.when(() -> Color.parseColor(anyString())).thenReturn(0);

        eventList = new ArrayList<>();
        adapter = new HistoryAdapter(eventList, deviceId);
    }

    @After
    public void tearDown() {
        mockedColor.close();
    }

    @Test
    public void testGetItemCount() {
        eventList.add(new Event());
        eventList.add(new Event());
        assertEquals(2, adapter.getItemCount());
    }

    @Test
    public void testUpdateList() {
        List<Event> newList = new ArrayList<>();
        newList.add(new Event());
        
        HistoryAdapter spyAdapter = spy(adapter);
        doNothing().when(spyAdapter).notifyDataSetChanged();
        
        spyAdapter.updateList(newList);
        assertEquals(1, spyAdapter.getItemCount());
        verify(spyAdapter).notifyDataSetChanged();
    }

    @Test
    public void testDetermineEntrantStatus() throws Exception {
        Method method = HistoryAdapter.class.getDeclaredMethod("determineEntrantStatus", Event.class);
        method.setAccessible(true);

        Event event = new Event();
        List<Event.WaitingListEntry> waitingList = new ArrayList<>();
        
        // Case: No waiting list
        event.setWaitingList(null);
        assertEquals("Waiting", method.invoke(adapter, event));

        // Case: Not in waiting list
        event.setWaitingList(waitingList);
        assertEquals("Waiting", method.invoke(adapter, event));

        // Case: In waiting list, status waiting
        Event.WaitingListEntry entry = new Event.WaitingListEntry();
        entry.setDeviceId(deviceId);
        entry.setParticipationStatus("waiting");
        waitingList.add(entry);
        assertEquals("Waiting", method.invoke(adapter, event));

        // Case: Final result LOSS
        entry.setFinalResult("LOSS");
        assertEquals("Not Selected", method.invoke(adapter, event));
        entry.setFinalResult(null);

        // Case: Status invited
        entry.setParticipationStatus("invited");
        assertEquals("Waiting Response", method.invoke(adapter, event));

        // Case: Status selected
        entry.setParticipationStatus("selected");
        assertEquals("Waiting Response", method.invoke(adapter, event));

        // Case: Status accepted
        entry.setParticipationStatus("accepted");
        assertEquals("Accepted", method.invoke(adapter, event));

        // Case: Status enrolled
        entry.setParticipationStatus("enrolled");
        assertEquals("Accepted", method.invoke(adapter, event));

        // Case: Status cancelled
        entry.setParticipationStatus("cancelled");
        assertEquals("Cancelled", method.invoke(adapter, event));

        // Case: Status declined
        entry.setParticipationStatus("declined");
        assertEquals("Declined", method.invoke(adapter, event));

        // Case: Status rejected
        entry.setParticipationStatus("rejected");
        assertEquals("Not Selected", method.invoke(adapter, event));
    }
}
