package com.booknowgo.service;

import com.booknowgo.dto.*;
import com.booknowgo.entity.*;
import com.booknowgo.exception.BadRequestException;
import com.booknowgo.exception.ResourceNotFoundException;
import com.booknowgo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingGuestRepository bookingGuestRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;
    private final PricingEngine pricingEngine;
    private final PaymentService paymentService;
    private final RefundRepository refundRepository;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Transactional
    public BookingResponse createBooking(BookingRequest request, Long customerId) {
        if (!request.getCheckOutDate().isAfter(request.getCheckInDate())) {
            throw new BadRequestException("Check-out date must be strictly after check-in date");
        }

        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + request.getHotelId()));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + request.getRoomId()));

        Coupon coupon = null;
        if (request.getCouponCode() != null && !request.getCouponCode().trim().isEmpty()) {
            coupon = couponRepository.findByCodeIgnoreCase(request.getCouponCode().trim()).orElse(null);
        }

        FareBreakdownDto fare = pricingEngine.calculateFare(
                room,
                request.getCheckInDate(),
                request.getCheckOutDate(),
                request.getNumberOfRooms(),
                coupon
        );

        String bookingReference = "BNG-" + LocalDate.now().getYear() + "-" + (100000 + RANDOM.nextInt(900000));

        Booking booking = Booking.builder()
                .bookingReference(bookingReference)
                .customer(customer)
                .hotel(hotel)
                .room(room)
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .totalNights(fare.getNumberOfNights())
                .numberOfRooms(request.getNumberOfRooms())
                .adults(request.getAdults())
                .children(request.getChildren())
                .basePrice(fare.getTotalRoomBasePrice())
                .taxAmount(fare.getTaxAmount())
                .serviceFee(fare.getServiceFee())
                .discountAmount(fare.getDiscountAmount())
                .coupon(coupon)
                .couponDiscount(fare.getCouponDiscount())
                .totalAmount(fare.getFinalTotalAmount())
                .bookingStatus("CONFIRMED")
                .specialRequests(request.getSpecialRequests())
                .build();

        booking = bookingRepository.save(booking);

        BookingGuest guest = BookingGuest.builder()
                .booking(booking)
                .fullName(request.getGuestName())
                .email(request.getGuestEmail())
                .phoneNumber(request.getGuestPhone())
                .isPrimary(true)
                .build();
        bookingGuestRepository.save(guest);

        // Process payment
        Payment payment = paymentService.processPayment(booking, request.getPaymentMethod(), fare.getFinalTotalAmount());
        booking.setPayment(payment);

        // Increment coupon usage
        if (coupon != null && fare.getCouponDiscount().compareTo(BigDecimal.ZERO) > 0) {
            coupon.setTimesUsed(coupon.getTimesUsed() + 1);
            couponRepository.save(coupon);
        }

        return mapToBookingResponse(booking, fare);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getCustomerBookings(Long customerId) {
        return bookingRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BookingResponse getBookingByReference(String reference) {
        Booking booking = bookingRepository.findByBookingReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with reference: " + reference));
        return mapToBookingResponse(booking);
    }

    @Transactional
    public RefundResponse cancelBooking(String reference, Long userId, String reason) {
        Booking booking = bookingRepository.findByBookingReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with reference: " + reference));

        if (!booking.getCustomer().getId().equals(userId)) {
            // Check if admin or user
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            boolean isAdmin = user.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equals(r.getName()));
            if (!isAdmin) {
                throw new BadRequestException("You do not have permission to cancel this booking");
            }
        }

        if ("CANCELLED".equalsIgnoreCase(booking.getBookingStatus())) {
            throw new BadRequestException("Booking is already cancelled");
        }

        booking.setBookingStatus("CANCELLED");
        bookingRepository.save(booking);

        // Calculate refund: full refund if > 24 hours before checkin, else 70% refund
        LocalDate today = LocalDate.now();
        long daysUntilCheckIn = ChronoUnit.DAYS.between(today, booking.getCheckInDate());
        BigDecimal refundAmount;
        BigDecimal cancellationFee = BigDecimal.ZERO;

        if (daysUntilCheckIn >= 1) {
            refundAmount = booking.getTotalAmount();
        } else {
            cancellationFee = booking.getTotalAmount().multiply(BigDecimal.valueOf(0.30)).setScale(2, RoundingMode.HALF_UP);
            refundAmount = booking.getTotalAmount().subtract(cancellationFee);
        }

        String refundRef = "RFD-" + LocalDate.now().getYear() + "-" + (100000 + RANDOM.nextInt(900000));
        Refund refund = Refund.builder()
                .booking(booking)
                .payment(booking.getPayment())
                .refundReference(refundRef)
                .refundAmount(refundAmount)
                .refundStatus("COMPLETED")
                .reason(reason)
                .processedAt(LocalDateTime.now())
                .build();
        refund = refundRepository.save(refund);

        if (booking.getPayment() != null) {
            booking.getPayment().setPaymentStatus("REFUNDED");
        }

        return RefundResponse.builder()
                .refundReference(refundRef)
                .bookingReference(booking.getBookingReference())
                .originalAmount(booking.getTotalAmount())
                .refundAmount(refundAmount)
                .cancellationFee(cancellationFee)
                .refundStatus("COMPLETED")
                .reason(reason)
                .processedAt(refund.getProcessedAt())
                .build();
    }

    public BookingResponse mapToBookingResponse(Booking booking) {
        FareBreakdownDto fare = pricingEngine.calculateFare(
                booking.getRoom(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getNumberOfRooms(),
                booking.getCoupon()
        );
        return mapToBookingResponse(booking, fare);
    }

    public BookingResponse mapToBookingResponse(Booking booking, FareBreakdownDto fare) {
        Hotel hotel = booking.getHotel();
        Room room = booking.getRoom();
        List<BookingGuest> guests = booking.getGuests();
        BookingGuest primaryGuest = (guests != null && !guests.isEmpty()) ? guests.get(0) : null;

        String pStatus = booking.getPayment() != null ? booking.getPayment().getPaymentStatus() : "SUCCESS";
        String pMethod = booking.getPayment() != null ? booking.getPayment().getPaymentMethod() : "CREDIT_CARD";
        String txnRef = booking.getPayment() != null ? booking.getPayment().getTransactionReference() : "";

        return BookingResponse.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .hotelId(hotel.getId())
                .hotelName(hotel.getName())
                .hotelAddress(hotel.getAddress())
                .hotelCity(hotel.getLocation() != null && hotel.getLocation().getCity() != null ? hotel.getLocation().getCity() : "Unknown")
                .hotelImage(hotel.getPrimaryImageUrl())
                .checkInTime(hotel.getCheckInTime())
                .checkOutTime(hotel.getCheckOutTime())
                .roomId(room.getId())
                .roomName(room.getName())
                .roomType(room.getRoomType())
                .bedType(room.getBedType())
                .mealPlan(room.getMealPlan())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .totalNights(booking.getTotalNights())
                .numberOfRooms(booking.getNumberOfRooms())
                .adults(booking.getAdults())
                .children(booking.getChildren())
                .guestName(primaryGuest != null ? primaryGuest.getFullName() : "")
                .guestEmail(primaryGuest != null ? primaryGuest.getEmail() : "")
                .guestPhone(primaryGuest != null ? primaryGuest.getPhoneNumber() : "")
                .specialRequests(booking.getSpecialRequests())
                .fareBreakdown(fare)
                .totalAmount(booking.getTotalAmount())
                .bookingStatus(booking.getBookingStatus())
                .paymentStatus(pStatus)
                .paymentMethod(pMethod)
                .transactionReference(txnRef)
                .cancellationPolicy(room.getCancellationPolicy())
                .createdAt(booking.getCreatedAt())
                .currency(fare != null && fare.getCurrency() != null ? fare.getCurrency() : "INR")
                .build();
    }
}
