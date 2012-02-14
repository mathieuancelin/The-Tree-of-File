package com.mancel01.thetreeof.model;

import com.mancel01.thetreeof.api.Change;
import com.google.common.io.Files;
import com.mancel01.thetreeof.Tree;
import com.mancel01.thetreeof.api.*;
import com.mancel01.thetreeof.util.F;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class Leaf implements Persistable, Visitable<Leaf> {
    
    public static final String META_FILE_NAME = "metadata.properties";
    
    private final String uuid = UUID.randomUUID().toString();
    private String name;
    private Node parent;
    private File path;
    private String fullName;

    private File blob;
    
    public Leaf(String name, Node parent) {
        this.name = name;
        this.fullName = parent.getFullName() + Tree.PATH_SEPARATOR + name;
        this.parent = parent;
        this.path = new File(parent.getPath(), uuid);
    }
    
    public Leaf(File b, Node parent) {
        this.name = b.getName();
        this.fullName = parent.getFullName() + Tree.PATH_SEPARATOR + name;
        this.parent = parent;
        this.path = new File(parent.getPath(), uuid);
    }

    /**public Leaf setBlob(File blob) {
        this.blob = blob;
        return this;
    }**/
    
    public void changeName(final String name) {
        final Leaf leaf = this;
        Task task = new Task() {
            
            Change<Leaf, String> change = new Change<Leaf, String>() {

                @Override
                public void applyChange(Leaf l, String name) {
                    l.setName(name);
                    l.persist();
                }
            };

            @Override
            public void apply() {
                change.applyChange(leaf, name);
            }
        };
    }

    void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }
    
    @Override
    public void persist() {
        if (!path.exists()) {
            path.mkdirs();
        }
        File metadata = new File(path, META_FILE_NAME);
        try {
            if (blob == null) {
                blob = new File(path, name);
                Files.touch(blob);
            } else {
                Files.copy(blob, new File(path, blob.getName()));
            }
            // TODO : persist meta
            Files.touch(metadata);
        } catch (IOException ex) {
            throw new F.ExceptionWrapper(ex);
        }
    }

    @Override
    public void visit(Visitor<Leaf> visitor) {
        visitor.visiting(this);
    }
}
