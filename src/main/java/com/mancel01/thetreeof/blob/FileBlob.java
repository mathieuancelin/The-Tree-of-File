package com.mancel01.thetreeof.blob;

import com.google.common.io.Files;
import com.mancel01.thetreeof.api.Blob;
import java.io.File;
import java.io.IOException;

public class FileBlob implements Blob {
    
    private final File file;

    public FileBlob(File file) {
        assert file != null;
        this.file = file;
    }
    
    public FileBlob(String file) {
        assert file != null;
        this.file = new File(file);
    }

    @Override
    public byte[] bytes() {
        try {
            return Files.toByteArray(file);
        } catch (IOException ex) {
            ex.printStackTrace();
            return new byte[0];
        }
    }
}
