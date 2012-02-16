package com.mancel01.thetreeof;

import com.mancel01.thetreeof.api.Persistable;
import com.mancel01.thetreeof.api.PersistenceProvider;
import com.mancel01.thetreeof.api.Store;
import com.mancel01.thetreeof.file.FilePersistenceProvider;
import com.mancel01.thetreeof.model.Leaf;
import com.mancel01.thetreeof.model.Node;
import com.mancel01.thetreeof.task.TaskExecutor;
import com.mancel01.thetreeof.util.F.Option;
import com.mancel01.thetreeof.util.*;
import java.io.File;
import java.util.Collection;

public class Tree implements Persistable<Tree> {
    
    public static final String PATH_SEPARATOR = "/";
    
    private Node root;
    private final File rootFile;
    private final Store config;
    private Registry registry;
    private TaskExecutor exec;
    
    static {
        SimpleLogger.enableColors(true);
        SimpleLogger.enableTrace(true);
    }
    
    public Tree() {
        this.config = new FileStore("config.properties");
        this.rootFile = new File(config.get("root").getOrElse("./repo"));
        init(new FilePersistenceProvider(rootFile));
    }
    
    public Tree(PersistenceProvider provider) {
        assert provider != null;
        this.config = new FileStore("config.properties");
        this.rootFile = new File(config.get("root").getOrElse("./repo"));
        init(provider);
    }
    
    public Tree(File rootFile, String config, PersistenceProvider provider) {
        assert provider != null;
        assert config != null;
        assert rootFile != null;
        this.config = new FileStore(config);
        this.rootFile = rootFile;
        init(provider);
    }
    
    public Tree(File rootFile, String config) {
        assert config != null;
        assert rootFile != null;
        this.config = new FileStore(config);
        this.rootFile = rootFile;
        init(new FilePersistenceProvider(rootFile));
    }
    
    public Tree(Store configuration) {
        assert configuration != null;
        this.config = configuration;
        this.rootFile = new File(config.get("root").getOrElse("./repo"));
        init(new FilePersistenceProvider(rootFile));
    }
    
    public Tree(Store configuration, PersistenceProvider provider) {
        assert configuration != null;
        assert provider != null;
        this.config = configuration;
        this.rootFile = new File(config.get("root").getOrElse("./repo"));
        init(provider);
    }
    
    public void waitAndStop() {
        while(!exec.isMailboxEmpty()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        exec.waitForLastTask();
        TaskExecutor.stopTaskExecutor(exec);
    }
    
    public void destroy() {
        for (PersistenceProvider provider : registry.optional(PersistenceProvider.class)) {
            provider.destroyTree();
        }
    }
    
    private void init(PersistenceProvider provider) {
        assert provider != null;
        this.root = new Node(this, "root");
        registry = new Registry();
        registry.register(PersistenceProvider.class, provider);
        exec = new TaskExecutor(registry);
        TaskExecutor.startTaskExecutor(exec);
        registry.register(TaskExecutor.class, exec);
        save();
    }
    
    @Override
    public Promise<Tree> save() {
        final Promise<Tree> promise = new Promise<Tree>();
        for (PersistenceProvider provider : registry.optional(PersistenceProvider.class)) {
            provider.createBlobStore();
        }
        Promise<Node> node = root.save();
        node.onRedeem(new F.Action<Node>() {

            @Override
            public void apply(Node t) {
                try {
                    promise.apply(t.tree());
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
        assert expression != null;
        Collection<Node> nodes = allNodes();
        for (Node node : nodes) {
            if (node.getFullName().toLowerCase().equals(expression.toLowerCase())) {
                return Option.some(node);
            }
        }
        return Option.none();
    }
    
    public Option<Leaf> selectLeaf(String expression) {
        assert expression != null;
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
    // TODO : method update with message in node/leaf
}
