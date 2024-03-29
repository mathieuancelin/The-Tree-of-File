package com.mancel01.thetreeof;

import com.mancel01.thetreeof.api.Blob;
import com.mancel01.thetreeof.blob.FileBlob;
import com.mancel01.thetreeof.blob.StringBlob;
import com.mancel01.thetreeof.model.Leaf;
import static com.mancel01.thetreeof.model.Leaf.leaf;
import com.mancel01.thetreeof.model.Node;
import static com.mancel01.thetreeof.model.Node.node;
import com.mancel01.thetreeof.visitor.PrintVisitor;
import java.io.File;
import org.junit.Test;


public class AppTest {

    @Test
    public void testApp() throws Exception {
        Tree tree = new Tree(new File("./repo1"), "dummy");
        Node root = tree.root();
        Blob bytes = new FileBlob("pom.xml");
        bytes = new StringBlob("Yo dude !!!");
        root.addChildAndSelect(node("categ1"))
                    .addLeafAndSelect(leaf("doc1", bytes))
                            .addMetadata("key1", "value1")
                            .addMetadata("key2", "value2")
                            .addMetadata("key3", "value3")
                        .back()
                    .addLeaf(leaf("doc2", bytes))
                .back()
            .addChildAndSelect(
                node("categ2"))
                    .addLeaf(leaf("doc3", bytes))
                    .addLeafAndSelect(leaf("doc4", bytes))
                            .addMetadata("key2", "value2")
                            .addMetadata("key3", "value3")
                        .back()
                .back()
            .addChildAndSelect(node("categ3"))
                .addChildAndSelect(node("categ31"))
                    .back()
                .addChildAndSelect(node("categ32"))
                    .addChildAndSelect(node("categ321"))
                        .back()
                    .back()
                .addChildAndSelect(node("categ33"))
                    .back()
                .addChildAndSelect(node("categ34"))
                    .back()
                .back()
            .addChildAndSelect(node("categ4"))
                .back()
            .addChildAndSelect(node("categ5"))
                .back()
            .addChildAndSelect(node("categ6"))
                .back();
        tree.save();
        for (Leaf leaf : tree.selectLeaf("/Root/categ1/doc1") ) {
            leaf.changeBlob(bytes);
        }
        root.visit(new PrintVisitor());
        tree.waitAndStop();
        Tree tree2 = new Tree(new File("./repo2"), "dummy");
        tree2.root().addChildAndSelect(node("categ1"))
                    .addLeafAndSelect(leaf("doc1", bytes))
                            .addMetadata("key1", "value1")
                            .addMetadata("key2", "value2")
                            .addMetadata("key3", "value3")
                        .back()
                    .addLeaf(leaf("doc2", bytes));
        tree2.save().get();
        tree2.waitAndStop();
    }
}
