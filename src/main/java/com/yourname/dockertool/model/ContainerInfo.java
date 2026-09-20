package com.yourname.dockertool.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContainerInfo {

    private String id;
    private String name;
    private String image;
    private String status;
    private String state;
    private Long created;
    private String[] ports;
}
