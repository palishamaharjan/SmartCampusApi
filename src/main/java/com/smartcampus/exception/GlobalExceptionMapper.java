/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.exception;


import com.smartcampus.model.ErrorResponse;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Dell
 */

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable>{
    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());
    
    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof WebApplicationException) {
            return ((WebApplicationException) exception). getResponse();
        }
        LOGGER.log(Level.SEVERE, "Unexpected error: " + exception.getMessage(), exception);
        return Response.status(500)
                .entity(new ErrorResponse(500, "Internal Server Error", "An unexpected error occured. Please contact the adminstrator.", System.currentTimeMillis()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
