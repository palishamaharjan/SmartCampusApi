/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resource;


import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.DataStore;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.*;

/**
 *
 * @author Dell
 */

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {
    
    private final String sensorId;
    private final Map<String, Sensor> sensors = DataStore.getSensors();
    private final Map<String, List<SensorReading>> readings = DataStore.getReadings();
    
    public SensorReadingResource(String sensorId) {
        this.sensorId= sensorId;
    }
    
    @GET
    public Response getReadings(){
        List<SensorReading> history = readings.getOrDefault(sensorId, new ArrayList<>());
        return Response.ok(history).build();
    }
    
    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = sensors.get(sensorId);
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Sensor '" + sensorId + "' is in MAINTENANCE mode and cannot accept readings."
            );
        }
        if (reading == null) {
            return Response.status(400)
                    .entity(Map.of("status", 400, "error", "Bad Request", "message", "reading body must include a value field.")).build();
        }
        if (reading.getId() == null || reading.getId().isBlank()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimeStamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }
        readings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
        sensor.setCurrentValue(reading.getValue());
        return Response.status(201).entity(reading).build();
    }
    
    @GET
    @Path("/{readingId}")
    public Response getReadingById(@PathParam("readingId") String readingId) {
        List<SensorReading> history = readings.getOrDefault(sensorId, new ArrayList<>());
        return history.stream()
                .filter(r -> r.getId().equals(readingId))
                .findFirst()
                .map(r -> Response.ok(r).build())
                .orElseGet(() -> Response.status(404).entity(Map.of("staus", 404, "error", "Not Found", "message", "Reading '" + readingId + "' not found.")).build());
    }
}
