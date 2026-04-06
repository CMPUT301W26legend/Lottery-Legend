package com.example.lottery_legend;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.example.lottery_legend.admin.AdminLogsAdapter;
import com.example.lottery_legend.model.Notification;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class AdminLogsAdapterTest {

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

    private void fixAdapter(androidx.recyclerview.widget.RecyclerView.Adapter<?> adapter) {
        try {
            Field mObservableField = androidx.recyclerview.widget.RecyclerView.Adapter.class.getDeclaredField("mObservable");
            mObservableField.setAccessible(true);
            Object mObservable = mObservableField.get(adapter);
            Field mObserversField = mObservable.getClass().getSuperclass().getDeclaredField("mObservers");
            mObserversField.setAccessible(true);
            mObserversField.set(mObservable, new ArrayList<>());
        } catch (Exception ignored) {}
    }

    @Test
    public void testGetItemCount() {
        List<Notification> logs = new ArrayList<>();
        logs.add(new Notification());
        logs.add(new Notification());
        AdminLogsAdapter adapter = new AdminLogsAdapter(logs, log -> {});
        assertEquals(2, adapter.getItemCount());
    }

    @Test
    public void testUpdateList() {
        List<Notification> logs = new ArrayList<>();
        AdminLogsAdapter adapter = new AdminLogsAdapter(logs, log -> {});
        fixAdapter(adapter);
        
        List<Notification> newLogs = new ArrayList<>();
        newLogs.add(new Notification());

        adapter.updateList(newLogs);
        
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void testReceiverDisplayLogic() {
        Notification log = new Notification();
        log.setType("LOTTERY_WIN");
        assertEquals("Selected/Accepted Users", log.getReceiverGroup());

        log.setRecipientName("John");
        String receiverDisplay = log.getReceiverGroup();
        if (log.getRecipientName() != null) {
            receiverDisplay = log.getRecipientName() + " (" + receiverDisplay + ")";
        }
        assertEquals("John (Selected/Accepted Users)", receiverDisplay);
    }
}
