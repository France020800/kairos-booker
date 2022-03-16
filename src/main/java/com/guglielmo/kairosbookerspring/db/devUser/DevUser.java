package com.guglielmo.kairosbookerspring.db.devUser;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "dev_user")
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class DevUser {

    @Id
    @SequenceGenerator(name="lesson_seq",
            sequenceName="lesson_seq",
            allocationSize=1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator="lesson_seq")
    private Integer id;

    @Column(name = "chat_id", unique = true)
    private Long chatId;

    @Column(name = "username")
    private String username;

    @Column(name = "is_dev_user")
    private Boolean isDevUser;
}
