package com.book.booking.models;

import com.book.room.models.Room;
import com.book.user.models.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "bookings")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "room_id", referencedColumnName = "id", nullable = false)
    private Room room;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Column(name = "start_date", nullable = false)
    private Date startDate;

    @Column(name = "end_date", nullable = false)
    private Date endDate;

    @Column(name = "current_capacity", nullable = false)
    private Integer currentCapacity;

    @Override
    public String toString() {
        return String.format(
                "Booking Details:%n" +
                        "----------------------------%n" +
                        "Booking ID: %d%n" +
                        "Room: %s%n" +
                        "User: %s%n" +
                        "Start Date: %s%n" +
                        "End Date: %s%n" +
                        "Current Capacity: %d%n" +
                        "----------------------------%n",
                id,
                room != null ? room.getName() : "N/A",
                user != null ? user.getUsername() : "N/A",
                startDate != null ? startDate.toString() : "N/A",
                endDate != null ? endDate.toString() : "N/A",
                currentCapacity
        );
    }


}
