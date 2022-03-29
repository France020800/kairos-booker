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

    @Column(name = "fiscal_code")
    private String fiscalCode;

    @Column(name = "adding_matricola", columnDefinition = "boolean default false")
    private boolean addingMatricola;

    @Column(name = "adding_password", columnDefinition = "boolean default false")
    private boolean addingPassword;

    @Column(name = "adding_auto_booking", columnDefinition = "boolean default false")
    private boolean addingAutoBooking;

    @Column(name = "removing_auto_booking", columnDefinition = "boolean default false")
    private boolean removingAutoBooking;

    @Column(name = "auto_booking", columnDefinition = "boolean default false")
    private boolean autoBooking;

    @Column(name = "writing_report", columnDefinition = "boolean default false")
    private boolean writingReport = false;
}
