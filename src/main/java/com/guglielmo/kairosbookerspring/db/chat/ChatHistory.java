package com.guglielmo.kairosbookerspring.db.chat;

import com.github.kshashov.telegram.api.bind.annotation.BotController;
import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity()
@Table(name = "chat_history")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String message;

    @Column(name = "chad_id")
    private Long chadId;

    @Column
    private Timestamp timestamp;

}
