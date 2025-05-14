package com.graduation.GMS.Services;

import com.graduation.GMS.DTO.Request.FAQRequest;
import com.graduation.GMS.DTO.Response.FAQResponse;
import com.graduation.GMS.DTO.Response.UserResponse;
import com.graduation.GMS.Models.FAQ;
import com.graduation.GMS.Models.User;
import com.graduation.GMS.Repositories.FAQRepository;
import com.graduation.GMS.Repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FAQService {
    @Autowired
    private FAQRepository faqRepository;
    @Autowired
    private UserRepository userRepository;

    @Transactional
    public ResponseEntity<?> createFAQ(@Valid FAQRequest request) {

        Optional<User> admin =userRepository.findById(request.getAdminId());
        FAQ faq = new FAQ();
        faq.setQuestion(request.getQuestion());
        faq.setAnswer(request.getAnswer());
        if (admin.isPresent()) {
            faq.setAdmin(admin.get());
        }
        faqRepository.save(faq);
        // Return the response with the saved faq details
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "FAQ created successfully"));
    }
    @Transactional
    public ResponseEntity<?> updateFAQ(Integer id, @Valid FAQRequest request) {

        Optional<FAQ> optionalFAQ = faqRepository.findById(id);
        if (optionalFAQ.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "FAQ not found"));
        }

        FAQ existingFAQ = optionalFAQ.get();

        Optional<User> admin =userRepository.findById(request.getAdminId());
        if (admin.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Admin Not Found"));
        }

        if (!existingFAQ.getAdmin().getId().equals(request.getAdminId())) {
            existingFAQ.setAdmin(admin.get());
        }
        if (!existingFAQ.getQuestion().equals(request.getQuestion())&&!request.getQuestion().isEmpty()) {
            existingFAQ.setQuestion(request.getQuestion());
        }
        if (!existingFAQ.getAnswer().equals(request.getAnswer())&&!request.getAnswer().isEmpty()) {
            existingFAQ.setAnswer(request.getAnswer());
        }

        faqRepository.save(existingFAQ);

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "FAQ updated successfully"));
    }

    public ResponseEntity<?> deleteFAQ(Integer id) {

        if (!faqRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "FAQ not found"));
        }

        faqRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "FAQ deleted successfully"));

    }

    public ResponseEntity<?> getFAQById(Integer id) {
        Optional<FAQ> faqOptional = faqRepository.findById(id);
        if (faqOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "FAQ Not found"));
        }

        FAQ faqEntity = faqOptional.get();
        UserResponse adminResponse = mapToUserResponse(faqEntity.getAdmin());

        FAQResponse responseDto = new FAQResponse(
                faqEntity.getId(),
                adminResponse,
                faqEntity.getQuestion(),
                faqEntity.getAnswer()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(responseDto);
    }

    public ResponseEntity<?> getAllFAQes() {
        List<FAQ> faqs = faqRepository.findAll();

        if (faqs.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No FAQs found"));
        }

        List<FAQResponse> faqResponses = faqs.stream()
                .map(c -> new FAQResponse(
                        c.getId(),
                        mapToUserResponse(c.getAdmin()),
                        c.getQuestion(),
                        c.getAnswer()
                ))
                .toList();

        return ResponseEntity.status(HttpStatus.OK)
                .body(faqResponses);
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getGender(),
                user.getDob(),
                user.getCreatedAt(),
                user.getQr()
        );
    }
}
