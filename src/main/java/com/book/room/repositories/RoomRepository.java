package com.book.room.repositories;

import com.book.room.models.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Integer> {

    @Query("SELECT r FROM Room r WHERE " +
            "(:name IS NULL OR r.name LIKE %:name%) AND " +
            "(:hasConditioner IS NULL OR r.hasConditioner = :hasConditioner) AND " +
            "(:hasProjector IS NULL OR r.hasProjector = :hasProjector) AND " +
            "(:hasFridge IS NULL OR r.hasFridge = :hasFridge) AND " +
            "(:hasBalcony IS NULL OR r.hasBalcony = :hasBalcony) AND " +
            "(:capacity IS NULL OR r.capacity >= :capacity)")
    List<Room> searchRooms(@Param("name") String name,
                           @Param("hasConditioner") Boolean hasConditioner,
                           @Param("hasProjector") Boolean hasProjector,
                           @Param("hasFridge") Boolean hasFridge,
                           @Param("hasBalcony") Boolean hasBalcony,
                           @Param("capacity") Integer capacity);

}
