package com.mancel01.thetreeof.model;

import com.mancel01.thetreeof.Tree;
import com.mancel01.thetreeof.api.*;
import com.mancel01.thetreeof.task.TaskExecutor;
import com.mancel01.thetreeof.util.Registry;
import java.util.UUID;

public class Leaf implements Persistable, Visitable<Leaf> {
    
    public static final String META_FILE_NAME = "metadata.properties";
    
    private final String uuid = UUID.randomUUID().toString();
    private String name;
    private Node parent;
    private String fullName;

    private String blob;
    
    public Leaf(String name, Node parent, final byte[] payload) {
        this.name = name;
        this.fullName = parent.getFullName() + Tree.PATH_SEPARATOR + name;
        this.parent = parent;
        if (payload != null) {
            blob = UUID.randomUUID().toString(); 
            for (TaskExecutor exec : Registry.optional(TaskExecutor.class)) {
                exec.addTask(new Task() {
                    @Override
                    public void apply() {
                        for (PersistenceProvider provider : Registry.optional(PersistenceProvider.class)) {
                            provider.persistAsBlob(blob, payload);
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

    public void changeBlob(final byte[] payload) {
        blob = UUID.randomUUID().toString(); 
        for (TaskExecutor exec : Registry.optional(TaskExecutor.class)) {
            exec.addTask(new Task() {
                @Override
                public void apply() {
                    for (PersistenceProvider provider : Registry.optional(PersistenceProvider.class)) {
                        provider.persistAsBlob(blob, payload);
                    }
                }

                @Override
                public String toString() {
                    return "Change blob for leaf " + uuid;
                }
            });
        }
    }
    
    public void changeName(final String name) {

    }

    void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public byte[] getBlob() {
        for (PersistenceProvider provider : Registry.optional(PersistenceProvider.class)) {
            return provider.getBlob(blob);
        }
        return new byte[0];
    }

    public String getBlobId() {
        return blob;
    }
    
    public Leaf me() {
        return this;
    }

    public String getName() {
        return name;
    }
    
    @Override
    public void persist() {
        for (TaskExecutor exec : Registry.optional(TaskExecutor.class)) {
            exec.addTask(new Task() {
                @Override
                public void apply() {
                    for (PersistenceProvider provider : Registry.optional(PersistenceProvider.class)) {
                        provider.persistLeaf(me());
                    }
                }

                @Override
                public String toString() {
                    return "Create Leaf path and metadata for " + uuid;
                }
            });
        }
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
    
    public static LeafCreator leaf(String name, byte[] bytes) {
        return new LeafCreator(name, bytes);
    }
    
    public static class LeafCreator implements Creator<Leaf> {

        private final String name;
        private final byte[] bytes;

        public LeafCreator(String name, byte[] bytes) {
            this.name = name;
            this.bytes = bytes;
        }
        
        public LeafCreator(String name) {
            this.name = name;
            this.bytes = new byte[0];
        }
        
        @Override
        public Leaf create(Node parent) {
            return new Leaf(name, parent, bytes);
        }
    }
}
