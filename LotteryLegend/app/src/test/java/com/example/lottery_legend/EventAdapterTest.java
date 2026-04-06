package com.example.lottery_legend;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.graphics.Color;
import android.widget.Button;
import android.widget.TextView;

import com.example.lottery_legend.event.EventAdapter;
import com.example.lottery_legend.model.Event;
import com.google.firebase.Timestamp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Method;
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
        // Set "now" to a fixed value for deterministic testing
        mockedTimestamp.when(Timestamp::now).thenReturn(new Timestamp(1000000, 0));

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

    @Test
    public void testUpdateStatusUI_Joined_Selected() throws Exception {
        Method method = EventAdapter.class.getDeclaredMethod("updateStatusUI", 
                EventAdapter.ViewHolder.class, Event.class, boolean.class, String.class, String.class);
        method.setAccessible(true);

        EventAdapter.ViewHolder holder = mock(EventAdapter.ViewHolder.class);
        holder.status = mock(TextView.class);
        holder.joinButton = mock(Button.class);

        Event event = new Event();
        event.setStatus("open");
        
        // Case: User joined and is selected
        method.invoke(adapter, holder, event, true, "selected", null);
        verify(holder.status).setText("Selected!");
    }

    @Test
    public void testUpdateStatusUI_Joined_Accepted() throws Exception {
        Method method = EventAdapter.class.getDeclaredMethod("updateStatusUI", 
                EventAdapter.ViewHolder.class, Event.class, boolean.class, String.class, String.class);
        method.setAccessible(true);

        EventAdapter.ViewHolder holder = mock(EventAdapter.ViewHolder.class);
        holder.status = mock(TextView.class);
        holder.joinButton = mock(Button.class);

        Event event = new Event();
        event.setStatus("open");

        // Case: User joined and accepted
        method.invoke(adapter, holder, event, true, "accepted", null);
        verify(holder.status).setText("Accepted");
    }

    @Test
    public void testUpdateStatusUI_Joined_Cancelled() throws Exception {
        Method method = EventAdapter.class.getDeclaredMethod("updateStatusUI", 
                EventAdapter.ViewHolder.class, Event.class, boolean.class, String.class, String.class);
        method.setAccessible(true);

        EventAdapter.ViewHolder holder = mock(EventAdapter.ViewHolder.class);
        holder.status = mock(TextView.class);
        holder.joinButton = mock(Button.class);

        Event event = new Event();
        event.setStatus("open");

        // Case: User joined but cancelled
        method.invoke(adapter, holder, event, true, "cancelled", null);
        verify(holder.status).setText("Cancelled/Declined");
    }

    @Test
    public void testUpdateStatusUI_NotSelected() throws Exception {
        Method method = EventAdapter.class.getDeclaredMethod("updateStatusUI", 
                EventAdapter.ViewHolder.class, Event.class, boolean.class, String.class, String.class);
        method.setAccessible(true);

        EventAdapter.ViewHolder holder = mock(EventAdapter.ViewHolder.class);
        holder.status = mock(TextView.class);
        holder.joinButton = mock(Button.class);

        Event event = new Event();
        event.setStatus("open");

        // Case: User joined but final result is LOSS
        method.invoke(adapter, holder, event, true, "waiting", "LOSS");
        verify(holder.status).setText("Not Selected");
    }

    @Test
    public void testUpdateStatusUI_Closed() throws Exception {
        Method method = EventAdapter.class.getDeclaredMethod("updateStatusUI", 
                EventAdapter.ViewHolder.class, Event.class, boolean.class, String.class, String.class);
        method.setAccessible(true);

        EventAdapter.ViewHolder holder = mock(EventAdapter.ViewHolder.class);
        holder.status = mock(TextView.class);
        holder.joinButton = mock(Button.class);
        // Mock cardContent to avoid NPE in setAlpha
        holder.cardContent = mock(android.widget.LinearLayout.class);

        Event event = new Event();
        // Event started in the past relative to our mocked "now" (1000000)
        event.setEventStartAt(new Timestamp(500000, 0));

        method.invoke(adapter, holder, event, false, null, null);
        verify(holder.status).setText("Closed");
    }
}
