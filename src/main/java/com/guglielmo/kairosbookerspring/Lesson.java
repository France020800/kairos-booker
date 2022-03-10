package com.guglielmo.kairosbookerspring;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Lesson {
    private boolean isBooked;
    private String courseName;
    private String time;
    private String date;
}
