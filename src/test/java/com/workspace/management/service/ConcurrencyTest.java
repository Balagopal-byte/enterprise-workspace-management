package com.workspace.management.service;

import com.workspace.management.config.DatabaseConfig;
import com.workspace.management.dao.BookingDAO;
import com.workspace.management.dao.impl.BookingDAOImpl;
import com.workspace.management.exception.BookingConflictException;
import com.workspace.management.model.Booking;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration test demonstrating the effectiveness of the concurrency engine.
 *
 * This test simulates 10 concurrent threads attempting to book the EXACT SAME desk
 * for the EXACT SAME time slot.
 *
 * Without pessimistic locking (SELECT ... FOR UPDATE) and transactions,
 * multiple threads would read that the desk is available and book it,
 * resulting in double-booking.
 *
 * With our concurrency protection in BookingService, exactly ONE booking should
 * succeed, and the other 9 should throw BookingConflictException.
 */
public class ConcurrencyTest {

    private static DatabaseConfig dbConfig;
    private static final int CONCURRENT_THREADS = 10;
    private static final int TEST_DESK_ID = 1;
    private static final LocalDate TEST_DATE = LocalDate.now().plusDays(10); // Future date

    @BeforeAll
    static void setup() throws Exception {
        dbConfig = DatabaseConfig.getInstance();
        // Clear any existing bookings for this specific test desk and date
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM bookings WHERE desk_id = " + TEST_DESK_ID +
                    " AND booking_date = '" + java.sql.Date.valueOf(TEST_DATE) + "'");
        }
    }

    @AfterAll
    static void teardown() throws Exception {
        // Clean up test data
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM bookings WHERE desk_id = " + TEST_DESK_ID +
                    " AND booking_date = '" + java.sql.Date.valueOf(TEST_DATE) + "'");
        }
    }

    @Test
    void testConcurrentBookingToSameDesk() throws InterruptedException {
        BookingService bookingService = new BookingService();
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(CONCURRENT_THREADS);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        List<Exception> otherExceptions = new CopyOnWriteArrayList<>();

        // Create 10 concurrent booking tasks
        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            final int userId = (i % 3) + 1; // Round-robin users 1, 2, 3

            executor.submit(() -> {
                try {
                    // Wait for the starting gun so all threads hit the DB simultaneously
                    latch.await();

                    // Attempt to book 09:00 to 17:00
                    bookingService.createBooking(
                            userId,
                            TEST_DESK_ID,
                            TEST_DATE,
                            LocalTime.of(9, 0),
                            LocalTime.of(17, 0),
                            "Concurrency Test Booking"
                    );

                    successCount.incrementAndGet();

                } catch (BookingConflictException e) {
                    // Expected for all but one thread
                    conflictCount.incrementAndGet();
                } catch (Exception e) {
                    otherExceptions.add(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Fire the starting gun!
        latch.countDown();

        // Wait for all threads to finish (timeout after 15 seconds)
        boolean completed = doneLatch.await(15, TimeUnit.SECONDS);

        // Verify results
        assertEquals(true, completed, "All threads should complete within the timeout");
        assertEquals(0, otherExceptions.size(), "There should be no generic exceptions");

        // The core assertion: Exactly ONE booking should succeed, 9 should conflict
        assertEquals(1, successCount.get(), "Exactly one concurrent booking should succeed");
        assertEquals(CONCURRENT_THREADS - 1, conflictCount.get(),
                "All other concurrent attempts should fail with a BookingConflictException");

        // Verify database state directly
        BookingDAO bookingDAO = new BookingDAOImpl();
        List<Booking> testBookings = bookingDAO.findByDeskId(TEST_DESK_ID);

        long countForTestDate = testBookings.stream()
                .filter(b -> b.getBookingDate().equals(TEST_DATE))
                .count();

        assertEquals(1, countForTestDate, "Database should contain exactly one booking for the test time slot");

        executor.shutdown();
    }
}
