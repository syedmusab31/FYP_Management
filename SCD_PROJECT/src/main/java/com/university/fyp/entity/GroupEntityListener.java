package com.university.fyp.entity;

import jakarta.persistence.*;

public class GroupEntityListener {

    @PrePersist
    @PreUpdate
    public void updateMemberIdsJson(Group group) {
        if (group.getMembers() != null && !group.getMembers().isEmpty()) {
            group.updateMemberIdsJson();
        } else if (group.getMemberIdsJson() == null || group.getMemberIdsJson().isEmpty()) {
            group.setMemberIdsJson("[]");
        }
    }

    @PostLoad
    public void ensureMemberIdsJson(Group group) {
        // Ensure memberIdsJson is set after loading
        if (group.getMemberIdsJson() == null || group.getMemberIdsJson().isEmpty()) {
            if (group.getMembers() != null && !group.getMembers().isEmpty()) {
                group.updateMemberIdsJson();
            } else {
                group.setMemberIdsJson("[]");
            }
        }
    }
}

