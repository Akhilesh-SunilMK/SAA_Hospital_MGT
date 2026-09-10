package com.hms.appointment.factory;

import com.hms.common.exception.BusinessRuleException;

public class UnsupportedAppointmentTypeException extends BusinessRuleException {
    public UnsupportedAppointmentTypeException(String message) {
        super(message);
    }
}
