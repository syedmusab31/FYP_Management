package com.university.fyp.controller;

import com.university.fyp.dto.DeadlineDTO;
import com.university.fyp.service.DeadlineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deadlines")
@RequiredArgsConstructor
public class DeadlineController {

    private final DeadlineService deadlineService;

    @GetMapping
    public ResponseEntity<List<DeadlineDTO>> getAllDeadlines() {
        return ResponseEntity.ok(deadlineService.getAllDeadlines());
    }

    @PostMapping
    public ResponseEntity<DeadlineDTO> createDeadline(@RequestBody DeadlineDTO dto) {
        return ResponseEntity.ok(deadlineService.createDeadline(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeadline(@PathVariable Long id) {
        deadlineService.deleteDeadline(id);
        return ResponseEntity.ok().build();
    }
}
