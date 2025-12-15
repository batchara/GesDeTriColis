package com.raoudate.GestionDeTri.repository;

import com.raoudate.GestionDeTri.enums.NotificationStatus;
import com.raoudate.GestionDeTri.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {


    List<Notification> findByTargetUserIdOrderByDateCreationDesc(String targetUserId);

    List<Notification> findByTargetUserIdAndStatusOrderByDateCreationDesc(String targetUserId, NotificationStatus status);

    long countByTargetUserIdAndStatus(String targetUserId, NotificationStatus status);

    List<Notification> findByTargetUserIdAndActionRequiredTrueAndStatusOrderByDateCreationDesc(
            String targetUserId, NotificationStatus status);
}
