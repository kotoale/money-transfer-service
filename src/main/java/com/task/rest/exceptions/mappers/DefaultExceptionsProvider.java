package com.task.rest.exceptions.mappers;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Default provider that maps Java exceptions to {@link javax.ws.rs.core.Response}
 * By default all exceptions are matched response with status INTERNAL_SERVER_ERROR
 *
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 * @see javax.ws.rs.core.Response
 * @see javax.ws.rs.ext.ExceptionMapper
 */
public class DefaultExceptionsProvider implements ExceptionMapper<Exception> {
    public Response toResponse(Exception exception) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
                .entity(exception.getMessage())
                .type(MediaType.TEXT_PLAIN)
                .build();
    }
}
