-- Seed Data for BookNowGo

-- Roles
INSERT INTO roles (id, name, description) VALUES
(1, 'ROLE_CUSTOMER', 'Standard customer role for booking hotels'),
(2, 'ROLE_HOTEL_OWNER', 'Hotel owner/manager role for managing properties & inventory'),
(3, 'ROLE_ADMIN', 'Platform administrator role with full oversight')
ON CONFLICT (id) DO NOTHING;

-- Locations
INSERT INTO locations (id, country, state, city, area, landmark, latitude, longitude, city_image, is_popular) VALUES
(1, 'India', 'Goa', 'Goa', 'Calangute', 'Near Calangute Beach', 15.5439, 73.7554, 'https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?w=800&auto=format&fit=crop&q=80', true),
(2, 'India', 'Maharashtra', 'Mumbai', 'Colaba', 'Near Gateway of India', 18.9220, 72.8347, 'https://images.unsplash.com/photo-1570168007204-dfb528c6958f?w=800&auto=format&fit=crop&q=80', true),
(3, 'India', 'Delhi', 'New Delhi', 'Connaught Place', 'Near Central Park', 28.6315, 77.2167, 'https://images.unsplash.com/photo-1587474260584-136574528ed5?w=800&auto=format&fit=crop&q=80', true),
(4, 'India', 'Karnataka', 'Bengaluru', 'Indiranagar', '100ft Road', 12.9784, 77.6408, 'https://images.unsplash.com/photo-1596176530529-78163a4f7af2?w=800&auto=format&fit=crop&q=80', true),
(5, 'France', 'Île-de-France', 'Paris', '7th Arrondissement', 'Near Eiffel Tower', 48.8584, 2.2945, 'https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=800&auto=format&fit=crop&q=80', true),
(6, 'United Arab Emirates', 'Dubai', 'Dubai', 'Downtown Dubai', 'Near Burj Khalifa', 25.1972, 55.2744, 'https://images.unsplash.com/photo-1512453979798-5ea266f8880c?w=800&auto=format&fit=crop&q=80', true)
ON CONFLICT (id) DO NOTHING;

-- Coupons
INSERT INTO coupons (id, code, description, discount_type, discount_value, min_booking_amount, max_discount_amount, valid_from, valid_to, usage_limit, times_used, is_active) VALUES
(1, 'SUMMER50', '50% off up to $100 for summer vacation escapes', 'PERCENTAGE', 50.00, 100.00, 100.00, CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP + INTERVAL '90 days', 500, 12, true),
(2, 'WELCOME20', 'Flat $20 instant welcome voucher for new members', 'FLAT', 20.00, 50.00, 20.00, CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP + INTERVAL '180 days', 1000, 45, true),
(3, 'LUXURYSTAY', '15% off luxury and boutique suites with no upper cap', 'PERCENTAGE', 15.00, 250.00, 500.00, CURRENT_TIMESTAMP - INTERVAL '5 days', CURRENT_TIMESTAMP + INTERVAL '60 days', 200, 8, true)
ON CONFLICT (id) DO NOTHING;
