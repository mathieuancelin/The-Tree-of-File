package com.mancel01.thetreeof.file;

import com.google.common.base.Joiner;
import com.google.common.io.Files;
import com.mancel01.thetreeof.Tree;
import com.mancel01.thetreeof.api.Blob;
import com.mancel01.thetreeof.api.PersistenceProvider;
import com.mancel01.thetreeof.blob.FileBlob;
import com.mancel01.thetreeof.model.Leaf;
import com.mancel01.thetreeof.model.Node;
import com.mancel01.thetreeof.util.FileStore;
import com.mancel01.thetreeof.util.F;
import com.mancel01.thetreeof.util.SimpleLogger;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

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
    public void persistAsBlob(String uuid, Blob bytes) {
        try {
            File blob = new File(blobStore, uuid);
            if (!blob.exists()) {
                Files.write(bytes.bytes(), blob);
            }
        } catch (IOException ex) {
            throw new F.ExceptionWrapper(ex);
        }
    }

    @Override
    public Blob getBlob(String uuid) {
        return new FileBlob(new File(blobStore, uuid));
    }

    @Override
    public void persistLeaf(Leaf leaf) {
        try {
            File path = new File(rootFile, leaf.getParent().getFullName() + Tree.PATH_SEPARATOR + leaf.getUuid());
            if (!path.exists()) {
                path.mkdirs();
            }
            File metadata = new File(path, Leaf.META_FILE_NAME);
            metadata.createNewFile();
            FileStore config = new FileStore(metadata.getAbsolutePath());
            config.set("uuid", leaf.getUuid());
            config.set("name", leaf.getName());
            config.set("fullName", leaf.getFullName());
            //config.set("blobId", leaf.getBlobId());
            config.set("created", leaf.created().toString()); 
            config.set("changed", leaf.lastChanged().toString());
            config.set("parent", leaf.getParent().getFullName());
            config.set("metadata", Joiner.on(",").join(leaf.getMetadata()));
            config.set("version", "" + leaf.getVersion());
            List<String> versions = new ArrayList<String>();
            for (Entry<Long, String> entry : leaf.getVersions().entrySet()) {
                versions.add(entry.getKey() + ";" + entry.getValue());
            }
            config.set("versions", Joiner.on(",").join(versions));
            config.persist();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void persistNode(Node node) {
        File path = null;
        if (node.getParent() == null) {
            path = new File(rootFile, node.getName());
        } else {
            path = new File(rootFile, node.getParent().getFullName() + Tree.PATH_SEPARATOR + node.getName());
        }
        if (!path.exists()) {
            path.mkdirs();
        }
        for (Node n : node.children()) {
            n.save();
        }
        for (Leaf leaf : node.leafs()) {
            leaf.save();
        }
    }

    @Override
    public void destroyTree() {
        destroy(rootFile);
    }
    
    private void destroy(File f) {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                destroy(c);
            }
        }
        if (!f.delete()) {
            SimpleLogger.error("Unable to delete {}", f.getAbsolutePath());
        }
    }

    @Override
    public void destroyNode(Node node) {
        Collection<Leaf> leafs = node.allLeafsBelow();
        for (Leaf leaf : leafs) {
            destroyLeaf(leaf);
        }
        File path = null;
        if (node.getParent() == null) {
            path = new File(rootFile, node.getName());
        } else {
            path = new File(rootFile, node.getParent().getFullName() + Tree.PATH_SEPARATOR + node.getName());
        }
        destroy(path);
    }

    @Override
    public void destroyLeaf(Leaf leaf) {
        File blob = new File(blobStore, leaf.getBlobId());
        File path = new File(rootFile, leaf.getParent().getFullName() + Tree.PATH_SEPARATOR + leaf.getUuid());
        File metadata = new File(path, Leaf.META_FILE_NAME);
        blob.delete();
        metadata.delete();
        path.delete();
    }
}
