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

import java.lang.reflect.Field;
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
        Context context = mock(Context.class);
        ArrayList<Event> eventList = new ArrayList<>();
        eventList.add(new Event());
        AdminEventAdapter adapter = new AdminEventAdapter(context, eventList);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void testUpdateList() {
        Context context = mock(Context.class);
        ArrayList<Event> eventList = new ArrayList<>();
        AdminEventAdapter adapter = new AdminEventAdapter(context, eventList);
        fixAdapter(adapter);
        
        ArrayList<Event> newList = new ArrayList<>();
        newList.add(new Event());
        adapter.updateList(newList);

        assertEquals(1, adapter.getItemCount());
    }
}
