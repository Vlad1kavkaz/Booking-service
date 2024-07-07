package com.book.room.controllers;

import com.book.room.models.Room;
import com.book.room.services.CRUDRoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/room")
@Slf4j
public class RoomController {

    @Autowired
    private CRUDRoomService crudRoomService;

    @GetMapping("/get-all-rooms")
    public List<Room> getAllRooms() {
        log.info("Fetching all rooms");
        return crudRoomService.getAllRooms();
    }

    @GetMapping("/get-room")
    private List<Room> searchRooms(
            @RequestParam(name = "capacity", required = false) Integer capacity,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "has-conditioner", required = false) Boolean hasConditioner,
            @RequestParam(name = "has-projector", required = false) Boolean hasProjector,
            @RequestParam(name = "has-fridge", required = false) Boolean hasFridge,
            @RequestParam(name = "has-balcony", required = false) Boolean hasBalcony) throws SQLException {

        log.info("Searching for rooms with parameters: name={}, hasConditioner={}, hasProjector={}, hasFridge={}, hasBalcony={}, capacity={}",
                name, hasConditioner, hasProjector, hasFridge, hasBalcony, capacity);
        return crudRoomService.searchRooms(
                name,
                hasConditioner,
                hasProjector,
                hasFridge,
                hasBalcony,
                capacity
        );
    }

    @PostMapping("/create-room")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> createRoom(@RequestBody Room room) {
        log.info("Creating a new room");
        crudRoomService.createRoom(room);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update-room/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Room> updateRoom(@PathVariable Integer id, @RequestBody Room roomDetails) {
        log.info("Updating room with id: {}", id);
        return crudRoomService.updateRoom(id, roomDetails)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/delete-room/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRoom(@PathVariable Integer id) {
        log.info("Deleting room with id: {}", id);
        crudRoomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }
}
