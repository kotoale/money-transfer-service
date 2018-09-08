package com.task.rest.exceptions.mappers;

import com.task.rest.exceptions.AbstractServiceException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * @author Alexander Kotov (kotov.alex.22@gmail.com)
 */
public class ServiceExceptionsProvider implements ExceptionMapper<AbstractServiceException> {
    public Response toResponse(AbstractServiceException exception) {
        return Response.status(exception.getCode())
                .entity(exception.getMessage())
                .type(MediaType.TEXT_PLAIN)
                .build();
    }
}
