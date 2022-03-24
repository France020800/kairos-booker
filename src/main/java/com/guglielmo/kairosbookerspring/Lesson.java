package com.guglielmo.kairosbookerspring;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Lesson {
    private boolean isBooked;
    private String courseName;
    private String startTime;
    private String endTime;
    private String date;
    private String classroom;
}
