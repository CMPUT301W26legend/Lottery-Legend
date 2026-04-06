package com.example.lottery_legend;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import android.graphics.Color;
import android.widget.TextView;

import com.example.lottery_legend.organizer.WaitingListAdapter;
import com.example.lottery_legend.organizer.WaitingListActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class WaitingListAdapterTest {

    private MockedStatic<Color> mockedColor;
    private WaitingListAdapter adapter;
    private List<WaitingListActivity.WaitingListUser> users;

    @Before
    public void setUp() {
        mockedColor = mockStatic(Color.class);
        mockedColor.when(() -> Color.parseColor(anyString())).thenReturn(0);

        users = new ArrayList<>();
        adapter = new WaitingListAdapter(users, mock(WaitingListAdapter.OnEntrantActionListener.class));
    }

    @After
    public void tearDown() {
        mockedColor.close();
    }

    @Test
    public void testGetItemCount() {
        users.add(mock(WaitingListActivity.WaitingListUser.class));
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void testUpdateList() {
        List<WaitingListActivity.WaitingListUser> newList = new ArrayList<>();
        newList.add(mock(WaitingListActivity.WaitingListUser.class));
        
        // Use spy to bypass the NPE in notifyDataSetChanged() which can happen in unit tests
        WaitingListAdapter spyAdapter = spy(adapter);
        doNothing().when(spyAdapter).notifyDataSetChanged();
        
        spyAdapter.updateList(newList);
        assertEquals(1, spyAdapter.getItemCount());
        verify(spyAdapter).notifyDataSetChanged();
    }

    @Test
    public void testUpdateStatusUI() throws Exception {
        Method method = WaitingListAdapter.class.getDeclaredMethod("updateStatusUI", WaitingListAdapter.ViewHolder.class, String.class);
        method.setAccessible(true);

        WaitingListAdapter.ViewHolder holder = mock(WaitingListAdapter.ViewHolder.class);
        holder.textStatus = mock(TextView.class);
        holder.textPromote = mock(TextView.class);

        // Case: waiting - verifies it executes without crash
        method.invoke(adapter, holder, "waiting");
        
        // Case: invited
        method.invoke(adapter, holder, "invited");
        
        // Case: accepted
        method.invoke(adapter, holder, "accepted");
    }
}
