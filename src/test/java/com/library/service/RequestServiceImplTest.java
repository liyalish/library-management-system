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

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {

    @Mock
    private BookRequestDao requestDao;

    @InjectMocks
    private RequestServiceImpl requestService;

    @Test
    void submitRequest_validType_createsRequest() {
        when(requestDao.countActiveByReader(1)).thenReturn(0);
        when(requestDao.create(any(BookRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        BookRequest result = requestService.submitRequest(1, 2, "HOME");

        assertEquals(1, result.getReaderId());
        assertEquals(2, result.getBookId());
        assertEquals("HOME", result.getRequestType());
        assertEquals("PENDING", result.getStatus());

        verify(requestDao).countActiveByReader(1);
        verify(requestDao).create(any(BookRequest.class));
    }

    @Test
    void submitRequest_invalidType_throwsException() {
        assertThrows(ServiceException.class,
                () -> requestService.submitRequest(1, 2, "INVALID_TYPE"));

        verify(requestDao, never()).create(any(BookRequest.class));
    }

    @Test
    void submitRequest_whenReaderHasFiveActiveRequests_throwsException() {
        when(requestDao.countActiveByReader(1)).thenReturn(5);

        assertThrows(ServiceException.class,
                () -> requestService.submitRequest(1, 2, "HOME"));

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

        assertThrows(ServiceException.class,
                () -> requestService.cancelRequest(10, 2));

        verify(requestDao, never()).update(any(BookRequest.class));
    }

    @Test
    void cancelRequest_alreadyIssued_throwsException() {
        BookRequest req = new BookRequest();
        req.setRequestId(10);
        req.setReaderId(1);
        req.setStatus("ISSUED");

        when(requestDao.findById(10)).thenReturn(Optional.of(req));

        assertThrows(ServiceException.class,
                () -> requestService.cancelRequest(10, 1));

        verify(requestDao, never()).update(any(BookRequest.class));
    }

    @Test
    void cancelRequest_notFound_throwsException() {
        when(requestDao.findById(404)).thenReturn(Optional.empty());

        assertThrows(ServiceException.class,
                () -> requestService.cancelRequest(404, 1));
    }

    @Test
    void requestReturn_ownIssuedRequest_setsPendingReturn() {
        BookRequest req = new BookRequest();
        req.setRequestId(20);
        req.setReaderId(1);
        req.setStatus("ISSUED");

        when(requestDao.findById(20)).thenReturn(Optional.of(req));

        requestService.requestReturn(20, 1);

        assertEquals("PENDING_RETURN", req.getStatus());
        verify(requestDao).update(req);
    }

    @Test
    void requestReturn_otherUsersRequest_throwsException() {
        BookRequest req = new BookRequest();
        req.setRequestId(20);
        req.setReaderId(1);
        req.setStatus("ISSUED");

        when(requestDao.findById(20)).thenReturn(Optional.of(req));

        assertThrows(ServiceException.class,
                () -> requestService.requestReturn(20, 2));

        verify(requestDao, never()).update(any(BookRequest.class));
    }

    @Test
    void requestReturn_notIssued_throwsException() {
        BookRequest req = new BookRequest();
        req.setRequestId(20);
        req.setReaderId(1);
        req.setStatus("PENDING");

        when(requestDao.findById(20)).thenReturn(Optional.of(req));

        assertThrows(ServiceException.class,
                () -> requestService.requestReturn(20, 1));

        verify(requestDao, never()).update(any(BookRequest.class));
    }
}