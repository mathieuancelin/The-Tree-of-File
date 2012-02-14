package com.mancel01.thetreeof.api;

import com.mancel01.thetreeof.model.Leaf;
import com.mancel01.thetreeof.model.Node;

public interface PersistenceProvider {
    
    public void createBlobStore();
    
    public void persistAsBlob(String uuid, byte[] bytes);
    
    public byte[] getBlob(String uuid);
    
    public void persistLeaf(Leaf leaf);
    
    public void persistNode(Node node);
    
}
