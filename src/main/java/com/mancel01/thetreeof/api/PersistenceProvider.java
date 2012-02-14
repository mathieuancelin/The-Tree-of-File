package com.mancel01.thetreeof.api;

import com.mancel01.thetreeof.model.Leaf;

public interface PersistenceProvider {
    
    public void createBlobStore();
    
    public void persistAsBlob(String uuid, byte[] bytes);
    
    public byte[] getBlob(String uuid);
    
    public void persistLeaf(Leaf leaf);
    
}
