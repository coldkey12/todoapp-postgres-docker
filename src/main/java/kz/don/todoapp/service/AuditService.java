package kz.don.todoapp.service;

import kz.don.todoapp.audit.AuditLog;
import kz.don.todoapp.audit.AuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditRepository auditRepository;

    public List<AuditLog> getAuditLogs() {
        log.info("Retrieving audit logs");

        return auditRepository.findAll();
    }

    public void saveAuditLog(AuditLog auditLog) {
        log.info("Saving audit log: {}", auditLog);
        auditRepository.save(auditLog);
    }

}
