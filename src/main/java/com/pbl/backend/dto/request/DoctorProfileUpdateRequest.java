package com.pbl.backend.dto.request;

import lombok.Data;

@Data
public class DoctorProfileUpdateRequest {

    private String name;
    private String phoneNumber;
    private Integer yoe;
    private String introduction;
    private String degree;
    private String avatarFilepath;
    private String achievements;
}
