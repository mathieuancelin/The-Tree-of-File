package com.mancel01.thetreeof.model;

import com.mancel01.thetreeof.Tree;
import com.mancel01.thetreeof.api.Metadata;
import com.mancel01.thetreeof.api.Persistable;
import com.mancel01.thetreeof.api.Visitable;
import com.mancel01.thetreeof.api.Visitor;
import java.io.File;
import java.util.*;

public class Node implements Persistable, Visitable<Node> {
    
    private final String uuid = UUID.randomUUID().toString();
    private String name;
    private final String fullName;
    private File path;
    private final Metadata metadata = new Metadata();
    private final Node parent;
    private final List<Node> children = Collections.synchronizedList(new ArrayList<Node>());
    private final List<Leaf> leafs = Collections.synchronizedList(new ArrayList<Leaf>());

    public Node(String name, File root) {
        this.parent = null;
        this.name = name;
        this.fullName = Tree.PATH_SEPARATOR + name;
        this.path = new File(root, name);
    }

    public Node(String name, Node parent) {
        this.parent = parent;
        this.name = name;
        this.fullName = parent.fullName + Tree.PATH_SEPARATOR + name;
        this.path = new File(parent.getPath(), name);
    }
    
    @Override
    public void persist() {
        if (!path.exists()) {
            path.mkdirs();
        }
        metadata.persist();
        for (Node node : children) {
            node.persist();
        }
        for (Leaf leaf : leafs) {
            leaf.persist();
        }
    }
    
    public Node addLeaf(Leaf leaf) {
        this.leafs.add(leaf);
        return this;
    }
    
    public Node addLeafs(Collection<Leaf> leafs) {
        this.leafs.addAll(leafs);
        return this;
    }
    
    public Node addLeafs(Leaf... leafs) {
        this.leafs.addAll(Arrays.asList(leafs));
        return this;
    }
    
    public Node addChild(Node node) {
        children.add(node);
        return this;
    }
    
    public Node addChilds(Collection<Node> nodes) {
        children.addAll(nodes);
        return this;
    }
    
    public Node addChilds(Node... nodes) {
        children.addAll(Arrays.asList(nodes));
        return this;
    }
    
    public Collection<Node> allNodesBelow() {
        Collection<Node> nodes = new ArrayList<Node>();
        nodes.add(this);
        for (Node node : children) {
            nodes.add(node);
        }
        return nodes;
    }
    
    public Collection<Leaf> allLeafsBelow() {
        Collection<Leaf> l = new ArrayList<Leaf>();
        Collection<Node> nodes = allNodesBelow();
        for (Node node : nodes) {
            l.addAll(node.leafs());
        }
        return l;
    }

    public Collection<Node> children() {
        return Collections.unmodifiableCollection(children);
    }
    
    public Collection<Leaf> leafs() {
        return Collections.unmodifiableCollection(leafs);
    }

    public Node getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    public Node setName(String name) {
        this.name = name;
        return this;
    }

    public File getPath() {
        return path;
    }

    public String getFullName() {
        return fullName;
    }

    @Override
    public void visit(Visitor<Node> visitor) {
        for (Node node : children) {
            visitor.visiting(node);
        }
        visitor.visiting(this);
    }
}
