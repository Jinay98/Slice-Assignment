package com.slice.service;

import com.slice.dto.request.CreateBookingRequest;
import com.slice.dto.response.BookingResponse;

public interface BookingService {
    BookingResponse createBooking(CreateBookingRequest request);
}
