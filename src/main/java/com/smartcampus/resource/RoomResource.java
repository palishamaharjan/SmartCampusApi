/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.DataStore;
import com.smartcampus.model.Room;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.util.*;

/**
 *
 * @author Dell
 */

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {
    
    private final Map<String, Room> rooms = DataStore.getRooms();
    
    @GET
    public Response getAllRooms() {
        return Response.ok(new ArrayList<>(rooms.values())).build();
    }
    
    @POST
    public Response createRoom (Room room) {
        if (room == null || room.getId() == null || room.getId().isBlank()){
            return Response.status(400)
                    .entity(Map.of("status", 400, "error", "Bad Request", "message", "Room must have a non-empty id")).build();
        }
        if (room.getName() == null || room.getName().isBlank()) {
            return Response.status(400)
                    .entity(Map.of("status", 400, "error", "Bad Request", "message", "Room must have a non-empty name.")).build();
        }
        if (rooms.containsKey(room.getId())){
            return Response.status(409)
                    .entity(Map.of("status", 409, "error", "Conflict", "message", "Room '"+ room.getId() + "' already exists.")).build();
        }
        rooms.put(room.getId(), room);
        return Response.created(URI.create("/api/v1/rooms/" + room.getId()))
                .entity(room).build();
    }
    
    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            return Response.status(404)
                    .entity(Map.of("staus", 404, "error", "Not Found", "message","Room '" + roomId +"' not found.")).build();
        }
        return Response.ok(room).build();
    }
    
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            return Response.status(404)
                    .entity(Map.of("status", 404, "error", "Not Found", "message", "Room '" + roomId + "' not found.")).build();
        }
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                    "Room '" + roomId + "' cannot be deleted. It still has " + room.getSensorIds().size() + " sensor(s) assigned to it."
            );
        }
        rooms.remove(roomId);
        return Response.noContent().build();
    }
}
