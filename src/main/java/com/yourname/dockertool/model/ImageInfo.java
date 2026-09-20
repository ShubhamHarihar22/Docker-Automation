package com.yourname.dockertool.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageInfo {
    private String id;
    private String tag;
    private long createdAt;
    private long sizeBytes;
}