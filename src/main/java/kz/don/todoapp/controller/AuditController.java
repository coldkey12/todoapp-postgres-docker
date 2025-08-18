package kz.don.todoapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.don.todoapp.audit.AuditLog;
import kz.don.todoapp.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin Controller", description = "Endpoints for admin operations")
@Slf4j
public class AuditController {

    private final AuditService auditService;

    @Operation(summary = "Get all user logs", description = "Returns a list of all logs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of logs retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        List<AuditLog> auditLogs = auditService.getAuditLogs();
        return ResponseEntity.ok(auditLogs);
    }
}
