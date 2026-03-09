package com.example.lottery_legend;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Date;

/**
 * Unit test for Firestore interactions using Mockito.
 *
 */
public class FirestoreUnitTest {

    @Mock
    private FirebaseFirestore mockDb;
    
    @Mock
    private CollectionReference mockCollection;
    
    @Mock
    private DocumentReference mockDocument;
    
    @Mock
    private Task<Void> mockTask;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        when(mockDb.collection(anyString())).thenReturn(mockCollection);
        when(mockCollection.document(anyString())).thenReturn(mockDocument);
        when(mockDocument.set(any())).thenReturn(mockTask);
    }

    @Test
    public void testFirestoreSetDataCalled() {
        Entrant entrant = new Entrant("Test User", "test@example.com", "12345", true, "test_user_id", new Timestamp(new Date()));
        String deviceId = "test_device_id";

        mockDb.collection("entrants").document(deviceId).set(entrant);

        verify(mockDb).collection("entrants");
        verify(mockCollection).document(deviceId);
        verify(mockDocument).set(entrant);
    }
}