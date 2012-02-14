package com.mancel01.thetreeof.visitor;

import com.mancel01.thetreeof.api.Visitor;
import com.mancel01.thetreeof.model.Leaf;
import com.mancel01.thetreeof.model.Node;

public class PrintVisitor implements Visitor<Node> {
    
    private final LeafVisitor leafVisitor = new LeafVisitor();

    @Override
    public void visiting(Node visited) {
        System.out.println("Node : " + visited.getFullName());
        for (Leaf leaf : visited.leafs()) {
            leaf.visit(leafVisitor);
        }
    }
    
    public static class LeafVisitor implements Visitor<Leaf> {

        @Override
        public void visiting(Leaf visited) {
            System.out.println("Leaf : " + visited.getFullName());
        }
    } 
}
