package com.mancel01.thetreeof.model;

import com.mancel01.thetreeof.Tree;
import com.mancel01.thetreeof.api.*;
import com.mancel01.thetreeof.blob.EmptyBlob;
import com.mancel01.thetreeof.task.TaskExecutor;
import com.mancel01.thetreeof.util.Promise;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Leaf implements Persistable<Leaf>, Visitable<Leaf> {
    
    public static final String META_FILE_NAME = "metadata.properties";
    
    private final String uuid = UUID.randomUUID().toString();
    private String name;
    private Node parent;
    private String fullName;
    private Date created = new Date();
    private Date lastChanged = new Date();
    private final Map<String, Metadata<String, String>> metadata = 
            new HashMap<String, Metadata<String, String>>();
    
    private final Map<Long, String> versions = new HashMap<Long, String>();
    
    private Long currentVersion = 0L; 
        
    private final Tree tree;
    
    public Leaf(Tree tree, String name, Node parent, final Blob payload) {
        assert tree != null;
        assert parent != null;
        assert payload != null;
        assert name != null;
        assert name.indexOf("/") == -1;
        this.name = name;
        this.tree = tree;
        this.fullName = parent.getFullName() + Tree.PATH_SEPARATOR + name;
        this.parent = parent;
        if (payload != null) {
            final long version = updateAndIncreaseVersion(UUID.randomUUID().toString()); 
            for (TaskExecutor exec : tree.reg().optional(TaskExecutor.class)) {
                exec.addTask(new Task() {
                    @Override
                    public void apply() {
                        for (PersistenceProvider provider : me().tree.reg().optional(PersistenceProvider.class)) {
                            provider.persistAsBlob(getBlobId(version), payload);
                        }
                    }

                    @Override
                    public String toString() {
                        return "Persist initial blob for leaf " + uuid;
                    }
                });
            }
        }
    }
    
    private long updateAndIncreaseVersion(String newBlodId) {
        currentVersion++;
        versions.put(currentVersion, newBlodId);
        save();
        return currentVersion;
    }

    public Tree tree() {
        return tree;
    }

    public void changeBlob(final Blob payload) {
        final long version = updateAndIncreaseVersion(UUID.randomUUID().toString()); 
        for (TaskExecutor exec : tree.reg().optional(TaskExecutor.class)) {
            exec.addTask(new Task() {
                @Override
                public void apply() {
                    for (PersistenceProvider provider : tree.reg().optional(PersistenceProvider.class)) {
                        provider.persistAsBlob(getBlobId(version), payload);
                    }
                }

                @Override
                public String toString() {
                    return "Change blob for leaf " + uuid;
                }
            });
        }
        lastChanged = new Date();
        save();
    }
    
    public void changeName(final String name) {
        this.name = name;
        this.fullName = parent.getFullName() + Tree.PATH_SEPARATOR + name;
        lastChanged = new Date();
        // TODO : move files
        save();
    }
    
    public void destroy() {
        for (TaskExecutor exec : tree.reg().optional(TaskExecutor.class)) {
            exec.addTask(new Task() {
                @Override
                public void apply() {
                    for (PersistenceProvider provider : tree.reg().optional(PersistenceProvider.class)) {
                        provider.destroyLeaf(me());
                    }
                }

                @Override
                public String toString() {
                    return "Destroy leaf " + uuid;
                }
            });
        }
    }

    void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public Date created() {
        return created;
    }

    public Date lastChanged() {
        return lastChanged;
    }
    
    public Leaf addMetadata(String key, String value) {
        metadata.put(key, new Metadata<String, String>(key, value));
        save();
        return this;
    }
    
    public Leaf clearMetadata() {
        metadata.clear();
        save();
        return this;
    }
    
    public Leaf removeMetadata(String key) {
        metadata.remove(key);
        save();
        return this;
    }
    
    public Metadata<String, String> getMetadata(String key) {
        return metadata.get(key);
    }

    public Collection<Metadata<String, String>> getMetadata() {
        return Collections.unmodifiableCollection(metadata.values());
    }

    public Blob getBlob() {
        for (PersistenceProvider provider : tree.reg().optional(PersistenceProvider.class)) {
            return provider.getBlob(getBlobId());
        }
        return EmptyBlob.INSTANCE;
    }
    
    public Blob getBlob(long version) {
        for (PersistenceProvider provider : tree.reg().optional(PersistenceProvider.class)) {
            return provider.getBlob(getBlobId(version));
        }
        return EmptyBlob.INSTANCE;
    }

    public String getBlobId() {
        return versions.get(currentVersion);
    }
    
    public String getBlobId(long version) {
        return versions.get(version);
    }

    public void changeVersion(long version) {
        this.currentVersion = version;
        save();
    }

    public long getVersion() {
        return currentVersion;
    }

    public Map<Long, String> getVersions() {
        return versions;
    }
    
    public Leaf me() {
        return this;
    }

    public String getName() {
        return name;
    }
    
    @Override
    public Promise<Leaf> save() {
        final Promise<Leaf> promise = new Promise<Leaf>();
        for (TaskExecutor exec : tree.reg().optional(TaskExecutor.class)) {
            exec.addTask(new Task() {
                @Override
                public void apply() {
                    for (PersistenceProvider provider : tree.reg().optional(PersistenceProvider.class)) {
                        provider.persistLeaf(me());
                        promise.apply(me());
                    }
                }

                @Override
                public String toString() {
                    return "Create Leaf path and metadata for " + uuid;
                }
            });
            return promise;
        }
        throw new RuntimeException("Should never append");
    }

    @Override
    public void visit(Visitor<Leaf> visitor) {
        visitor.visiting(this);
    }
    
    public Node back() {
        return getParent();
    }

    public Node getParent() {
        return parent;
    }

    public String getUuid() {
        return uuid;
    }
    
    public static LeafCreator leaf(String name) {
        return new LeafCreator(name);
    }
    
    public static LeafCreator leaf(String name, Blob bytes) {
        return new LeafCreator(name, bytes);
    }
    
    public static class LeafCreator implements Creator<Leaf> {

        private final String name;
        private final Blob bytes;

        public LeafCreator(String name, Blob bytes) {
            this.name = name;
            this.bytes = bytes;
        }
        
        public LeafCreator(String name) {
            this.name = name;
            this.bytes = EmptyBlob.INSTANCE;
        }
        
        @Override
        public Leaf create(Node parent) {
            return new Leaf(parent.tree(), name, parent, bytes);
        }
    }
}
