package com.secureguard.dto;

import com.secureguard.model.Inquiry;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class InquiryDTO {

    private Long id;
    
    @NotBlank(message = "Subject is required")
    @Size(max = 200, message = "Subject must not exceed 200 characters")
    private String subject;
    
    @NotBlank(message = "Message is required")
    @Size(max = 2000, message = "Message must not exceed 2000 characters")
    private String message;
    
    @NotNull(message = "Category is required")
    private Inquiry.InquiryCategory category;
    
    private Inquiry.InquiryPriority priority = Inquiry.InquiryPriority.MEDIUM;
    
    private String contactEmail;
    private String contactPhone;
    
    // Read-only fields for response
    private String ticketNumber;
    private Inquiry.InquiryStatus status;
    private String userName;
    private String userEmail;
    private String supportResponse;
    private String supportStaffName;
    private LocalDateTime respondedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public InquiryDTO() {}

    public InquiryDTO(String subject, String message, Inquiry.InquiryCategory category) {
        this.subject = subject;
        this.message = message;
        this.category = category;
    }

    public InquiryDTO(String subject, String message, Inquiry.InquiryCategory category, 
                     Inquiry.InquiryPriority priority, String contactEmail, String contactPhone) {
        this.subject = subject;
        this.message = message;
        this.category = category;
        this.priority = priority;
        this.contactEmail = contactEmail;
        this.contactPhone = contactPhone;
    }

    // Factory method to create DTO from entity
    public static InquiryDTO fromEntity(Inquiry inquiry) {
        InquiryDTO dto = new InquiryDTO();
        dto.setId(inquiry.getId());
        dto.setSubject(inquiry.getSubject());
        dto.setMessage(inquiry.getMessage());
        dto.setCategory(inquiry.getCategory());
        dto.setPriority(inquiry.getPriority());
        dto.setContactEmail(inquiry.getContactEmail());
        dto.setContactPhone(inquiry.getContactPhone());
        dto.setTicketNumber(inquiry.getTicketNumber());
        dto.setStatus(inquiry.getStatus());
        dto.setSupportResponse(inquiry.getSupportResponse());
        dto.setSupportStaffName(inquiry.getSupportStaffName());
        dto.setRespondedAt(inquiry.getRespondedAt());
        dto.setCreatedAt(inquiry.getCreatedAt());
        dto.setUpdatedAt(inquiry.getUpdatedAt());
        
        // Set user information if available
        if (inquiry.getUser() != null) {
            dto.setUserName(inquiry.getUser().getFullName());
            dto.setUserEmail(inquiry.getUser().getEmail());
        }
        
        return dto;
    }

    // Helper methods
    public boolean isOpen() {
        return status == Inquiry.InquiryStatus.OPEN || status == Inquiry.InquiryStatus.IN_PROGRESS;
    }

    public boolean isResolved() {
        return status == Inquiry.InquiryStatus.RESOLVED || status == Inquiry.InquiryStatus.CLOSED;
    }

    public boolean hasResponse() {
        return supportResponse != null && !supportResponse.trim().isEmpty();
    }

    public String getPriorityBadgeClass() {
        if (priority == null) return "badge-secondary";
        switch (priority) {
            case URGENT: return "badge-danger";
            case HIGH: return "badge-warning";
            case MEDIUM: return "badge-info";
            case LOW: return "badge-secondary";
            default: return "badge-secondary";
        }
    }

    public String getStatusBadgeClass() {
        if (status == null) return "badge-secondary";
        switch (status) {
            case OPEN: return "badge-primary";
            case IN_PROGRESS: return "badge-warning";
            case WAITING_FOR_CUSTOMER: return "badge-info";
            case RESOLVED: return "badge-success";
            case CLOSED: return "badge-secondary";
            default: return "badge-secondary";
        }
    }

    public String getCategoryDisplayName() {
        return category != null ? category.getDisplayName() : "";
    }

    public String getPriorityDisplayName() {
        return priority != null ? priority.getDisplayName() : "";
    }

    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : "";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Inquiry.InquiryCategory getCategory() {
        return category;
    }

    public void setCategory(Inquiry.InquiryCategory category) {
        this.category = category;
    }

    public Inquiry.InquiryPriority getPriority() {
        return priority;
    }

    public void setPriority(Inquiry.InquiryPriority priority) {
        this.priority = priority;
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

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public Inquiry.InquiryStatus getStatus() {
        return status;
    }

    public void setStatus(Inquiry.InquiryStatus status) {
        this.status = status;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
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
}