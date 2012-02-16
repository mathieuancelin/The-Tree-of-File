package com.mancel01.thetreeof.blob;

import com.mancel01.thetreeof.api.Blob;

public enum EmptyBlob implements Blob {
    
    INSTANCE {
        @Override
        public byte[] bytes() {
            return new byte[0];
        }
    }
}
