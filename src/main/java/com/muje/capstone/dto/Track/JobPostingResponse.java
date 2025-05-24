package com.muje.capstone.dto.Track;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPostingResponse {
    private String company;
    private String title;
    private String location;
    private String salary;
    private List<String> skills;
    private String detailUrl;
}
