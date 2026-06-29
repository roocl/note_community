package org.notes.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.notes.exception.NotFoundException;
import org.notes.mapper.EsSyncFailureMapper;
import org.notes.mapper.NoteMapper;
import org.notes.mapper.UserMapper;
import org.notes.model.entity.EsSyncFailure;
import org.notes.model.entity.Note;
import org.notes.model.entity.User;
import org.notes.model.es.NoteDocument;
import org.notes.model.es.UserDocument;
import org.notes.repository.NoteSearchRepository;
import org.notes.repository.UserSearchRepository;
import org.notes.service.CategoryService;
import org.notes.service.QuestionService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EsSyncFailureServiceImplTest {

    @Mock
    private EsSyncFailureMapper esSyncFailureMapper;
    @Mock
    private NoteMapper noteMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private NoteSearchRepository noteSearchRepository;
    @Mock
    private UserSearchRepository userSearchRepository;
    @Mock
    private QuestionService questionService;
    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private EsSyncFailureServiceImpl esSyncFailureService;

    private EsSyncFailure noteFailure;

    @BeforeEach
    void setUp() {
        noteFailure = new EsSyncFailure();
        noteFailure.setId(1L);
        noteFailure.setEntityType("NOTE");
        noteFailure.setEntityId(10L);
        noteFailure.setOperation("SAVE");
        noteFailure.setRetryCount(0);
    }

    @Test
    void recordFailure_insertsPendingFailure() {
        esSyncFailureService.recordFailure("NOTE", 10L, "SAVE", new RuntimeException("es down"));

        verify(esSyncFailureMapper).insert(argThat(failure ->
                "NOTE".equals(failure.getEntityType())
                        && Long.valueOf(10L).equals(failure.getEntityId())
                        && "SAVE".equals(failure.getOperation())
                        && "PENDING".equals(failure.getStatus())
                        && failure.getErrorMessage().contains("es down")
        ));
    }

    @Test
    void recordFailure_updatesExistingOpenFailure() {
        when(esSyncFailureMapper.findOpen("NOTE", 10L, "SAVE")).thenReturn(noteFailure);

        esSyncFailureService.recordFailure("NOTE", 10L, "SAVE", new RuntimeException("es down again"));

        verify(esSyncFailureMapper).markFailed(1L, "es down again", 0);
        verify(esSyncFailureMapper, never()).insert(any());
    }

    @Test
    void retryFailure_savesNoteDocumentAndMarksSuccess() {
        Note note = new Note();
        note.setNoteId(10);
        note.setQuestionId(1);
        when(esSyncFailureMapper.findById(1L)).thenReturn(noteFailure);
        when(noteMapper.findById(10)).thenReturn(note);

        esSyncFailureService.retryFailure(1L);

        verify(noteSearchRepository).save(any(NoteDocument.class));
        verify(esSyncFailureMapper).markSuccess(1L);
    }

    @Test
    void retryFailure_recordsRetryFailureWhenEsStillFails() {
        User user = new User();
        user.setUserId(7L);
        EsSyncFailure failure = new EsSyncFailure();
        failure.setId(2L);
        failure.setEntityType("USER");
        failure.setEntityId(7L);
        failure.setOperation("SAVE");
        failure.setRetryCount(2);
        when(esSyncFailureMapper.findById(2L)).thenReturn(failure);
        when(userMapper.findById(7L)).thenReturn(user);
        when(userSearchRepository.save(any(UserDocument.class))).thenThrow(new RuntimeException("still down"));

        esSyncFailureService.retryFailure(2L);

        verify(esSyncFailureMapper).markFailed(eq(2L), contains("still down"), eq(3));
    }

    @Test
    void retryFailure_throwsWhenFailureMissing() {
        when(esSyncFailureMapper.findById(99L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> esSyncFailureService.retryFailure(99L));
    }

    @Test
    void retryPendingFailures_retriesEachPendingFailure() {
        when(esSyncFailureMapper.findPending(10)).thenReturn(List.of(noteFailure));
        Note note = new Note();
        note.setNoteId(10);
        when(esSyncFailureMapper.findById(1L)).thenReturn(noteFailure);
        when(noteMapper.findById(10)).thenReturn(note);

        esSyncFailureService.retryPendingFailures(10);

        verify(esSyncFailureMapper).markSuccess(1L);
    }

    @Test
    void reconcileNotes_recordsSaveFailureWhenEsDocumentMissing() {
        Note note = new Note();
        note.setNoteId(10);
        note.setAuthorId(1L);
        note.setQuestionId(2);
        note.setContent("content");
        when(noteMapper.findAll()).thenReturn(List.of(note));
        when(noteSearchRepository.findById(10)).thenReturn(Optional.empty());
        when(noteSearchRepository.findAll()).thenReturn(List.of());

        int mismatchCount = esSyncFailureService.reconcileNotes();

        assertEquals(1, mismatchCount);
        verify(esSyncFailureMapper).insert(argThat(failure ->
                "NOTE".equals(failure.getEntityType())
                        && Long.valueOf(10L).equals(failure.getEntityId())
                        && "SAVE".equals(failure.getOperation())
        ));
    }

    @Test
    void reconcileUsers_recordsDeleteFailureForOrphanEsDocument() {
        UserDocument doc = new UserDocument();
        doc.setUserId(7L);
        when(userMapper.findAll()).thenReturn(List.of());
        when(userSearchRepository.findAll()).thenReturn(List.of(doc));
        when(userMapper.findById(7L)).thenReturn(null);

        int mismatchCount = esSyncFailureService.reconcileUsers();

        assertEquals(1, mismatchCount);
        verify(esSyncFailureMapper).insert(argThat(failure ->
                "USER".equals(failure.getEntityType())
                        && Long.valueOf(7L).equals(failure.getEntityId())
                        && "DELETE".equals(failure.getOperation())
        ));
    }
}
