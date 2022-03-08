package com.guglielmo.kairosbookerspring.db.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Integer> {
}
