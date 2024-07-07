package com.book.booking.controllers;

import com.book.booking.models.Booking;
import com.book.booking.services.BookingService;
import com.book.notifacations.controllers.EmailController;
import com.book.notifacations.dto.EmailContext;
import com.book.notifacations.messages.Messages;
import com.book.room.models.Room;
import com.book.room.services.CRUDRoomService;
import com.book.user.models.User;
import com.book.user.services.UserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/booking")
@Slf4j
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final EmailController emailController;
    private final UserService userService;
    private final CRUDRoomService roomService;

    private List<Booking> bookings;

    @PostConstruct
    public void init() {
        this.bookings = getAllBookings();
    }

    @GetMapping("/get-all-bookings")
    public List<Booking> getAllBookings() {
        log.info("Fetching all bookings");
        return bookingService.getAllBookings();
    }

    @PostMapping("/create-booking")
    public ResponseEntity<String> createBooking(@RequestBody Booking booking) {
        log.info("Creating a new booking");
        bookingService.createBooking(booking);

        User user = userService.findById(booking.getUser().getId()).get();
        String email = user.getEmail();
        Room room = roomService.findById(booking.getRoom().getId());

        Optional<Booking> completeBooking =  bookingService.createBooking(booking);
        completeBooking.get().setUser(user);
        completeBooking.get().setRoom(room);

        log.info(completeBooking.get().toString());

        emailController.sendSimpleEmail(
                EmailContext.builder()
                        .to(email)
                        .subject(Messages.BOOKING_INFO.getMessage())
                        .body(completeBooking.get().toString())
                        .build()
        );
        return ResponseEntity.ok(completeBooking.get().toString());
    }

    @GetMapping("/get-bookings-for-room/{roomId}")
    public List<Booking> getBookingsForRoom(@PathVariable Integer roomId) {
        log.info("Fetching bookings for room with id: {}", roomId);
        return bookingService.getBookingsForRoom(roomId);
    }

    @DeleteMapping("/delete-booking/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBooking(@PathVariable Integer id) {
        log.info("Deleting booking with id: {}", id);
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/find-available-rooms")
    public ResponseEntity<List<Room>> findAvailableRooms(
            @RequestBody Room requestedRoom,
            @RequestParam("startDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("endDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) throws SQLException {

        log.info("Finding available rooms for criteria: {}", requestedRoom);
        List<Room> availableRooms = bookingService.findAvailableRooms(requestedRoom, startDate, endDate);

        if (availableRooms.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(availableRooms);
        }
    }

    @Scheduled(fixedRate = 3000000)
    public void checkBookings() {
        List<Booking> bookingsUpdate = bookingService.getAllBookings();

        Map<Integer, Booking> previousBookingsMap = bookings.stream()
                .collect(Collectors.toMap(Booking::getId, booking -> booking));

        for (Booking currentBooking : bookingsUpdate) {
            Booking previousBooking = previousBookingsMap.get(currentBooking.getId());

            if (previousBooking != null) {
                if (!currentBooking.getStartDate().equals(previousBooking.getStartDate()) ||
                        !currentBooking.getEndDate().equals(previousBooking.getEndDate())) {

                    log.info("send update email about update booking id=" + currentBooking.getId());

                    currentBooking.setUser(userService.findById(currentBooking.getUser().getId()).get());
                    currentBooking.setRoom(roomService.findById(currentBooking.getRoom().getId()));

                    emailController.sendSimpleEmail(
                            EmailContext.builder()
                                    .to(currentBooking.getUser().getEmail())
                                    .subject(Messages.BOOKING_UPDATE.getMessage())
                                    .body(currentBooking.toString())
                                    .build()
                    );

                }
            }
        }

        bookings = bookingsUpdate;
    }


    @Scheduled(fixedRate = 3600000)
    public void checkBestRoomBooking() {

        // Получаем все текущие бронирования
        List<Booking> bookingsUpdate = bookingService.getAllBookings();

        // Создаем карту текущих бронирований по идентификатору комнаты для быстрого доступа
        Map<Integer, List<Booking>> currentBookingsByRoomId = bookingsUpdate.stream()
                .collect(Collectors.groupingBy(booking -> booking.getRoom().getId()));

        // Итерируемся по всем обновленным бронированиям
        for (Booking currentBooking : bookingsUpdate) {

            // Получаем текущую комнату, на которую оформлено бронирование
            Room currentRoom = currentBooking.getRoom();

            // Проверяем, что есть бронирование для текущей комнаты
            if (!currentBookingsByRoomId.containsKey(currentRoom.getId())) {
                continue; // Если нет бронирования для текущей комнаты, переходим к следующему бронированию
            }

            // Проверяем альтернативные комнаты, которые могут подойти в качестве замены
            List<Room> alternativeRooms = roomService.findAllRoomsExcept(currentRoom.getId());

            List<Room> suitableAlternativeRooms = new ArrayList<>();

            for (Room alternativeRoom : alternativeRooms) {
                // Проверяем, что альтернативная комната соответствует всем критериям
                if (alternativeRoom.getHasConditioner() == currentRoom.getHasConditioner() &&
                        alternativeRoom.getHasProjector() == currentRoom.getHasProjector() &&
                        alternativeRoom.getHasFridge() == currentRoom.getHasFridge() &&
                        alternativeRoom.getHasBalcony() == currentRoom.getHasBalcony() &&
                        alternativeRoom.getCapacity() >= currentBooking.getCurrentCapacity() &&
                        alternativeRoom.getCapacity() <= currentRoom.getCapacity()){

                    // Проверяем доступность альтернативной комнаты на заданный период времени
                    boolean isAvailable = bookingService.isRoomAvailableForBooking(
                            alternativeRoom.getId(),
                            currentBooking.getStartDate(),
                            currentBooking.getEndDate()
                    );

                    if (isAvailable) {
                        suitableAlternativeRooms.add(alternativeRoom);
                    }
                }
            }

            // Если есть подходящие альтернативные комнаты, обновляем бронирование на первую найденную
            if (!suitableAlternativeRooms.isEmpty()) {
                Room alternativeRoom = suitableAlternativeRooms.get(0); // Просто берем первую подходящую комнату

                // Отправляем уведомление о обновлении бронирования
                User user = userService.findById(currentBooking.getUser().getId()).orElse(null);

                // Можно здесь выполнить обновление бронирования
                bookingService.updateBooking(currentBooking, alternativeRoom);

                if (user != null) {
                    emailController.sendSimpleEmail(
                            EmailContext.builder()
                                    .to(user.getEmail())
                                    .subject(Messages.BOOKING_UPDATE.getMessage())
                                    .body("Your booking has been updated to room: " + alternativeRoom.getName() +
                                            "\n Now your booking is: \n" + currentBooking)
                                    .build()
                    );
                }
            }
        }
    }

}