package com.graduation.GMS.Services;

import com.graduation.GMS.DTO.Request.AboutUsRequest;
import com.graduation.GMS.DTO.Request.FAQRequest;
import com.graduation.GMS.DTO.Response.FAQResponse;
import com.graduation.GMS.DTO.Response.ProfileResponse;
import com.graduation.GMS.DTO.Response.UserResponse;
import com.graduation.GMS.Models.AboutUs;
import com.graduation.GMS.Models.FAQ;
import com.graduation.GMS.Repositories.AboutUsRepository;
import com.graduation.GMS.Repositories.FAQRepository;
import com.graduation.GMS.Repositories.UserRepository;
import com.graduation.GMS.Handlers.HandleCurrentUserSession;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.graduation.GMS.DTO.Response.UserResponse.mapToUserResponse;

@Service
@AllArgsConstructor
public class FAQService {

    private final FAQRepository faqRepository;
    private final AboutUsRepository aboutUsRepository;


    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin')")
    public ResponseEntity<?> createFAQ(FAQRequest request) {

        FAQ faq = new FAQ();
        faq.setQuestion(request.getQuestion());
        faq.setAnswer(request.getAnswer());
        faq.setAdmin(HandleCurrentUserSession.getCurrentUser());
        faqRepository.save(faq);
        // Return the response with the saved faq details
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "FAQ created successfully"));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin')")
    public ResponseEntity<?> updateFAQ(Integer id, FAQRequest request) {
        var existingFAQ = faqRepository.findById(id).orElse(null);
        if (existingFAQ == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "FAQ not found"));
        }
        if (!existingFAQ.getQuestion().equals(request.getQuestion()) && !request.getQuestion().isEmpty()) {
            existingFAQ.setQuestion(request.getQuestion());
        }
        if (!existingFAQ.getAnswer().equals(request.getAnswer()) && !request.getAnswer().isEmpty()) {
            existingFAQ.setAnswer(request.getAnswer());
        }

        faqRepository.save(existingFAQ);

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "FAQ updated successfully"));
    }

    @PreAuthorize("hasAnyAuthority('Admin')")
    public ResponseEntity<?> deleteFAQ(Integer id) {

        if (!faqRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "FAQ not found"));
        }

        faqRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "FAQ deleted successfully"));

    }

    public ResponseEntity<?> getFAQById(Integer id) {
        var faqEntity = faqRepository.findById(id).orElse(null);
        if (faqEntity == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "FAQ Id not found"));
        }

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


    @PreAuthorize("hasAnyAuthority('Admin','Secretary')")
    public ResponseEntity<?> updateAboutUs(AboutUsRequest request) {
        AboutUs about = aboutUsRepository.findById(1).orElse(new AboutUs());
        boolean isUpdated = false;

        if (request.getGymName() != null && !request.getGymName().equals(about.getGymName())) {
            about.setGymName(request.getGymName());
            isUpdated = true;
        }
        if (request.getGymDescription() != null && !request.getGymDescription().equals(about.getGymDescription())) {
            about.setGymDescription(request.getGymDescription());
            isUpdated = true;
        }
        if (request.getOurMission() != null && !request.getOurMission().equals(about.getOurMission())) {
            about.setOurMission(request.getOurMission());
            isUpdated = true;
        }
        if (request.getOurVision() != null && !request.getOurVision().equals(about.getOurVision())) {
            about.setOurVision(request.getOurVision());
            isUpdated = true;
        }
        if (request.getFacebookLink() != null && !request.getFacebookLink().equals(about.getFacebookLink())) {
            about.setFacebookLink(request.getFacebookLink());
            isUpdated = true;
        }
        if (request.getInstagramLink() != null && !request.getInstagramLink().equals(about.getInstagramLink())) {
            about.setInstagramLink(request.getInstagramLink());
            isUpdated = true;
        }
        if (request.getTwitterLink() != null && !request.getTwitterLink().equals(about.getTwitterLink())) {
            about.setTwitterLink(request.getTwitterLink());
            isUpdated = true;
        }
        if (isUpdated) {
            about.setUpdatedAt(LocalDateTime.now());
            aboutUsRepository.save(about);
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "data updated"));
    }

    public ResponseEntity<?> getAboutUs() {
        var about = aboutUsRepository.findById(1).orElse(null);
        if (about == null) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(Map.of("message", "no content"));
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", about));
    }


}
