package com.mancel01.thetreeof;

import com.mancel01.thetreeof.api.Persistable;
import com.mancel01.thetreeof.api.PersistenceProvider;
import com.mancel01.thetreeof.api.file.FilePersistenceProvider;
import com.mancel01.thetreeof.model.Leaf;
import com.mancel01.thetreeof.model.Node;
import com.mancel01.thetreeof.task.TaskExecutor;
import com.mancel01.thetreeof.util.Configuration;
import com.mancel01.thetreeof.util.Registry;
import com.mancel01.thetreeof.util.SimpleLogger;
import java.io.File;
import java.util.Collection;

public class Tree implements Persistable {
    
    public static final String PATH_SEPARATOR = "/";
    
    private final Node root;
    private final File rootFile;
    private final Configuration config;
    private final TaskExecutor exec = new TaskExecutor();
    
    static {
        SimpleLogger.enableColors(true);
        SimpleLogger.enableTrace(true);
    }
    
    private Tree() {
        config = new Configuration("config.properties");
        rootFile = new File(config.get("root").getOrElse("./repo"));
        root = new Node("Root", rootFile);
        Registry.register(PersistenceProvider.class, new FilePersistenceProvider(rootFile));
        TaskExecutor.startTaskExecutor(exec);
        Registry.register(TaskExecutor.class, exec);
    }
    
    public void waitAndStop() {
        while(!exec.isMailboxEmpty()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        TaskExecutor.stopTaskExecutor(exec);
    }
    
    @Override
    public void persist() {
        root.persist();
        for (PersistenceProvider provider : Registry.optional(PersistenceProvider.class)) {
            provider.createBlobStore();
        }
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
    // TODO : Async task for persist
    // TODO : Blobs in separate folder
    // TODO : Service registry
    // TODO : Separate model from persitence
    // TODO : Persistence provider
    // TODO : File peristence provider
    
}
