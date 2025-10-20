package com.secureguard.controller;

import com.secureguard.dto.InquiryDTO;
import com.secureguard.dto.InquiryResponseDTO;
import com.secureguard.model.Inquiry;
import com.secureguard.model.User;
import com.secureguard.service.InquiryService;
import com.secureguard.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/inquiries")
public class InquiryController {

    @Autowired
    private InquiryService inquiryService;

    @Autowired
    private UserService userService;

    // User inquiry submission (AJAX)
    @PostMapping("/submit")
    @ResponseBody
    public ResponseEntity<?> submitInquiry(@Valid @RequestBody InquiryDTO inquiryDTO,
                                           HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Please login first"));
            }

            User user = userService.findById(userId);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            Inquiry inquiry = inquiryService.createInquiry(inquiryDTO, user);
            InquiryDTO responseDTO = InquiryDTO.fromEntity(inquiry);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Your inquiry has been submitted successfully!",
                "ticketNumber", inquiry.getTicketNumber(),
                "inquiry", responseDTO
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // User's inquiry list page
    @GetMapping("/my-inquiries")
    public String myInquiries(Model model, HttpSession session,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) String status) {
        
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }

        User user = userService.findById(userId);
        if (user == null) {
            return "redirect:/auth/login";
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Inquiry> inquiryPage;
        
        if (status != null && !status.isEmpty()) {
            Inquiry.InquiryStatus inquiryStatus = Inquiry.InquiryStatus.valueOf(status);
            inquiryPage = inquiryService.findByUserAndStatus(user, inquiryStatus, pageable);
        } else {
            inquiryPage = inquiryService.findByUser(user, pageable);
        }

        model.addAttribute("inquiries", inquiryPage);
        model.addAttribute("user", user);
        model.addAttribute("currentStatus", status);
        model.addAttribute("categories", Inquiry.InquiryCategory.values());
        model.addAttribute("statuses", Inquiry.InquiryStatus.values());
        model.addAttribute("priorities", Inquiry.InquiryPriority.values());
        
        return "client/my-inquiries";
    }

    // Get inquiry details (AJAX)
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> getInquiry(@PathVariable Long id, HttpSession session) {
        try {
            // Allow admin users (who may not have userId in session) to fetch any inquiry.
            String userRole = (String) session.getAttribute("userRole");
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null && !"ADMIN".equals(userRole)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Please login first"));
            }

            Inquiry inquiry = inquiryService.findById(id);
            if (inquiry == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Inquiry not found"));
            }

            // Check if user owns this inquiry or is admin
            boolean isAdmin = "ADMIN".equals(userRole);
            boolean isOwner = (userId != null) && inquiry.getUser().getId().equals(userId);

            if (!isAdmin && !isOwner) {
                return ResponseEntity.badRequest().body(Map.of("error", "Access denied"));
            }

            InquiryDTO inquiryDTO = InquiryDTO.fromEntity(inquiry);
            return ResponseEntity.ok(inquiryDTO);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Admin inquiry management page
    @GetMapping("/admin")
    public String adminInquiries(Model model, HttpSession session,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(required = false) String status,
                                 @RequestParam(required = false) String category,
                                 @RequestParam(required = false) String priority,
                                 @RequestParam(required = false) String search) {
        
        String userRole = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(userRole)) {
            return "redirect:/auth/login";
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Inquiry> inquiryPage = inquiryService.findInquiriesWithFilters(
            status, category, priority, search, pageable);

        // Get statistics
        Map<String, Long> stats = inquiryService.getInquiryStatistics();

        model.addAttribute("inquiries", inquiryPage);
        model.addAttribute("stats", stats);
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentCategory", category);
        model.addAttribute("currentPriority", priority);
        model.addAttribute("currentSearch", search);
        model.addAttribute("categories", Inquiry.InquiryCategory.values());
        model.addAttribute("statuses", Inquiry.InquiryStatus.values());
        model.addAttribute("priorities", Inquiry.InquiryPriority.values());
        
        String adminType = (String) session.getAttribute("adminType");
        if ("CUSTOMER_SUPPORT".equals(adminType)) {
            return "admin/customer_support_admin/inquiries";
        }
        return "admin/inquiries";
    }

    // Admin respond to inquiry (AJAX)
    @PostMapping("/admin/{id}/respond")
    @ResponseBody
    public ResponseEntity<?> respondToInquiry(@PathVariable Long id,
                                              @Valid @RequestBody InquiryResponseDTO responseDTO,
                                              HttpSession session) {
        try {
            String userRole = (String) session.getAttribute("userRole");
            if (!"ADMIN".equals(userRole)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Access denied"));
            }

            Inquiry inquiry = inquiryService.findById(id);
            if (inquiry == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Inquiry not found"));
            }

            Inquiry updatedInquiry = inquiryService.respondToInquiry(
                id, responseDTO.getResponse(), responseDTO.getStaffName());
            
            InquiryDTO inquiryDTO = InquiryDTO.fromEntity(updatedInquiry);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Response submitted successfully!",
                "inquiry", inquiryDTO
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Admin update inquiry status (AJAX)
    @PutMapping("/admin/{id}/status")
    @ResponseBody
    public ResponseEntity<?> updateInquiryStatus(@PathVariable Long id,
                                                 @RequestBody Map<String, String> statusUpdate,
                                                 HttpSession session) {
        try {
            String userRole = (String) session.getAttribute("userRole");
            if (!"ADMIN".equals(userRole)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Access denied"));
            }

            String status = statusUpdate.get("status");
            Inquiry.InquiryStatus inquiryStatus = Inquiry.InquiryStatus.valueOf(status);
            
            Inquiry inquiry = inquiryService.updateStatus(id, inquiryStatus);
            InquiryDTO inquiryDTO = InquiryDTO.fromEntity(inquiry);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Status updated successfully!",
                "inquiry", inquiryDTO
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Admin update inquiry priority (AJAX)
    @PutMapping("/admin/{id}/priority")
    @ResponseBody
    public ResponseEntity<?> updateInquiryPriority(@PathVariable Long id,
                                                   @RequestBody Map<String, String> priorityUpdate,
                                                   HttpSession session) {
        try {
            String userRole = (String) session.getAttribute("userRole");
            if (!"ADMIN".equals(userRole)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Access denied"));
            }

            String priority = priorityUpdate.get("priority");
            Inquiry.InquiryPriority inquiryPriority = Inquiry.InquiryPriority.valueOf(priority);
            
            Inquiry inquiry = inquiryService.updatePriority(id, inquiryPriority);
            InquiryDTO inquiryDTO = InquiryDTO.fromEntity(inquiry);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Priority updated successfully!",
                "inquiry", inquiryDTO
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get inquiry statistics for admin dashboard (AJAX)
    @GetMapping("/admin/stats")
    @ResponseBody
    public ResponseEntity<?> getInquiryStats(HttpSession session) {
        try {
            String userRole = (String) session.getAttribute("userRole");
            if (!"ADMIN".equals(userRole)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Access denied"));
            }

            Map<String, Long> stats = inquiryService.getInquiryStatistics();
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // User edit inquiry (AJAX)
    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> editInquiry(@PathVariable Long id,
                                         @Valid @RequestBody InquiryDTO inquiryDTO,
                                         HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Please login first"));
            }

            User user = userService.findById(userId);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            Inquiry updatedInquiry = inquiryService.updateInquiry(id, inquiryDTO, user);
            InquiryDTO responseDTO = InquiryDTO.fromEntity(updatedInquiry);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Your inquiry has been updated successfully!",
                "inquiry", responseDTO
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // User delete inquiry (AJAX)
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteInquiry(@PathVariable Long id, HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Please login first"));
            }

            User user = userService.findById(userId);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
            }

            inquiryService.deleteUserInquiry(id, user);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Your inquiry has been deleted successfully!"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Admin delete inquiry (AJAX)
    @DeleteMapping("/admin/{id}")
    @ResponseBody
    public ResponseEntity<?> adminDeleteInquiry(@PathVariable Long id, HttpSession session) {
        try {
            String userRole = (String) session.getAttribute("userRole");
            if (!"ADMIN".equals(userRole)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Access denied"));
            }

            inquiryService.adminDeleteInquiry(id);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Inquiry has been deleted successfully!"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}