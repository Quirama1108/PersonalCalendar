package com.calendar.application.exception;

public class EventOverlapException extends RuntimeException {

    public EventOverlapException() {
        super("Event overlaps with an existing event");
    }
}