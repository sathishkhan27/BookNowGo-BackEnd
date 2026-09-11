package com.booknowgo.util;

import com.booknowgo.entity.*;
import com.booknowgo.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final CouponRepository couponRepository;
    private final PaymentRepository paymentRepository;
    private final RoomRepository roomRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initializing system roles, accounts, and base locations...");

        // 1. Roles
        Role customerRole = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_CUSTOMER").description("Customer role").build()));
        Role ownerRole = roleRepository.findByName("ROLE_HOTEL_OWNER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_HOTEL_OWNER").description("Hotel owner role").build()));
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").description("Platform administrator role").build()));

        // 2. Default System Users
        userRepository.findByEmail("customer@booknowgo.com").orElseGet(() ->
                userRepository.save(User.builder()
                        .email("customer@booknowgo.com")
                        .passwordHash(passwordEncoder.encode("Customer@123"))
                        .firstName("Alex")
                        .lastName("Rivera")
                        .phoneNumber("+1 (555) 234-5678")
                        .avatarUrl("https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80")
                        .isActive(true)
                        .isVerified(true)
                        .roles(Set.of(customerRole))
                        .build())
        );

        userRepository.findByEmail("owner@booknowgo.com").orElseGet(() ->
                userRepository.save(User.builder()
                        .email("owner@booknowgo.com")
                        .passwordHash(passwordEncoder.encode("Owner@123"))
                        .firstName("Vikram")
                        .lastName("Singhania")
                        .phoneNumber("+91 98765 43210")
                        .avatarUrl("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop&q=80")
                        .isActive(true)
                        .isVerified(true)
                        .roles(Set.of(ownerRole))
                        .build())
        );

        userRepository.findByEmail("admin@booknowgo.com").orElseGet(() ->
                userRepository.save(User.builder()
                        .email("admin@booknowgo.com")
                        .passwordHash(passwordEncoder.encode("Admin@123"))
                        .firstName("Eleanor")
                        .lastName("Vance")
                        .phoneNumber("+1 (555) 999-0000")
                        .avatarUrl("https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=150&auto=format&fit=crop&q=80")
                        .isActive(true)
                        .isVerified(true)
                        .roles(Set.of(adminRole, customerRole, ownerRole))
                        .build())
        );

        // 3. Base Geographic Locations (only if table empty)
        if (locationRepository.count() == 0) {
            locationRepository.save(Location.builder()
                    .country("India").state("Goa").city("Goa").area("Calangute").landmark("Calangute Beach Coast")
                    .latitude(BigDecimal.valueOf(15.5439)).longitude(BigDecimal.valueOf(73.7554))
                    .cityImage("https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?w=800&auto=format&fit=crop&q=80")
                    .isPopular(true).build());

            locationRepository.save(Location.builder()
                    .country("India").state("Maharashtra").city("Mumbai").area("Colaba").landmark("Gateway of India")
                    .latitude(BigDecimal.valueOf(18.9220)).longitude(BigDecimal.valueOf(72.8347))
                    .cityImage("https://images.unsplash.com/photo-1570168007204-dfb528c6958f?w=800&auto=format&fit=crop&q=80")
                    .isPopular(true).build());

            locationRepository.save(Location.builder()
                    .country("India").state("Delhi").city("New Delhi").area("Connaught Place").landmark("Central Park & Janpath")
                    .latitude(BigDecimal.valueOf(28.6315)).longitude(BigDecimal.valueOf(77.2167))
                    .cityImage("https://images.unsplash.com/photo-1587474260584-136574528ed5?w=800&auto=format&fit=crop&q=80")
                    .isPopular(true).build());

            locationRepository.save(Location.builder()
                    .country("India").state("Karnataka").city("Bengaluru").area("Indiranagar").landmark("100ft Road Shopping District")
                    .latitude(BigDecimal.valueOf(12.9784)).longitude(BigDecimal.valueOf(77.6408))
                    .cityImage("https://images.unsplash.com/photo-1596176530529-78163a4f7af2?w=800&auto=format&fit=crop&q=80")
                    .isPopular(true).build());

            locationRepository.save(Location.builder()
                    .country("France").state("Île-de-France").city("Paris").area("7th Arrondissement").landmark("Champ de Mars & Eiffel Tower")
                    .latitude(BigDecimal.valueOf(48.8584)).longitude(BigDecimal.valueOf(2.2945))
                    .cityImage("https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=800&auto=format&fit=crop&q=80")
                    .isPopular(true).build());

            locationRepository.save(Location.builder()
                    .country("United Arab Emirates").state("Dubai").city("Dubai").area("Downtown Dubai").landmark("Burj Khalifa & Dubai Mall")
                    .latitude(BigDecimal.valueOf(25.1972)).longitude(BigDecimal.valueOf(55.2744))
                    .cityImage("https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&auto=format&fit=crop&q=80")
                    .isPopular(true).build());
        }

        // 4. Default INR Coupons (only if table empty)
        if (couponRepository.count() == 0) {
            couponRepository.save(Coupon.builder()
                    .code("SUMMER50")
                    .description("50% discount up to ₹1,500 for summer escapes")
                    .discountType("PERCENTAGE")
                    .discountValue(BigDecimal.valueOf(50.0))
                    .minBookingAmount(BigDecimal.valueOf(3000.0))
                    .maxDiscountAmount(BigDecimal.valueOf(1500.0))
                    .validFrom(LocalDateTime.now().minusDays(30))
                    .validTo(LocalDateTime.now().plusDays(90))
                    .usageLimit(1000)
                    .timesUsed(0)
                    .isActive(true)
                    .build());

            couponRepository.save(Coupon.builder()
                    .code("WELCOME20")
                    .description("Flat ₹500 instant welcome discount on any stay")
                    .discountType("FLAT")
                    .discountValue(BigDecimal.valueOf(500.0))
                    .minBookingAmount(BigDecimal.valueOf(2000.0))
                    .maxDiscountAmount(BigDecimal.valueOf(500.0))
                    .validFrom(LocalDateTime.now().minusDays(10))
                    .validTo(LocalDateTime.now().plusDays(180))
                    .usageLimit(2000)
                    .timesUsed(0)
                    .isActive(true)
                    .build());

            couponRepository.save(Coupon.builder()
                    .code("LUXURYSTAY")
                    .description("15% off premier luxury boutique stays up to ₹5,000")
                    .discountType("PERCENTAGE")
                    .discountValue(BigDecimal.valueOf(15.0))
                    .minBookingAmount(BigDecimal.valueOf(10000.0))
                    .maxDiscountAmount(BigDecimal.valueOf(5000.0))
                    .validFrom(LocalDateTime.now().minusDays(5))
                    .validTo(LocalDateTime.now().plusDays(60))
                    .usageLimit(500)
                    .timesUsed(0)
                    .isActive(true)
                    .build());
        }

        migrateLegacyPricingToInr();
        log.info("System initialization complete. No hardcoded hotels or bookings provisioned.");
    }

    private void migrateLegacyPricingToInr() {
        try {
            boolean updated = false;

            List<Room> allRooms = roomRepository.findAll();
            for (Room r : allRooms) {
                if (r.getBasePrice() != null && r.getBasePrice().compareTo(BigDecimal.valueOf(1000)) < 0) {
                    BigDecimal inr = r.getBasePrice().multiply(BigDecimal.valueOf(80)).setScale(-2, RoundingMode.HALF_UP);
                    if (inr.compareTo(BigDecimal.valueOf(2500)) < 0) {
                        inr = BigDecimal.valueOf(3500.00);
                    }
                    r.setBasePrice(inr);
                    roomRepository.save(r);
                    updated = true;
                }
            }

            List<Payment> allPayments = paymentRepository.findAll();
            for (Payment p : allPayments) {
                if ("USD".equalsIgnoreCase(p.getCurrency())) {
                    p.setCurrency("INR");
                    if (p.getAmount() != null && p.getAmount().compareTo(BigDecimal.valueOf(1000)) < 0) {
                        p.setAmount(p.getAmount().multiply(BigDecimal.valueOf(80)).setScale(2, RoundingMode.HALF_UP));
                    }
                    paymentRepository.save(p);
                    updated = true;
                }
            }

            if (updated) {
                log.info("Migrated legacy records to INR currency & pricing successfully.");
            }
        } catch (Exception e) {
            log.warn("Migration check encountered a non-fatal error: {}", e.getMessage());
        }
    }
}
