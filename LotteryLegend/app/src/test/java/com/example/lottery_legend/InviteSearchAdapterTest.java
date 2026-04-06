package com.example.lottery_legend;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import com.example.lottery_legend.model.Entrant;
import com.example.lottery_legend.organizer.InviteSearchAdapter;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InviteSearchAdapterTest {

    @Test
    public void testGetItemCount() {
        List<Entrant> entrants = new ArrayList<>();
        entrants.add(new Entrant());
        Map<String, String> statuses = new HashMap<>();
        InviteSearchAdapter adapter = new InviteSearchAdapter(entrants, statuses);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void testGetSelectedEntrantIds() {
        List<Entrant> entrants = new ArrayList<>();
        Entrant e1 = new Entrant();
        e1.setDeviceId("d1");
        entrants.add(e1);
        Map<String, String> statuses = new HashMap<>();
        InviteSearchAdapter adapter = new InviteSearchAdapter(entrants, statuses);
        
        // This is a bit tricky to test without actual view binding, 
        // but we can test the internal set if it were exposed or via public methods if any.
        // Currently selectedEntrantIds is private.
        assertEquals(0, adapter.getSelectedEntrantIds().size());
    }
}
