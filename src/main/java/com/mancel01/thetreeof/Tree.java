package com.mancel01.thetreeof;

import com.mancel01.thetreeof.api.Persistable;
import com.mancel01.thetreeof.api.PersistenceProvider;
import com.mancel01.thetreeof.file.FilePersistenceProvider;
import com.mancel01.thetreeof.model.Leaf;
import com.mancel01.thetreeof.model.Node;
import com.mancel01.thetreeof.task.TaskExecutor;
import com.mancel01.thetreeof.util.Configuration;
import com.mancel01.thetreeof.util.F.Option;
import com.mancel01.thetreeof.util.Registry;
import com.mancel01.thetreeof.util.SimpleLogger;
import java.io.File;
import java.util.Collection;

public class Tree implements Persistable {
    
    public static final String PATH_SEPARATOR = "/";
    
    private Node root;
    private final File rootFile;
    private final Configuration config;
    private TaskExecutor exec;
    
    static {
        SimpleLogger.enableColors(true);
        SimpleLogger.enableTrace(true);
    }
    
    public Tree() {
        this.config = new Configuration("config.properties");
        this.rootFile = new File(config.get("root").getOrElse("./repo"));
        init();
    }
    
    public Tree(File rootFile, String config) {
        this.config = new Configuration(config);
        this.rootFile = rootFile;
        init();
    }
    
    public Tree(Configuration configuration) {
        this.config = configuration;
        this.rootFile = new File(config.get("root").getOrElse("./repo"));
        init();
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
    
    public void destroyTree() {
        for (PersistenceProvider provider : Registry.optional(PersistenceProvider.class)) {
            provider.destroyTree();
        }
    }
    
    public void init() {
        this.root = new Node(this, "root");
        Registry.register(PersistenceProvider.class, new FilePersistenceProvider(rootFile));
        exec = new TaskExecutor();
        TaskExecutor.startTaskExecutor(exec);
        Registry.register(TaskExecutor.class, exec);
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
    
    public Option<Node> selectNode(String expression) {
        Collection<Node> nodes = allNodes();
        for (Node node : nodes) {
            if (node.getFullName().toLowerCase().matches(expression)) {
                return Option.some(node);
            }
        }
        return Option.none();
    }

//    
//    private static final Holder<Tree> HOLDER = TreeHolder.INSTANCE;
//    
//    public static synchronized Tree instance() {
//        return HOLDER.get();
//    }
//    
//    private static interface Holder<T> {
//        T get();
//    }
//    
//    private static enum TreeHolder implements Holder<Tree> {
//
//        INSTANCE {
//
//            private final Tree tree = new Tree();
//            
//            @Override
//            public Tree get() {
//                return tree;
//            }
//        }
//    }
    
    // TODO : Security visitor
    // TODO : Security provider
    // TODO : finish crud methods
    // TODO : add helpers methods to creates nodes and stuff like Tree.instance().select("/Root/machin/truc").addLeaf
    // TODO : add search methods
}
