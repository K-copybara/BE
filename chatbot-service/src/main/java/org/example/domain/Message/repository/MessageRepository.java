package org.example.domain.Message.repository;

import org.example.domain.entity.ChatSession;
import org.example.domain.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByChatSessionOrderBySentAtAsc(ChatSession chatSession);

    Optional<Message> findTopByChatSessionOrderBySentAtDesc(ChatSession session);
}
