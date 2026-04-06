package com.example.lottery_legend;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import android.graphics.Color;

import com.example.lottery_legend.model.Event;
import com.example.lottery_legend.organizer.OrganizerEventAdapter;
import com.google.firebase.Timestamp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;

public class OrganizerEventAdapterTest {

    private MockedStatic<Color> mockedColor;
    private MockedStatic<Timestamp> mockedTimestamp;

    @Before
    public void setUp() {
        mockedColor = mockStatic(Color.class);
        mockedColor.when(() -> Color.parseColor(anyString())).thenReturn(0);

        mockedTimestamp = mockStatic(Timestamp.class);
        mockedTimestamp.when(Timestamp::now).thenReturn(new Timestamp(1000, 0));
    }

    @After
    public void tearDown() {
        mockedColor.close();
        mockedTimestamp.close();
    }

    @Test
    public void testGetItemCount() {
        List<Event> eventList = new ArrayList<>();
        eventList.add(new Event());
        OrganizerEventAdapter adapter = new OrganizerEventAdapter(eventList, "dev1");
        assertEquals(1, adapter.getItemCount());
    }
}
