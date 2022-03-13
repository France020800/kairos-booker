package com.guglielmo.kairosbookerspring.db.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<KairosUser, Integer> {
    Optional<KairosUser> findByChadId(Long chadId);
    Optional<KairosUser> findByMatricola(String matricola);
}
