package com.guglielmo.kairosbookerspring.db.user;

import com.guglielmo.kairosbookerspring.Lesson;
import lombok.*;
import org.openqa.selenium.WebElement;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "user")
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "matricola")
    private String matricola;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "chat_id", unique = true)
    private Long chadId;

    @Column(name = "adding_matricola")
    private boolean adding_matricola;

    @Column(name = "adding_password")
    private boolean adding_password;

    @Column(name = "lessons")
    private String lessons;

    @Column(name = "auto_booking")
    private boolean auto_booking;
}
