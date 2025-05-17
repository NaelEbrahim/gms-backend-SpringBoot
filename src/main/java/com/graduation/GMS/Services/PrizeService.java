package com.graduation.GMS.Services;
import com.graduation.GMS.DTO.Request.PrizeRequest;
import com.graduation.GMS.DTO.Response.PrizeResponse;
import com.graduation.GMS.Models.Event;
import com.graduation.GMS.Models.Prize;
import com.graduation.GMS.Repositories.EventRepository;
import com.graduation.GMS.Repositories.PrizeRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.graduation.GMS.DTO.Response.UserResponse.mapToUserResponse;

@Service
@AllArgsConstructor
public class PrizeService {

    private EventRepository eventRepository;
    private PrizeRepository prizeRepository;

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Secretary')")
    public ResponseEntity<?> createPrize(PrizeRequest request) {
        // Check if event title already exists
        Optional<Event> existingEvent = eventRepository.findById(request.getEvent_id());
        if (existingEvent.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Event Not Found"));
        }

        Prize prize = new Prize();
        prize.setEvent(existingEvent.get());
        prize.setDescription(request.getDescription());
        prize.setPrecondition(request.getPrecondition());
        prizeRepository.save(prize);
        // Return the response with the saved prize details
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Prize created successfully"));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Secretary')")
    public ResponseEntity<?> updatePrize(Integer id, PrizeRequest request) {
        Optional<Prize> optionalPrize = prizeRepository.findById(id);
        if (optionalPrize.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Prize not found"));
        }

        Prize existingPrize = optionalPrize.get();

        if (!existingPrize.getEvent().getId().equals(request.getEvent_id())&&request.getEvent_id()!=null) {
            Optional<Event> existingEvent = eventRepository.findById(request.getEvent_id());
            if (existingEvent.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Event Not Found"));
            }
            existingPrize.setEvent(existingEvent.get());
        }
        if (!existingPrize.getDescription().equals(request.getDescription())&&!request.getDescription().isEmpty()) {
            existingPrize.setDescription(request.getDescription());
        }
        if (!existingPrize.getPrecondition().equals(request.getPrecondition())&&!request.getPrecondition().isEmpty()) {
            existingPrize.setPrecondition(request.getPrecondition());
        }
        prizeRepository.save(existingPrize);

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Prize updated successfully"));
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('Admin','Secretary')")
    public ResponseEntity<?> deletePrize(Integer id) {
        if (!prizeRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Prize not found"));
        }

        prizeRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Prize deleted successfully"));
    }

    @PreAuthorize("hasAnyAuthority('Admin','Secretary')")
    public ResponseEntity<?> getPrizeById(Integer id) {
        Optional<Prize> prizeOptional = prizeRepository.findById(id);
        if (prizeOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Prize Not found"));
        }

        Prize prizeEntity = prizeOptional.get();

        PrizeResponse responseDto = new PrizeResponse(
                prizeEntity.getId(),
                prizeEntity.getDescription(),
                prizeEntity.getPrecondition()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(responseDto);
    }

    @PreAuthorize("hasAnyAuthority('Admin','Secretary')")
    public ResponseEntity<?> getAllPrizes() {
        List<Prize> prizes = prizeRepository.findAll();

        if (prizes.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No Prizes found"));
        }

        List<PrizeResponse> prizeResponses = prizes.stream()
                .map(prize -> new PrizeResponse(
                        prize.getId(),
                        prize.getDescription(),
                        prize.getPrecondition()
                ))
                .toList();

        return ResponseEntity.status(HttpStatus.OK).body(prizeResponses);
    }
}
