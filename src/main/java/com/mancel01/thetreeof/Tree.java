package com.mancel01.thetreeof;

import com.mancel01.thetreeof.api.Persistable;
import com.mancel01.thetreeof.model.Leaf;
import com.mancel01.thetreeof.model.Node;
import com.mancel01.thetreeof.util.Configuration;
import java.io.File;
import java.util.Collection;

public class Tree implements Persistable {
    
    public static final String PATH_SEPARATOR = "/";
    
    private final Node root;
    private final File rootFile;
    private final Configuration config;
    
    private Tree() {
        config = new Configuration("config.properties");
        rootFile = new File(config.get("root").getOrElse("./repo"));
        root = new Node("Root", rootFile);
    }
    
    @Override
    public void persist() {
        root.persist();
    }
    
    public File rootFile() {
        return rootFile;
    }

    public Node root() {
        return root;
    }
    
    public Collection<Node> allNodes() {
        return root.allNodesBelow();
    }
    
    public Collection<Leaf> allLeafs() {
        return root.allLeafsBelow();
    }

    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    
    private static final Holder<Tree> HOLDER = TreeHolder.INSTANCE;
    
    public static synchronized Tree instance() {
        return HOLDER.get();
    }
    
    private static interface Holder<T> {
        T get();
    }
    
    private static enum TreeHolder implements Holder<Tree> {

        INSTANCE {

            private final Tree tree = new Tree();
            
            @Override
            public Tree get() {
                return tree;
            }
        }
    }
    
    // TODO : Security visitor
    // TODO : Auto add node, leafs
    // TODO : Async task for persist
    // TODO : Blobs in separate folder
    // TODO : Service registry
    // TODO : Separate model from persitence
    // TODO : Persistence provider
    // TODO : File peristence provider
    
}
