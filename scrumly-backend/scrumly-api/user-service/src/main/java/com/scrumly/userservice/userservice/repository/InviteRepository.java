package com.scrumly.userservice.userservice.repository;

import com.scrumly.userservice.userservice.domain.invites.InviteEntity;
import com.scrumly.userservice.userservice.enums.invite.InviteStatus;
import com.scrumly.userservice.userservice.enums.invite.InviteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InviteRepository extends JpaRepository<InviteEntity, Long>, JpaSpecificationExecutor<InviteEntity> {
    InviteEntity findByInviteId(String inviteId);
    InviteEntity findByConnectingIdAndCreatedForAndInviteTypeAndCurrentStatusIn(String connectingId,
                                                                              String createdFor,
                                                                              InviteType inviteType,
                                                                              List<InviteStatus> inviteStatus);
}
