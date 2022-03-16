package com.guglielmo.kairosbookerspring.db.devUser;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DevUserRepository extends JpaRepository<DevUser, Integer> {
    public Optional<DevUser> findByChatId(Long chatId);
    public List<DevUser> findAll();
}
