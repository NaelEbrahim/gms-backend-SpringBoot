package com.graduation.GMS.Controllers;

import com.graduation.GMS.DTO.Request.FAQRequest;
import com.graduation.GMS.Services.FAQService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/FAQ")
public class FAQController {

    @Autowired
    private FAQService faqService;

    // Endpoint to create a new faq
    @PostMapping("/create")
    public ResponseEntity<?> createFAQ(@Valid @RequestBody FAQRequest faqRequest) {
        return faqService.createFAQ(faqRequest);
    }

    // Endpoint to update an existing faq
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateFAQ(@PathVariable Integer id, @Valid @RequestBody FAQRequest faqRequest) {
        return faqService.updateFAQ(id, faqRequest);
    }

    // Endpoint to delete a faq
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteFAQ(@PathVariable Integer id) {
        return faqService.deleteFAQ(id);
    }

    // Endpoint to get details of a specific faq by ID
    @GetMapping("/show/{id}")
    public ResponseEntity<?> getFAQById(@PathVariable Integer id) {
        return faqService.getFAQById(id);
    }

    // Endpoint to get all faqs
    @GetMapping("/show/all")
    public ResponseEntity<?> getAllFAQes() {
        return faqService.getAllFAQes();
    }

}
