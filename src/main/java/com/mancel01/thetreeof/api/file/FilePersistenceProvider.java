package com.mancel01.thetreeof.api.file;

import com.google.common.io.Files;
import com.mancel01.thetreeof.Tree;
import com.mancel01.thetreeof.api.PersistenceProvider;
import com.mancel01.thetreeof.model.Leaf;
import com.mancel01.thetreeof.model.Node;
import com.mancel01.thetreeof.util.Configuration;
import com.mancel01.thetreeof.util.F;
import java.io.File;
import java.io.IOException;

public class FilePersistenceProvider implements PersistenceProvider {
    
    private final File rootFile;
    private final File blobStore;

    public FilePersistenceProvider(File rootFile) {
        this.rootFile = rootFile;
        this.blobStore = new File(rootFile, "blobs");
    }

    @Override
    public void createBlobStore() {
        if (!blobStore.exists()) {
            blobStore.mkdirs();
        }
    }

    @Override
    public void persistAsBlob(String uuid, byte[] bytes) {
        try {
            File blob = new File(blobStore, uuid);
            if (!blob.exists()) {
                Files.write(bytes, blob);
            }
        } catch (IOException ex) {
            throw new F.ExceptionWrapper(ex);
        }
    }

    @Override
    public byte[] getBlob(String uuid) {
        try {
            File blob = new File(blobStore, uuid);
            return Files.toByteArray(blob);
        } catch (IOException ex) {
            throw new F.ExceptionWrapper(ex);
        }
    }

    @Override
    public void persistLeaf(Leaf leaf) {
        try {
            File path = new File(Tree.instance().rootFile(), leaf.getParent().getFullName() + Tree.PATH_SEPARATOR + leaf.getUuid());
            if (!path.exists()) {
                path.mkdirs();
            }
            File metadata = new File(path, Leaf.META_FILE_NAME);
            metadata.createNewFile();
            Configuration config = new Configuration(metadata.getAbsolutePath());
            config.set("uuid", leaf.getUuid());
            config.set("name", leaf.getName());
            config.set("fullName", leaf.getFullName());
            config.set("blobId", leaf.getBlobId());
            config.set("created", leaf.created().toString());       
            config.persist();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void persistNode(Node node) {
        File path = null;
        if (node.getParent() == null) {
            path = new File(Tree.instance().rootFile(), node.getName());
        } else {
            path = new File(Tree.instance().rootFile(), node.getParent().getFullName() + Tree.PATH_SEPARATOR + node.getName());
        }
        if (!path.exists()) {
            path.mkdirs();
        }
        for (Node n : node.children()) {
            n.persist();
        }
        for (Leaf leaf : node.leafs()) {
            leaf.persist();
        }
    }
}
