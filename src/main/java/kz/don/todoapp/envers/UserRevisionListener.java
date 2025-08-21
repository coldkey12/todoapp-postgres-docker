package kz.don.todoapp.envers;

import org.hibernate.envers.RevisionListener;

public class UserRevisionListener implements RevisionListener {

    // TODO resolve username from security context for admin
    @Override
    public void newRevision(Object revisionEntity) {
        AuditEnversInfo auditEnversInfo = (AuditEnversInfo) revisionEntity;
        auditEnversInfo.setUsername(auditEnversInfo.getUsername());
    }
}
