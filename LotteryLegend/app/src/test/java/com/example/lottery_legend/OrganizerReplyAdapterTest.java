package com.example.lottery_legend;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import android.content.Context;

import com.example.lottery_legend.model.Comment;
import com.example.lottery_legend.organizer.OrganizerReplyAdapter;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class OrganizerReplyAdapterTest {

    @Test
    public void testGetItemCount() {
        Context context = mock(Context.class);
        OrganizerReplyAdapter adapter = new OrganizerReplyAdapter(context, "ORGANIZER", "dev1", mock(OrganizerReplyAdapter.OnReplyInteractionListener.class));
        
        // Use spy to bypass notifyDataSetChanged() NPE in unit tests
        OrganizerReplyAdapter spyAdapter = spy(adapter);
        doNothing().when(spyAdapter).notifyDataSetChanged();

        List<Comment> replies = new ArrayList<>();
        replies.add(new Comment());
        spyAdapter.setReplies(replies);
        
        assertEquals(1, spyAdapter.getItemCount());
        verify(spyAdapter).notifyDataSetChanged();
    }

    @Test
    public void testSetRepliesNull() {
        Context context = mock(Context.class);
        OrganizerReplyAdapter adapter = new OrganizerReplyAdapter(context, "ORGANIZER", "dev1", mock(OrganizerReplyAdapter.OnReplyInteractionListener.class));
        
        OrganizerReplyAdapter spyAdapter = spy(adapter);
        doNothing().when(spyAdapter).notifyDataSetChanged();

        spyAdapter.setReplies(null);
        assertEquals(0, spyAdapter.getItemCount());
    }
}
