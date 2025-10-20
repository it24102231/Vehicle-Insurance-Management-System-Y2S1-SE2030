package com.secureguard.repository;

import com.secureguard.model.Inquiry;
import com.secureguard.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    // Find inquiries by user
    List<Inquiry> findByUserOrderByCreatedAtDesc(User user);
    Page<Inquiry> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    // Find inquiries by user and status
    Page<Inquiry> findByUserAndStatusOrderByCreatedAtDesc(User user, Inquiry.InquiryStatus status, Pageable pageable);

    // Find inquiries by status
    List<Inquiry> findByStatusOrderByCreatedAtDesc(Inquiry.InquiryStatus status);
    Page<Inquiry> findByStatusOrderByCreatedAtDesc(Inquiry.InquiryStatus status, Pageable pageable);

    // Find inquiries by category
    List<Inquiry> findByCategoryOrderByCreatedAtDesc(Inquiry.InquiryCategory category);
    Page<Inquiry> findByCategoryOrderByCreatedAtDesc(Inquiry.InquiryCategory category, Pageable pageable);

    // Find inquiries by priority
    List<Inquiry> findByPriorityOrderByCreatedAtDesc(Inquiry.InquiryPriority priority);
    Page<Inquiry> findByPriorityOrderByCreatedAtDesc(Inquiry.InquiryPriority priority, Pageable pageable);

    // Find all inquiries with pagination
    Page<Inquiry> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // Find open inquiries
    @Query("SELECT i FROM Inquiry i WHERE i.status IN ('OPEN', 'IN_PROGRESS') ORDER BY i.createdAt DESC")
    List<Inquiry> findOpenInquiries();

    // Find urgent inquiries
    @Query("SELECT i FROM Inquiry i WHERE i.priority = 'URGENT' ORDER BY i.createdAt DESC")
    List<Inquiry> findUrgentInquiries();

    // Find unresolved inquiries
    @Query("SELECT i FROM Inquiry i WHERE i.status IN ('OPEN', 'IN_PROGRESS', 'WAITING_FOR_CUSTOMER') ORDER BY i.createdAt DESC")
    List<Inquiry> findUnresolvedInquiries();

    // Search inquiries by subject or message
    @Query("SELECT i FROM Inquiry i WHERE LOWER(i.subject) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(i.message) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY i.createdAt DESC")
    List<Inquiry> searchInquiries(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT i FROM Inquiry i WHERE LOWER(i.subject) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(i.message) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY i.createdAt DESC")
    Page<Inquiry> searchInquiries(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Find inquiry by ticket number
    Optional<Inquiry> findByTicketNumber(String ticketNumber);

    // Count inquiries by status
    long countByStatus(Inquiry.InquiryStatus status);

    // Count inquiries by user
    long countByUser(User user);

    // Count total inquiries
    @Query("SELECT COUNT(i) FROM Inquiry i")
    long countTotalInquiries();

    // Count open inquiries
    @Query("SELECT COUNT(i) FROM Inquiry i WHERE i.status IN ('OPEN', 'IN_PROGRESS')")
    long countOpenInquiries();

    // Count resolved inquiries
    @Query("SELECT COUNT(i) FROM Inquiry i WHERE i.status IN ('RESOLVED', 'CLOSED')")
    long countResolvedInquiries();

    // Find recent inquiries (for admin dashboard)
    @Query("SELECT i FROM Inquiry i ORDER BY i.createdAt DESC")
    List<Inquiry> findRecentInquiries();
}