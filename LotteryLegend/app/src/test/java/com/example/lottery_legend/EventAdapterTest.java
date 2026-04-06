package com.example.lottery_legend;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import android.graphics.Color;

import com.example.lottery_legend.event.EventAdapter;
import com.example.lottery_legend.model.Event;
import com.google.firebase.Timestamp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;

public class EventAdapterTest {

    private MockedStatic<Color> mockedColor;
    private MockedStatic<Timestamp> mockedTimestamp;
    private EventAdapter adapter;
    private List<Event> eventList;
    private String deviceId = "testDevice";

    @Before
    public void setUp() {
        mockedColor = mockStatic(Color.class);
        mockedColor.when(() -> Color.parseColor(anyString())).thenReturn(0);

        mockedTimestamp = mockStatic(Timestamp.class);
        mockedTimestamp.when(Timestamp::now).thenReturn(new Timestamp(1000, 0));

        eventList = new ArrayList<>();
        adapter = new EventAdapter(eventList, deviceId);
    }

    @After
    public void tearDown() {
        mockedColor.close();
        mockedTimestamp.close();
    }

    @Test
    public void testGetItemCount() {
        eventList.add(new Event());
        assertEquals(1, adapter.getItemCount());
    }

    // Logic in updateStatusUI is complex and involves UI updates.
    // Testing it would require Robolectric or instrumented tests for full coverage.
    // However, we can test the data-driven part if we isolate it.
}
