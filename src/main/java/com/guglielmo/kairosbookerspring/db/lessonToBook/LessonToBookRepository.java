package com.guglielmo.kairosbookerspring.db.lessonToBook;

import com.guglielmo.kairosbookerspring.db.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LessonToBookRepository extends JpaRepository<LessonToBook, Integer> {
    Optional<LessonToBook> findByCourseName(String courseName);
}
