package com.example.lottery_legend;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import android.graphics.Color;
import android.widget.TextView;

import com.example.lottery_legend.model.Event;
import com.example.lottery_legend.organizer.OrganizerEventAdapter;
import com.google.firebase.Timestamp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Method;
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
        mockedTimestamp.when(Timestamp::now).thenReturn(new Timestamp(1000000, 0));
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

    @Test
    public void testUpdateStatusUI_Closed() throws Exception {
        OrganizerEventAdapter adapter = new OrganizerEventAdapter(new ArrayList<>(), "dev1");
        Method method = OrganizerEventAdapter.class.getDeclaredMethod("updateStatusUI", TextView.class, Event.class);
        method.setAccessible(true);

        TextView statusView = mock(TextView.class);
        Event event = new Event();
        // Event started in the past (before 1000000)
        event.setEventStartAt(new Timestamp(500000, 0));

        method.invoke(adapter, statusView, event);
        verify(statusView).setText("CLOSED");
    }

    @Test
    public void testUpdateStatusUI_Drawn() throws Exception {
        OrganizerEventAdapter adapter = new OrganizerEventAdapter(new ArrayList<>(), "dev1");
        Method method = OrganizerEventAdapter.class.getDeclaredMethod("updateStatusUI", TextView.class, Event.class);
        method.setAccessible(true);

        TextView statusView = mock(TextView.class);
        Event event = new Event();
        event.setStatus("drawn");
        // Event starts in the future (after 1000000)
        event.setEventStartAt(new Timestamp(1500000, 0));

        method.invoke(adapter, statusView, event);
        verify(statusView).setText("DRAWN");
    }

    @Test
    public void testUpdateStatusUI_Active() throws Exception {
        OrganizerEventAdapter adapter = new OrganizerEventAdapter(new ArrayList<>(), "dev1");
        Method method = OrganizerEventAdapter.class.getDeclaredMethod("updateStatusUI", TextView.class, Event.class);
        method.setAccessible(true);

        TextView statusView = mock(TextView.class);
        Event event = new Event();
        event.setStatus("open");
        event.setEventStartAt(new Timestamp(1500000, 0));

        method.invoke(adapter, statusView, event);
        verify(statusView).setText("ACTIVE");
    }
}
