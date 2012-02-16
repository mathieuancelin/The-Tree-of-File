package com.mancel01.thetreeof.blob;

import com.mancel01.thetreeof.api.Blob;
import java.io.UnsupportedEncodingException;

public class StringBlob implements Blob {
    
    private final String value;

    public StringBlob(String value) {
        assert value != null;
        this.value = value;
    }

    @Override
    public byte[] bytes() {
        try {
            return value.getBytes("utf-8");
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
            return new byte[0];
        }
    }
}
