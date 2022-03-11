package com.guglielmo.kairosbookerspring.db.lessonToBook;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "lesson_to_book")
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class LessonToBook {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "chat_id")
    private Long chatId;

    @Column(name = "course_name")
    private String courseName;
}
