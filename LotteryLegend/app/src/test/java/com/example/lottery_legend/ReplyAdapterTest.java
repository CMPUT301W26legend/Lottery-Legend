package com.example.lottery_legend;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import android.content.Context;

import com.example.lottery_legend.entrant.ReplyAdapter;
import com.example.lottery_legend.model.Comment;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;

public class ReplyAdapterTest {

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

    @Test
    public void testGetItemCount() {
        Context context = mock(Context.class);
        ReplyAdapter adapter = new ReplyAdapter(context, "ENTRANT", "dev1", false, mock(ReplyAdapter.OnReplyInteractionListener.class));
        
        // Use spy to bypass notifyDataSetChanged() NPE in unit tests
        ReplyAdapter spyAdapter = spy(adapter);
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
        ReplyAdapter adapter = new ReplyAdapter(context, "ENTRANT", "dev1", false, mock(ReplyAdapter.OnReplyInteractionListener.class));
        
        ReplyAdapter spyAdapter = spy(adapter);
        doNothing().when(spyAdapter).notifyDataSetChanged();

        spyAdapter.setReplies(null);
        assertEquals(0, spyAdapter.getItemCount());
    }
}
