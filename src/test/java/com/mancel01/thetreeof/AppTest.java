package com.mancel01.thetreeof;

import com.google.common.io.Files;
import com.mancel01.thetreeof.model.Node;
import com.mancel01.thetreeof.visitor.PrintVisitor;
import org.junit.Test;

import static com.mancel01.thetreeof.model.Node.*;
import static com.mancel01.thetreeof.model.Leaf.*;
import java.io.File;


public class AppTest {

    @Test
    public void testApp() throws Exception {
        Node root = Tree.instance().root();
        byte[] bytes = Files.toByteArray(new File("pom.xml"));
        root.addChild(node("categ1"))
                    .addLeaf(leaf("doc1", bytes))
                    .addLeaf(leaf("doc2", bytes))
                .back()
            .addChild(
                node("categ2"))
                    .addLeaf(leaf("doc3", bytes))
                    .addLeaf(leaf("doc4", bytes))
                .back()
            .addChild(node("categ3"))
                .addChild(node("categ31"))
                    .back()
                .addChild(node("categ32"))
                    .addChild(node("categ321"))
                        .back()
                    .back()
                .addChild(node("categ33"))
                    .back()
                .addChild(node("categ34"))
                    .back()
                .back()
            .addChild(node("categ4"))
                .back()
            .addChild(node("categ5"))
                .back()
            .addChild(node("categ6"))
                .back();
        Tree.instance().persist();
        root.visit(new PrintVisitor());
        Tree.instance().waitAndStop();
    }
}
