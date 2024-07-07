package com.book.booking.services;

import com.book.booking.models.Booking;
import com.book.booking.repositories.BookingRepository;
import com.book.room.models.Room;
import com.book.room.services.CRUDRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor()
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CRUDRoomService crudRoomService;

    public List<Booking> getAllBookings() {
        assert bookingRepository != null;
        return bookingRepository.findAll();
    }

    public Optional<Booking> createBooking(Booking booking) {
        bookingRepository.save(booking);
        return getBookingWithDetails(booking.getId());
    }

    public List<Booking> getBookingsForRoom(Integer roomId) {
        return bookingRepository.findByRoomId(roomId);
    }

    public void deleteBooking(Integer id) {
        bookingRepository.deleteById(id);
    }

    public List<Room> findAvailableRooms(Room requestedRoom, LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        // Получаем все комнаты, соответствующие заданным параметрам, без учета вместимости
        List<Room> rooms = crudRoomService.searchRooms(
                requestedRoom.getName(),
                requestedRoom.getHasConditioner(),
                requestedRoom.getHasProjector(),
                requestedRoom.getHasFridge(),
                requestedRoom.getHasBalcony(),
                null // Capacity will be filtered later
        );

        // Фильтруем комнаты по критериям вместимости и доступности
        return rooms.stream()
                .filter(room -> room.getCapacity() >= requestedRoom.getCapacity())
                .filter(room -> isRoomAvailable(room, startDate, endDate))
                .collect(Collectors.toList());
    }

    private boolean isRoomAvailable(Room room, LocalDateTime startDate, LocalDateTime endDate) {
        // Проверяем наличие бронирований для комнаты в указанный период
        List<Booking> existingBookings = bookingRepository.findByRoomId(room.getId());
        return existingBookings.stream().noneMatch(b ->
                (startDate.isBefore(b.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()) && endDate.isAfter(b.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()))
        );
    }

    public Optional<Booking> getBookingWithDetails(Integer id) {
        return bookingRepository.findById(id);
    }

    public void updateBooking(Booking booking, Room room) {

        // Обновляем информацию о комнате в бронировании
        booking.setRoom(room);

        // Вызываем метод для обновления бронирования в репозитории
        Date startDate = booking.getStartDate();
        Date endDate = booking.getEndDate();
        Integer currentCapacity = booking.getCurrentCapacity();
        bookingRepository.updateBooking(booking.getId(), room, booking.getUser(), startDate, endDate, currentCapacity);

    }

    public boolean isRoomAvailableForBooking(Integer roomId, Date startDate, Date endDate) {

        // Получаем все бронирования для данной комнаты
        List<Booking> existingBookings = bookingRepository.findByRoomId(roomId);

        // Проверяем, есть ли пересечения временных интервалов существующих бронирований с заданным периодом

        return existingBookings.stream().noneMatch(b ->
                (startDate.before(b.getEndDate()) && endDate.after(b.getStartDate()))
        );
    }
}
