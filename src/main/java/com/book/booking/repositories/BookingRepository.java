package com.book.booking.repositories;


import com.book.booking.models.Booking;
import com.book.room.models.Room;
import com.book.user.models.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
    List<Booking> findByRoomId(Integer roomId);

    // Метод для поиска пересекающихся бронирований
    List<Booking> findByRoomIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Integer roomId, Date endDate, Date startDate);

    @EntityGraph(attributePaths = {"room", "user"})
    Optional<Booking> findById(Integer id);

    @Transactional
    @Modifying
    @Query("UPDATE Booking b SET " +
            "b.room = :room, " +
            "b.user = :user, " +
            "b.startDate = :startDate, " +
            "b.endDate = :endDate, " +
            "b.currentCapacity = :currentCapacity " +
            "WHERE b.id = :bookingId")
    void updateBooking(@Param("bookingId") Integer bookingId,
                       @Param("room") Room room,
                       @Param("user") User user,
                       @Param("startDate") Date startDate,
                       @Param("endDate") Date endDate,
                       @Param("currentCapacity") Integer currentCapacity);
}
