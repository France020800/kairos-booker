package com.guglielmo.kairosbookerspring;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "lesson")
@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@NoArgsConstructor
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "isBooked")
    private boolean isBooked;

    @Column(name = "courseName")
    private String courseName;

    @Column(name = "time")
    private String time;

    @Column(name = "date")
    private String date;

    @Column(name = "chat_id")
    private Long chatId;
}
