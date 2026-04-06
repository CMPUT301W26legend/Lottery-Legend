package com.example.lottery_legend;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import android.text.TextUtils;
import android.text.format.DateUtils;

import com.example.lottery_legend.entrant.NotificationAdapter;
import com.example.lottery_legend.model.Notification;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class NotificationAdapterTest {

    private MockedStatic<TextUtils> mockedTextUtils;
    private MockedStatic<DateUtils> mockedDateUtils;
    private NotificationAdapter adapter;
    private List<Notification> notifications;

    @Before
    public void setUp() {
        mockedTextUtils = mockStatic(TextUtils.class);
        mockedTextUtils.when(() -> TextUtils.isEmpty(any())).thenAnswer(invocation -> {
            CharSequence s = invocation.getArgument(0);
            return s == null || s.length() == 0;
        });

        mockedDateUtils = mockStatic(DateUtils.class);

        notifications = new ArrayList<>();
        adapter = new NotificationAdapter(notifications, notification -> {});
    }

    @After
    public void tearDown() {
        mockedTextUtils.close();
        mockedDateUtils.close();
    }

    @Test
    public void testGetItemCount() {
        notifications.add(new Notification());
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void testResolveTitle() throws Exception {
        Method method = NotificationAdapter.NotificationViewHolder.class.getDeclaredMethod("resolveTitle", Notification.class, String.class);
        method.setAccessible(true);
        
        Notification notification = new Notification();
        
        // Case: Title is present
        notification.setTitle("Custom Title");
        assertEquals("Custom Title", method.invoke(null, notification, "TYPE"));

        // Case: No title, specific types
        notification.setTitle(null);
        assertEquals("Lottery Result", method.invoke(null, notification, "LOTTERY_WIN"));
        assertEquals("Lottery Result", method.invoke(null, notification, "LOTTERY_LOSE"));
        assertEquals("Sign-up Invitation", method.invoke(null, notification, "SIGN_UP_MESSAGE"));
        assertEquals("Waiting List Update", method.invoke(null, notification, "WAITLIST_MESSAGE"));
        assertEquals("Selected Group Message", method.invoke(null, notification, "SELECTED_MESSAGE"));
        assertEquals("Cancellation Update", method.invoke(null, notification, "CANCELLED_MESSAGE"));
        assertEquals("Co-organizer Invitation", method.invoke(null, notification, "CO_ORGANIZER_INVITE"));
        assertEquals("Private Event Invitation", method.invoke(null, notification, "PRIVATE_INVITE"));
        assertEquals("General Notification", method.invoke(null, notification, "GENERIC_ANNOUNCEMENT"));
        assertEquals("Notification", method.invoke(null, notification, "UNKNOWN"));
    }

    @Test
    public void testResolveMessage() throws Exception {
        Method method = NotificationAdapter.NotificationViewHolder.class.getDeclaredMethod("resolveMessage", Notification.class, String.class, String.class);
        method.setAccessible(true);

        Notification notification = new Notification();
        notification.setMessage("Invite");

        // Case: SIGN_UP_MESSAGE with status ACCEPTED
        assertEquals("Invite • Accepted", method.invoke(null, notification, "SIGN_UP_MESSAGE", "ACCEPTED"));
        
        // Case: SIGN_UP_MESSAGE with status DECLINED
        assertEquals("Invite • Declined", method.invoke(null, notification, "SIGN_UP_MESSAGE", "DECLINED"));
        
        // Case: Already has status
        notification.setMessage("Invite • Accepted");
        assertEquals("Invite • Accepted", method.invoke(null, notification, "SIGN_UP_MESSAGE", "ACCEPTED"));

        // Case: Other type (should just return message)
        notification.setMessage("Win!");
        assertEquals("Win!", method.invoke(null, notification, "LOTTERY_WIN", "ANY"));
    }
}
