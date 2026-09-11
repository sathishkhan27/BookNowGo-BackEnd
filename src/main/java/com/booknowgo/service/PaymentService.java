package com.booknowgo.service;

import com.booknowgo.entity.Booking;
import com.booknowgo.entity.Payment;
import com.booknowgo.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Transactional
    public Payment processPayment(Booking booking, String paymentMethod, BigDecimal amount) {
        String method = paymentMethod != null ? paymentMethod.toUpperCase() : "CREDIT_CARD";
        String txnRef = "TXN-" + System.currentTimeMillis() + "-" + (1000 + RANDOM.nextInt(9000));

        Payment payment = Payment.builder()
                .booking(booking)
                .transactionReference(txnRef)
                .paymentMethod(method)
                .paymentStatus("SUCCESS")
                .amount(amount)
                .currency("INR")
                .gatewayResponse("{\"status\":\"AUTHORIZED\",\"code\":\"00\",\"authCode\":\"BNG" + RANDOM.nextInt(999999) + "\"}")
                .paidAt(LocalDateTime.now())
                .build();

        return paymentRepository.save(payment);
    }
}
