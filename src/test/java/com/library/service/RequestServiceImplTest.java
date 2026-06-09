package com.library.service;

import com.library.dao.BookRequestDao;
import com.library.exception.ServiceException;
import com.library.model.BookRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link RequestServiceImpl}, focused on the parts that do not require a
 * live database transaction: request submission validation and cancellation rules.
 */
@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {

    @Mock
    private BookRequestDao requestDao;

    @InjectMocks
    private RequestServiceImpl requestService;

    @Test
    void submitRequest_validType_createsRequest() {
        when(requestDao.create(any(BookRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        BookRequest result = requestService.submitRequest(1, 2, "HOME");

        assertEquals(1, result.getReaderId());
        assertEquals(2, result.getBookId());
        assertEquals("HOME", result.getRequestType());
        verify(requestDao).create(any(BookRequest.class));
    }

    @Test
    void submitRequest_invalidType_throwsException() {
        assertThrows(ServiceException.class,
                () -> requestService.submitRequest(1, 2, "INVALID_TYPE"));
        verify(requestDao, never()).create(any(BookRequest.class));
    }

    @Test
    void cancelRequest_ownPendingRequest_cancels() {
        BookRequest req = new BookRequest();
        req.setRequestId(10);
        req.setReaderId(1);
        req.setStatus("PENDING");
        when(requestDao.findById(10)).thenReturn(Optional.of(req));

        requestService.cancelRequest(10, 1);

        assertEquals("CANCELLED", req.getStatus());
        verify(requestDao).update(req);
    }

    @Test
    void cancelRequest_otherUsersRequest_throwsException() {
        BookRequest req = new BookRequest();
        req.setRequestId(10);
        req.setReaderId(1);
        req.setStatus("PENDING");
        when(requestDao.findById(10)).thenReturn(Optional.of(req));

        // reader 2 tries to cancel reader 1's request
        assertThrows(ServiceException.class, () -> requestService.cancelRequest(10, 2));
        verify(requestDao, never()).update(any(BookRequest.class));
    }

    @Test
    void cancelRequest_alreadyIssued_throwsException() {
        BookRequest req = new BookRequest();
        req.setRequestId(10);
        req.setReaderId(1);
        req.setStatus("ISSUED");
        when(requestDao.findById(10)).thenReturn(Optional.of(req));

        assertThrows(ServiceException.class, () -> requestService.cancelRequest(10, 1));
    }

    @Test
    void cancelRequest_notFound_throwsException() {
        when(requestDao.findById(404)).thenReturn(Optional.empty());

        assertThrows(ServiceException.class, () -> requestService.cancelRequest(404, 1));
    }
}