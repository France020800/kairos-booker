package com.guglielmo.kairosbookerspring.db.user;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "kairos_user")
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class KairosUser {
    @Id
    @SequenceGenerator(name = "user_seq",
            sequenceName = "user_seq",
            allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "user_seq")
    private Integer id;

    @Column(name = "matricola")
    private String matricola;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "chat_id", unique = true)
    private Long chadId;

    @Column(name = "adding_matricola", nullable = false)
    private boolean addingMatricola;

    @Column(name = "adding_password", nullable = false)
    private boolean addingPassword;

    @Column(name = "adding_auto_booking", nullable = false)
    private boolean addingAutoBooking;

    @Column(name = "removing_auto_booking", nullable = false)
    private boolean removingAutoBooking;

    @Column(name = "lessons")
    private String lessons;

    @Column(name = "auto_booking", nullable = false)
    private boolean autoBooking;

    @Column(name = "writing_report", nullable = false)
    private boolean writingReport;
}
