/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Dell
 */

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {
    
    
    @GET
    public Response discover(){
        Map<String, Object> response = new HashMap<>();
        response.put("apiVersion", "1.0.0");
        response.put("name", "Smart Campus Sensor & Room Management API");
        response.put("description", "RESTful API for managing compus rooms and IoT sensors");
        response.put("status", "operational");
        
        Map<String, String> contact = new HashMap<>();
        contact.put("name", "Smart Campus Admin Team");
        contact.put("email", "campus-api@westminster.ac.uk");
        contact.put("department", "Facilities & Infrastruction");
        response.put("contact", contact);
        
        Map<String, String> resources = new HashMap<>();
        resources.put("roomns", "/api/v1/rooms");
        resources.put("sensors", "/api/v1/sensors");
        response.put("resources", resources);
        
        Map<String, String> links = new HashMap<>();
        links.put("self", "/api/v1");
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        response.put("_links", links);
        
        return Response.ok(response).build();
    }
}
