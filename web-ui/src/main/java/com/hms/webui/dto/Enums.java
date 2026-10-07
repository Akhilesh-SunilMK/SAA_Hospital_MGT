package com.hms.webui.dto;

/** Mirrors the fixed value sets used across the backend DTOs, grouped for template dropdowns. */
public final class Enums {
    private Enums() {}

    public enum AppointmentType { OPD, FOLLOW_UP, EMERGENCY, TELE }
    public enum Priority { LOW, NORMAL, HIGH, CRITICAL }
    public enum AppointmentStatus { SCHEDULED, CHECKED_IN, IN_PROGRESS, COMPLETED, CANCELLED, NO_SHOW }

    public enum RecordType { CONSULTATION, FOLLOW_UP, EMERGENCY }
    public enum PrescriptionStatus { ISSUED, DISPENSED, CANCELLED }
    public enum ReportFormat { PDF, EXCEL, CSV }

    public enum LabPriority { NORMAL, STAT }
    public enum LabOrderStatus { ORDERED, COLLECTED, PROCESSING, COMPLETED, CANCELLED }

    public enum PatientCategory { GENERAL, INSURED, SENIOR_CITIZEN, STAFF_CONCESSION }

    public enum InvoiceStatus { DRAFT, ISSUED, PARTIALLY_PAID, PAID, CANCELLED, REFUNDED }
    public enum PaymentMethod { CASH, CARD, UPI, NET_BANKING, INSURANCE }

    public enum ChannelType { EMAIL, SMS, PUSH, WHATSAPP }
    public enum NotificationStatus { PENDING, SENT, FAILED, RETRYING, SKIPPED }

    public enum AdmissionStatus { ADMITTED, DISCHARGED }

    public enum DayOfWeekOption { MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY }

    public enum Gender { MALE, FEMALE, OTHER }
}
