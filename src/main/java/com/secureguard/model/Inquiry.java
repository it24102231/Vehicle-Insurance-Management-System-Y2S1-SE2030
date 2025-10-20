package com.secureguard.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inquiries")
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;

    @Column(name = "subject", nullable = false)
    @NotBlank(message = "Subject is required")
    @Size(max = 200, message = "Subject must not exceed 200 characters")
    private String subject;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "Message is required")
    @Size(max = 2000, message = "Message must not exceed 2000 characters")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private InquiryCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private InquiryPriority priority = InquiryPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InquiryStatus status = InquiryStatus.OPEN;

    @Column(name = "ticket_number", unique = true, nullable = false)
    private String ticketNumber;

    @Column(name = "support_response", columnDefinition = "TEXT")
    private String supportResponse;

    @Column(name = "support_staff_name")
    private String supportStaffName;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_phone")
    private String contactPhone;

    // Enums for Vehicle Insurance Company
    public enum InquiryCategory {
        GENERAL_INQUIRY("General Inquiry"),
        POLICY_INFORMATION("Policy Information"),
        CLAIM_ASSISTANCE("Claim Assistance"),
        PREMIUM_PAYMENT("Premium Payment"),
        POLICY_RENEWAL("Policy Renewal"),
        COVERAGE_QUESTIONS("Coverage Questions"),
        COMPLAINT("Complaint"),
        TECHNICAL_SUPPORT("Technical Support"),
        ACCOUNT_HELP("Account Help"),
        OTHER("Other");

        private final String displayName;

        InquiryCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum InquiryPriority {
        LOW("Low"),
        MEDIUM("Medium"),
        HIGH("High"),
        URGENT("Urgent");

        private final String displayName;

        InquiryPriority(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum InquiryStatus {
        OPEN("Open"),
        IN_PROGRESS("In Progress"),
        WAITING_FOR_CUSTOMER("Waiting for Customer"),
        RESOLVED("Resolved"),
        CLOSED("Closed");

        private final String displayName;

        InquiryStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Constructors
    public Inquiry() {}

    public Inquiry(User user, String subject, String message, InquiryCategory category, 
                   InquiryPriority priority) {
        this.user = user;
        this.subject = subject;
        this.message = message;
        this.category = category;
        this.priority = priority;
        this.status = InquiryStatus.OPEN;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.contactEmail = user.getEmail();
        this.contactPhone = user.getPhoneNumber();
        this.ticketNumber = generateTicketNumber();
    }

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (ticketNumber == null) {
            ticketNumber = generateTicketNumber();
        }
        if (status == null) {
            status = InquiryStatus.OPEN;
        }
        if (priority == null) {
            priority = InquiryPriority.MEDIUM;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
    private String generateTicketNumber() {
        return "INS-" + System.currentTimeMillis();
    }

    public boolean isOpen() {
        return status == InquiryStatus.OPEN || status == InquiryStatus.IN_PROGRESS;
    }

    public boolean isResolved() {
        return status == InquiryStatus.RESOLVED || status == InquiryStatus.CLOSED;
    }

    public boolean hasResponse() {
        return supportResponse != null && !supportResponse.trim().isEmpty();
    }

    public String getPriorityBadgeClass() {
        switch (priority) {
            case URGENT: return "badge-danger";
            case HIGH: return "badge-warning";
            case MEDIUM: return "badge-info";
            case LOW: return "badge-secondary";
            default: return "badge-secondary";
        }
    }

    public String getStatusBadgeClass() {
        switch (status) {
            case OPEN: return "badge-primary";
            case IN_PROGRESS: return "badge-warning";
            case WAITING_FOR_CUSTOMER: return "badge-info";
            case RESOLVED: return "badge-success";
            case CLOSED: return "badge-secondary";
            default: return "badge-secondary";
        }
    }

    public void respond(String response, String staffName) {
        this.supportResponse = response;
        this.supportStaffName = staffName;
        this.respondedAt = LocalDateTime.now();
        this.status = InquiryStatus.RESOLVED;
        this.updatedAt = LocalDateTime.now();
    }

    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : "Unknown";
    }

    public String getCategoryDisplayName() {
        return category != null ? category.getDisplayName() : "Unknown";
    }

    public String getPriorityDisplayName() {
        return priority != null ? priority.getDisplayName() : "Unknown";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.contactEmail = user.getEmail();
            this.contactPhone = user.getPhoneNumber();
        }
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public InquiryCategory getCategory() {
        return category;
    }

    public void setCategory(InquiryCategory category) {
        this.category = category;
    }

    public InquiryPriority getPriority() {
        return priority;
    }

    public void setPriority(InquiryPriority priority) {
        this.priority = priority;
    }

    public InquiryStatus getStatus() {
        return status;
    }

    public void setStatus(InquiryStatus status) {
        this.status = status;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getSupportResponse() {
        return supportResponse;
    }

    public void setSupportResponse(String supportResponse) {
        this.supportResponse = supportResponse;
    }

    public String getSupportStaffName() {
        return supportStaffName;
    }

    public void setSupportStaffName(String supportStaffName) {
        this.supportStaffName = supportStaffName;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }
}