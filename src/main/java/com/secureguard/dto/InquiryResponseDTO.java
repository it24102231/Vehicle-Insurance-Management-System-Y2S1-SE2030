package com.secureguard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class InquiryResponseDTO {

    @NotBlank(message = "Response message is required")
    @Size(max = 2000, message = "Response must not exceed 2000 characters")
    private String response;

    @NotBlank(message = "Staff name is required")
    @Size(max = 100, message = "Staff name must not exceed 100 characters")
    private String staffName;

    // Constructors
    public InquiryResponseDTO() {}

    public InquiryResponseDTO(String response, String staffName) {
        this.response = response;
        this.staffName = staffName;
    }

    // Getters and Setters
    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }
}