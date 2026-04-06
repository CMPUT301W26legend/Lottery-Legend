package com.example.lottery_legend;

import com.example.lottery_legend.model.Comment;
import com.google.firebase.Timestamp;
import org.junit.Test;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;

/**
 * Unit test for the Comment model class.
 */
public class CommentUnitTest {

    @Test
    public void testCommentGettersAndSetters() {
        Comment comment = new Comment();
        
        String commentId = "comment123";
        String authorId = "author456";
        String authorType = "ENTRANT";
        String authorName = "John Doe";
        String content = "This is a comment.";
        Timestamp now = new Timestamp(new Date());
        String parentId = "parent789";
        String rootId = "root000";
        int level = 1;
        String replyToId = "user111";
        String replyToName = "Jane Doe";
        
        comment.setCommentId(commentId);
        comment.setAuthorId(authorId);
        comment.setAuthorType(authorType);
        comment.setAuthorNameSnapshot(authorName);
        comment.setContent(content);
        comment.setCreatedAt(now);
        comment.setUpdatedAt(now);
        comment.setParentCommentId(parentId);
        comment.setRootCommentId(rootId);
        comment.setThreadLevel(level);
        comment.setReplyToUserId(replyToId);
        comment.setReplyToUserNameSnapshot(replyToName);
        
        comment.setLikeCount(10);
        comment.setLoveCount(5);
        comment.setHelpfulCount(2);
        comment.setReactionCount(17);
        comment.setReplyCount(3);
        
        Map<String, Integer> reactionCounts = new HashMap<>();
        reactionCounts.put("like", 10);
        comment.setReactionTypeCounts(reactionCounts);

        assertEquals(commentId, comment.getCommentId());
        assertEquals(authorId, comment.getAuthorId());
        assertEquals(authorType, comment.getAuthorType());
        assertEquals(authorName, comment.getAuthorNameSnapshot());
        assertEquals(content, comment.getContent());
        assertEquals(now, comment.getCreatedAt());
        assertEquals(now, comment.getUpdatedAt());
        assertEquals(parentId, comment.getParentCommentId());
        assertEquals(rootId, comment.getRootCommentId());
        assertEquals(level, comment.getThreadLevel());
        assertEquals(replyToId, comment.getReplyToUserId());
        assertEquals(replyToName, comment.getReplyToUserNameSnapshot());
        
        assertEquals(10, comment.getLikeCount());
        assertEquals(5, comment.getLoveCount());
        assertEquals(2, comment.getHelpfulCount());
        assertEquals(17, comment.getReactionCount());
        assertEquals(3, comment.getReplyCount());
        assertEquals(reactionCounts, comment.getReactionTypeCounts());
    }
}
