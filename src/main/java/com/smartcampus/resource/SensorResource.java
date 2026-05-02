/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.DataStore;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author Dell
 */

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {
    
    private final Map<String, Room> rooms = DataStore.getRooms();
    private final Map<String, Sensor> sensors = DataStore.getSensors();
    
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> result = new ArrayList<>(sensors.values());
        if (type != null && !type.isBlank()) {
            result = result.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }
        return Response.ok(result).build();
    }
    
    @POST
    public Response createSensor(Sensor sensor){
        if (sensor == null || sensor.getId() == null || sensor.getId().isBlank()) {
            return Response.status(400)
                    .entity(Map.of("status", 400, "error", "Bad Request", "message", "Sensor must have a non-empty id.")).build();
        }
        if (sensors.containsKey(sensor.getId())){
            return Response.status(409)
                    .entity(Map.of("status", 409, "error", "Conflict","message", "Sensor '" + sensor.getId() + "' already exists.")).build();
        }
        if (sensor.getRoomId() == null || !rooms.containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                    "Cannot register sensor: roomId '" + sensor.getRoomId() + "' does not refer to an existing room."
            );
        }
        if (sensor.getStatus() == null || sensor.getStatus().isBlank()) {
            sensor.setStatus("ACTIVE");
        }
        sensors.put(sensor.getId(), sensor);
        rooms.get(sensor.getRoomId()).addSensorId(sensor.getId());
        DataStore.getReadings().put(sensor.getId(), new ArrayList<>());
        return Response.created(URI.create("/api/v1/sensors/" + sensor.getId()))
                .entity(sensor).build();
    }
    
    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = sensors.get(sensorId);
        if (sensor == null) {
            return Response.status(404)
                    .entity(Map.of("status", 404, "error", "Not Found","message", "Sensor '" + sensorId + "' not found.")).build();
        }
        return Response.ok(sensor).build();
    }
    
    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = sensors.get(sensorId);
        if (sensor ==null){
            return Response.status(404)
                    .entity(Map.of("status", 404, "error", "Not Found", "message", "Sensor '" + sensorId + "' not found.")).build();
        }
        Room parentRoom = rooms.get(sensor.getRoomId());
        if (parentRoom != null) {
            parentRoom.removeSensorId(sensorId);
        }
        sensors.remove(sensorId);
        DataStore.getReadings().remove(sensorId);
        return Response.noContent().build();
    }
    
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingsResource(@PathParam("sensorId") String sensorId) {
        if(!sensors.containsKey(sensorId)) {
            throw new NotFoundException("Sensor '" + sensorId + "' not found.");
        }
        return new SensorReadingResource(sensorId);
    }
}
