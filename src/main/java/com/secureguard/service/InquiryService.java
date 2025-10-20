package com.secureguard.service;

import com.secureguard.dto.InquiryDTO;
import com.secureguard.dto.InquiryResponseDTO;
import com.secureguard.model.Inquiry;
import com.secureguard.model.User;
import com.secureguard.repository.InquiryRepository;
import com.secureguard.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InquiryService {

    @Autowired
    private InquiryRepository inquiryRepository;

    @Autowired
    private UserRepository userRepository;

    public Inquiry createInquiry(InquiryDTO inquiryDTO, User user) {
        Inquiry inquiry = new Inquiry(
            user,
            inquiryDTO.getSubject(),
            inquiryDTO.getMessage(),
            inquiryDTO.getCategory(),
            inquiryDTO.getPriority() != null ? inquiryDTO.getPriority() : Inquiry.InquiryPriority.MEDIUM
        );
        
        // Set contact information if provided
        if (inquiryDTO.getContactEmail() != null) {
            inquiry.setContactEmail(inquiryDTO.getContactEmail());
        }
        if (inquiryDTO.getContactPhone() != null) {
            inquiry.setContactPhone(inquiryDTO.getContactPhone());
        }
        
        return inquiryRepository.save(inquiry);
    }

    public List<InquiryDTO> getInquiriesByUser(User user) {
        List<Inquiry> inquiries = inquiryRepository.findByUserOrderByCreatedAtDesc(user);
        return inquiries.stream()
                .map(InquiryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<InquiryDTO> getInquiryById(Long id) {
        Optional<Inquiry> inquiry = inquiryRepository.findById(id);
        return inquiry.map(InquiryDTO::fromEntity);
    }
    
    public Inquiry findById(Long id) {
        return inquiryRepository.findById(id).orElse(null);
    }
    
    public Page<Inquiry> findByUser(User user, Pageable pageable) {
        return inquiryRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }
    
    public Page<Inquiry> findByUserAndStatus(User user, Inquiry.InquiryStatus status, Pageable pageable) {
        return inquiryRepository.findByUserAndStatusOrderByCreatedAtDesc(user, status, pageable);
    }
    
    public Page<Inquiry> findInquiriesWithFilters(String status, String category, String priority, String search, Pageable pageable) {
        // This is a simplified implementation - in a real app you'd use Specifications or Criteria API
        if (status != null && !status.isEmpty()) {
            Inquiry.InquiryStatus inquiryStatus = Inquiry.InquiryStatus.valueOf(status);
            return inquiryRepository.findByStatusOrderByCreatedAtDesc(inquiryStatus, pageable);
        } else if (category != null && !category.isEmpty()) {
            Inquiry.InquiryCategory inquiryCategory = Inquiry.InquiryCategory.valueOf(category);
            return inquiryRepository.findByCategoryOrderByCreatedAtDesc(inquiryCategory, pageable);
        } else if (priority != null && !priority.isEmpty()) {
            Inquiry.InquiryPriority inquiryPriority = Inquiry.InquiryPriority.valueOf(priority);
            return inquiryRepository.findByPriorityOrderByCreatedAtDesc(inquiryPriority, pageable);
        } else if (search != null && !search.isEmpty()) {
            return inquiryRepository.searchInquiries(search, pageable);
        } else {
            return inquiryRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
    }

    public List<InquiryDTO> getAllInquiries() {
        List<Inquiry> inquiries = inquiryRepository.findAll();
        return inquiries.stream()
                .map(InquiryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<InquiryDTO> getInquiriesByStatus(Inquiry.InquiryStatus status) {
        List<Inquiry> inquiries = inquiryRepository.findByStatusOrderByCreatedAtDesc(status);
        return inquiries.stream()
                .map(InquiryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<InquiryDTO> getInquiriesByCategory(Inquiry.InquiryCategory category) {
        List<Inquiry> inquiries = inquiryRepository.findByCategoryOrderByCreatedAtDesc(category);
        return inquiries.stream()
                .map(InquiryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<InquiryDTO> getInquiriesByPriority(Inquiry.InquiryPriority priority) {
        List<Inquiry> inquiries = inquiryRepository.findByPriorityOrderByCreatedAtDesc(priority);
        return inquiries.stream()
                .map(InquiryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<InquiryDTO> getOpenInquiriesList() {
        List<Inquiry> inquiries = inquiryRepository.findOpenInquiries();
        return inquiries.stream()
                .map(InquiryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<InquiryDTO> getUrgentInquiriesList() {
        List<Inquiry> inquiries = inquiryRepository.findUrgentInquiries();
        return inquiries.stream()
                .map(InquiryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<InquiryDTO> searchInquiries(String searchTerm) {
        List<Inquiry> inquiries = inquiryRepository.searchInquiries(searchTerm);
        return inquiries.stream()
                .map(InquiryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public Inquiry respondToInquiry(Long inquiryId, String response, String staffName) {
        Optional<Inquiry> inquiryOpt = inquiryRepository.findById(inquiryId);
        if (inquiryOpt.isPresent()) {
            Inquiry inquiry = inquiryOpt.get();
            inquiry.respond(response, staffName);
            return inquiryRepository.save(inquiry);
        }
        throw new RuntimeException("Inquiry not found");
    }
    
    public Inquiry updateStatus(Long inquiryId, Inquiry.InquiryStatus status) {
        Optional<Inquiry> inquiryOpt = inquiryRepository.findById(inquiryId);
        if (inquiryOpt.isPresent()) {
            Inquiry inquiry = inquiryOpt.get();
            inquiry.setStatus(status);
            inquiry.setUpdatedAt(LocalDateTime.now());
            return inquiryRepository.save(inquiry);
        }
        throw new RuntimeException("Inquiry not found");
    }
    
    public Inquiry updatePriority(Long inquiryId, Inquiry.InquiryPriority priority) {
        Optional<Inquiry> inquiryOpt = inquiryRepository.findById(inquiryId);
        if (inquiryOpt.isPresent()) {
            Inquiry inquiry = inquiryOpt.get();
            inquiry.setPriority(priority);
            inquiry.setUpdatedAt(LocalDateTime.now());
            return inquiryRepository.save(inquiry);
        }
        throw new RuntimeException("Inquiry not found");
    }

    public void deleteInquiry(Long inquiryId) {
        inquiryRepository.deleteById(inquiryId);
    }

    public boolean canUserEditInquiry(Inquiry inquiry) {
        // User can edit inquiry only if it has no response yet
        return !inquiry.hasResponse();
    }

    public Inquiry updateInquiry(Long inquiryId, InquiryDTO inquiryDTO, User user) {
        Optional<Inquiry> inquiryOpt = inquiryRepository.findById(inquiryId);
        if (!inquiryOpt.isPresent()) {
            throw new RuntimeException("Inquiry not found");
        }

        Inquiry inquiry = inquiryOpt.get();
        
        // Check if user owns this inquiry
        if (!inquiry.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        // Check if inquiry can be edited (no response yet)
        if (!canUserEditInquiry(inquiry)) {
            throw new RuntimeException("Cannot edit inquiry after receiving a response");
        }

        // Update inquiry fields
        inquiry.setSubject(inquiryDTO.getSubject());
        inquiry.setMessage(inquiryDTO.getMessage());
        inquiry.setCategory(inquiryDTO.getCategory());
        inquiry.setPriority(inquiryDTO.getPriority());
        inquiry.setContactEmail(inquiryDTO.getContactEmail());
        inquiry.setContactPhone(inquiryDTO.getContactPhone());
        inquiry.setUpdatedAt(LocalDateTime.now());

        return inquiryRepository.save(inquiry);
    }

    public void deleteUserInquiry(Long inquiryId, User user) {
        Optional<Inquiry> inquiryOpt = inquiryRepository.findById(inquiryId);
        if (!inquiryOpt.isPresent()) {
            throw new RuntimeException("Inquiry not found");
        }

        Inquiry inquiry = inquiryOpt.get();
        
        // Check if user owns this inquiry
        if (!inquiry.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        // Check if inquiry can be deleted (no response yet)
        if (!canUserEditInquiry(inquiry)) {
            throw new RuntimeException("Cannot delete inquiry after receiving a response");
        }

        inquiryRepository.delete(inquiry);
    }

    public void adminDeleteInquiry(Long inquiryId) {
        // Admin can delete any inquiry regardless of its status
        inquiryRepository.deleteById(inquiryId);
    }

    public Map<String, Long> getInquiryStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", inquiryRepository.countTotalInquiries());
        stats.put("open", inquiryRepository.countOpenInquiries());
        stats.put("resolved", inquiryRepository.countResolvedInquiries());
        stats.put("urgent", (long) inquiryRepository.findUrgentInquiries().size());
        return stats;
    }

    // Individual statistics methods for AdminController
    public long getTotalInquiries() {
        return inquiryRepository.countTotalInquiries();
    }

    public long getOpenInquiries() {
        return inquiryRepository.countOpenInquiries();
    }

    public long getResolvedInquiries() {
        return inquiryRepository.countResolvedInquiries();
    }

    public long getUrgentInquiries() {
        return (long) inquiryRepository.findUrgentInquiries().size();
    }
}