package com.mancel01.thetreeof;

import com.mancel01.thetreeof.api.Persistable;
import com.mancel01.thetreeof.api.PersistenceProvider;
import com.mancel01.thetreeof.file.FilePersistenceProvider;
import com.mancel01.thetreeof.model.Leaf;
import com.mancel01.thetreeof.model.Node;
import com.mancel01.thetreeof.task.TaskExecutor;
import com.mancel01.thetreeof.util.Configuration;
import com.mancel01.thetreeof.util.F;
import com.mancel01.thetreeof.util.F.Option;
import com.mancel01.thetreeof.util.Promise;
import com.mancel01.thetreeof.util.Registry;
import com.mancel01.thetreeof.util.SimpleLogger;
import java.io.File;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Tree implements Persistable<Tree> {
    
    public static final String PATH_SEPARATOR = "/";
    
    private Node root;
    private final File rootFile;
    private final Configuration config;
    private Registry registry;
    private TaskExecutor exec;
    
    static {
        SimpleLogger.enableColors(true);
        SimpleLogger.enableTrace(true);
    }
    
    public Tree() {
        this.config = new Configuration("config.properties");
        this.rootFile = new File(config.get("root").getOrElse("./repo"));
        init(new FilePersistenceProvider(rootFile));
    }
    
    public Tree(PersistenceProvider provider) {
        this.config = new Configuration("config.properties");
        this.rootFile = new File(config.get("root").getOrElse("./repo"));
        init(provider);
    }
    
    public Tree(File rootFile, String config, PersistenceProvider provider) {
        this.config = new Configuration(config);
        this.rootFile = rootFile;
        init(provider);
    }
    
    public Tree(File rootFile, String config) {
        this.config = new Configuration(config);
        this.rootFile = rootFile;
        init(new FilePersistenceProvider(rootFile));
    }
    
    public Tree(Configuration configuration) {
        this.config = configuration;
        this.rootFile = new File(config.get("root").getOrElse("./repo"));
        init(new FilePersistenceProvider(rootFile));
    }
    
    public Tree(Configuration configuration, PersistenceProvider provider) {
        this.config = configuration;
        this.rootFile = new File(config.get("root").getOrElse("./repo"));
        init(provider);
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
        for (PersistenceProvider provider : registry.optional(PersistenceProvider.class)) {
            provider.destroyTree();
        }
    }
    
    private void init(PersistenceProvider provider) {
        this.root = new Node(this, "root");
        registry = new Registry();
        registry.register(PersistenceProvider.class, provider);
        exec = new TaskExecutor();
        TaskExecutor.startTaskExecutor(exec);
        registry.register(TaskExecutor.class, exec);
    }
    
    @Override
    public Promise<Tree> persist() {
        final Promise<Tree> promise = new Promise<Tree>();
        for (PersistenceProvider provider : registry.optional(PersistenceProvider.class)) {
            provider.createBlobStore();
        }
        Promise<Node> node = root.persist();
        node.onRedeem(new F.Action<Promise<Node>>() {

            @Override
            public void apply(Promise<Node> t) {
                try {
                    promise.apply(t.get().tree());
                } catch (Exception ex) {}
            }
        });
        return promise;
    }
    
    public File rootFile() {
        return rootFile;
    }

    public Node root() {
        return root;
    }

    public synchronized Registry reg() {
        return registry;
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
            if (node.getFullName().toLowerCase().equals(expression.toLowerCase())) {
                return Option.some(node);
            }
        }
        return Option.none();
    }
    
    public Option<Leaf> selectLeaf(String expression) {
        Collection<Leaf> leafs = allLeafs();
        for (Leaf leaf : leafs) {
            if (leaf.getFullName().toLowerCase().equals(expression.toLowerCase())) {
                return Option.some(leaf);
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
    // TODO : add search methods
    // TODO : Store intead of Configuration, then implems
    // TODO : method update with message in node/leaf
    // TODO : task listener
}
