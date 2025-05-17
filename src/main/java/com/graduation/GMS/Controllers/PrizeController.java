package com.graduation.GMS.Controllers;

import com.graduation.GMS.DTO.Request.EventRequest;
import com.graduation.GMS.DTO.Request.PrizeRequest;
import com.graduation.GMS.Services.PrizeService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prize")
@AllArgsConstructor
public class PrizeController {
    private PrizeService prizeService;

    @PostMapping("/create")
    public ResponseEntity<?> createPrize(@Valid @RequestBody PrizeRequest request) {
        return prizeService.createPrize(request);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updatePrize(@PathVariable Integer id,
                                         @Valid @RequestBody PrizeRequest request) {
        return prizeService.updatePrize(id, request);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePrize(@PathVariable Integer id) {
        return prizeService.deletePrize(id);
    }

    @GetMapping("/show/{id}")
    public ResponseEntity<?> getPrizeById(@PathVariable Integer id) {
        return prizeService.getPrizeById(id);
    }

    @GetMapping("/show/all")
    public ResponseEntity<?> getAllPrizes() {
        return prizeService.getAllPrizes();
    }

}
