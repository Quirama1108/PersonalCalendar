package com.calendar.application.exception;

public class OutsideSchedulingHoursException extends RuntimeException {

    public OutsideSchedulingHoursException() {
        super("Event is outside allowed scheduling hours");
    }
}