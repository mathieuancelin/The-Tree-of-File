package com.mancel01.thetreeof.api;

import com.mancel01.thetreeof.model.Leaf;
import com.mancel01.thetreeof.model.Node;

public interface PersistenceProvider {
    
    public void createBlobStore();
    
    public void persistAsBlob(String uuid, Blob blob);
    
    public Blob getBlob(String uuid);
    
    public void persistLeaf(Leaf leaf);
    
    public void persistNode(Node node);
    
    public void destroyTree();
    
    public void destroyNode(Node node);
    
    public void destroyLeaf(Leaf leaf);
}
