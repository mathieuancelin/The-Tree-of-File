package com.mancel01.thetreeof;

import com.mancel01.thetreeof.model.Leaf;
import com.mancel01.thetreeof.model.Node;
import com.mancel01.thetreeof.visitor.PrintVisitor;
import java.io.File;
import org.junit.Test;

public class AppTest {

    @Test
    public void testApp() {
        Node root = Tree.instance().root();
        Node categ1 = new Node("categ1", root);
        Node categ2 = new Node("categ2", root);
        Leaf doc1 = new Leaf(new File("/Users/mathieuancelin/Desktop/mustache.java/nodetest.js"), categ1);
        Leaf doc2 = new Leaf("doc2", categ1);
        Leaf doc3 = new Leaf("doc3", categ2);
        Leaf doc4 = new Leaf("doc4", categ2);
        categ1.addLeafs(doc1, doc2);
        categ2.addLeafs(doc3, doc4);
        root.addChilds(categ1, categ2);
        Tree.instance().persist();
        PrintVisitor visitor = new PrintVisitor();
        Tree.instance().root().visit(visitor);
    }
}
