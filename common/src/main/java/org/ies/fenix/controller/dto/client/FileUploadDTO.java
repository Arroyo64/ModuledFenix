package org.ies.fenix.controller.dto.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadDTO {
    private String fileName;
    private String fileType;
    private byte[] bytes;
}
