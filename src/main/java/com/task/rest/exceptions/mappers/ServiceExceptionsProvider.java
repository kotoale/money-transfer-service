package com.task.rest.exceptions.mappers;

import com.task.rest.exceptions.AbstractServiceException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Custom provider that maps Java exceptions
 * that extend {@link AbstractServiceException} to {@link javax.ws.rs.core.Response}
 *
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 * @see AbstractServiceException
 * @see javax.ws.rs.core.Response
 * @see javax.ws.rs.ext.ExceptionMapper
 */
public class ServiceExceptionsProvider implements ExceptionMapper<AbstractServiceException> {
    public Response toResponse(AbstractServiceException exception) {
        return Response.status(exception.getCode())
                .entity(exception.getMessage())
                .type(MediaType.TEXT_PLAIN)
                .build();
    }
}
