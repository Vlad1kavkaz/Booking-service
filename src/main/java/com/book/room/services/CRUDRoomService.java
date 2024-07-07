package com.book.room.services;

import com.book.room.models.Room;
import com.book.room.repositories.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CRUDRoomService {

    @Autowired
    private RoomRepository roomRepository;

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public List<Room> searchRooms(String name,
                                  Boolean hasConditioner,
                                  Boolean hasProjector,
                                  Boolean hasFridge,
                                  Boolean hasBalcony,
                                  Integer capacity) throws SQLException {
        return roomRepository.searchRooms(
                name,
                hasConditioner,
                hasProjector,
                hasFridge,
                hasBalcony,
                capacity);
    }


    public void createRoom(Room room) {
        roomRepository.save(room);
    }

    public Optional<Room> updateRoom(Integer id, Room roomDetails) {
        return roomRepository.findById(id).map(room -> {
            room.setName(roomDetails.getName());
            room.setHasConditioner(roomDetails.getHasConditioner());
            room.setHasProjector(roomDetails.getHasProjector());
            room.setHasFridge(roomDetails.getHasFridge());
            room.setHasBalcony(roomDetails.getHasBalcony());
            room.setCapacity(roomDetails.getCapacity());
            return roomRepository.save(room);
        });
    }

    public void deleteRoom(Integer id) {
        roomRepository.deleteById(id);
    }

    public Room findById(Integer id) {
        return roomRepository.findById(id).orElse(null);
    }

    public List<Room> findAllRoomsExcept(Integer roomId) {
        // Получаем все комнаты, кроме комнаты с заданным идентификатором
        return roomRepository.findAll().stream()
                .filter(room -> !room.getId().equals(roomId))
                .collect(Collectors.toList());
    }
}
