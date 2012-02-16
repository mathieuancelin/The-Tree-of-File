package com.mancel01.thetreeof.model;

import com.mancel01.thetreeof.Tree;
import com.mancel01.thetreeof.api.*;
import com.mancel01.thetreeof.model.Leaf.LeafCreator;
import com.mancel01.thetreeof.task.TaskExecutor;
import com.mancel01.thetreeof.util.Promise;
import com.mancel01.thetreeof.util.SimpleLogger;
import java.util.*;

public class Node implements Persistable<Node>, Visitable<Node> {
    
    private final String uuid = UUID.randomUUID().toString();
    private String name;
    private final String fullName;
    private final Node parent;
    private final List<Node> children = Collections.synchronizedList(new ArrayList<Node>());
    private final List<Leaf> leafs = Collections.synchronizedList(new ArrayList<Leaf>());

    private final Tree tree;
    
    public Node(Tree tree, String name) {
        assert tree != null;
        assert name != null;
        assert name.indexOf("/") == -1;
        this.tree = tree;
        this.parent = null;
        this.name = name;
        this.fullName = Tree.PATH_SEPARATOR + name;
        SimpleLogger.trace("create node {}", fullName);
    }

    public Node(Tree tree, String name, Node parent) {
        assert tree != null;
        assert name != null;
        assert name.indexOf("/") == -1;
        assert parent != null;
        this.tree = tree;
        this.parent = parent;
        this.name = name;
        this.fullName = parent.fullName + Tree.PATH_SEPARATOR + name;
        SimpleLogger.trace("create node {}", fullName);
    }
    
    public Node me() {
        return this;
    }
    
    public Tree tree() {
        return tree;
    }
    
    @Override
    public Promise<Node> save() {
        final Promise<Node> promise = new Promise<Node>();
        for (TaskExecutor exec : tree.reg().optional(TaskExecutor.class)) {
            exec.addTask(new Task() {
                @Override
                public void apply() {
                    for (PersistenceProvider provider : tree.reg().optional(PersistenceProvider.class)) {
                        provider.persistNode(me());
                    }
                    promise.apply(me());
                }

                @Override
                public String toString() {
                    return "Persist initial for Node " + uuid;
                }
            });
            return promise;
        }
        throw new RuntimeException("Should never happen");
    }
    
    public Node addLeaf(Leaf leaf) {
        this.leafs.add(leaf);
        return this;
    }
    
    public Node addLeaf(LeafCreator c) {
        Leaf leaf = c.create(this);
        this.leafs.add(leaf);
        return this;
    }
    
    public Leaf addLeafAndSelect(LeafCreator c) {
        Leaf leaf = c.create(this);
        this.leafs.add(leaf);
        return leaf;
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
    
    /**
     * 
     * @param c
     * @return WARNING return new node and not himself
     */
    public Node addChildAndSelect(NodeCreator c) {
        Node node = c.create(this);
        children.add(node);
        return node;
    }
    
    public Node addChild(NodeCreator c) {
        Node node = c.create(this);
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
    
    public Node back() {
        return getParent();
    }

    public Collection<Node> children() {
        return Collections.unmodifiableCollection(children);
    }
    
    public Collection<Leaf> leafs() {
        return Collections.unmodifiableCollection(leafs);
    }
    
    public void destroy() {
        for (TaskExecutor exec : tree.reg().optional(TaskExecutor.class)) {
            exec.addTask(new Task() {
                @Override
                public void apply() {
                    for (PersistenceProvider provider : tree.reg().optional(PersistenceProvider.class)) {
                        provider.destroyNode(me());
                    }
                }

                @Override
                public String toString() {
                    return "Destroy leaf " + uuid;
                }
            });
        }
    }

    public Node getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    /**public Node setName(String name) {
        this.name = name;
        return this;
    }**/

    public String getFullName() {
        return fullName;
    }

    @Override
    public void visit(Visitor<Node> visitor) {
        if (parent == null) {
            visitor.visiting(this);
        }
        for (Node node : children) {
            node.visit(visitor);
        }
        if (parent != null) {
            visitor.visiting(this);
        }
    }
    
    public static NodeCreator node(String name) {
        return new NodeCreator(name);
    }
    
    public static class NodeCreator implements Creator<Node> {

        private final String name;

        public NodeCreator(String name) {
            this.name = name;
        }
        
        @Override
        public Node create(Node parent) {
            return new Node(parent.tree(), name, parent);
        }
    }
}
