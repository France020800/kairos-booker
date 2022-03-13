package com.guglielmo.kairosbookerspring.db.lessonToBook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonToBookRepository extends JpaRepository<LessonToBook, Integer> {
    Optional<LessonToBook> findByCourseName(String courseName);
    List<LessonToBook> findByChatId(Long chatId);
}
