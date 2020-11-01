package io.github.kladdkaka.kaffe;


import org.junit.jupiter.api.Test;
import org.xmlunit.builder.Input;

import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.xmlunit.matchers.CompareMatcher.isIdenticalTo;

class StackNodeTest {

    @Test
    void toHtml() {
        StackNode stackNode = new StackNode(
                "com.android.dx.dex.code.RopTranslator.translate()",
                400,
                new StackNode(
                        "com.android.dx.dex.code.RopTranslator.translateAndGetResult()",
                        300,
                        new StackNode(
                                "com.android.dx.dex.code.RopTranslator.outputInstructions()",
                                300,
                                new StackNode(
                                        "com.android.dx.dex.code.RopTranslator.outputBlock()",
                                        300,
                                        new StackNode(
                                                "com.android.dx.rop.code.InsnList.forEach()",
                                                300,
                                                new StackNode(
                                                        "com.android.dx.rop.code.PlainInsn.accept()",
                                                        300,
                                                        new StackNode(
                                                                "com.android.dx.dex.code.RopTranslator$LocalVariableAwareTranslationVisitor.visitPlainInsn()",
                                                                300,
                                                                new StackNode(
                                                                        "com.android.dx.dex.code.RopTranslator$LocalVariableAwareTranslationVisitor.addIntroductionIfNecessary()",
                                                                        300,
                                                                        new StackNode(
                                                                                "com.android.dx.rop.code.LocalVariableInfo.getAssignment()",
                                                                                300,
                                                                                new StackNode(
                                                                                        "java.util.HashMap.get()",
                                                                                        300,
                                                                                        new StackNode(
                                                                                                "java.util.HashMap.hash()",
                                                                                                300,
                                                                                                new StackNode(
                                                                                                        "com.android.dx.rop.code.Insn.hashCode()",
                                                                                                        300,
                                                                                                        new StackNode("java.lang.System.identityHashCode()", 300)
                                                                                                )
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                ),
                new StackNode(
                        "com.android.dx.dex.code.RopTranslator.<init>()",
                        100,
                        new StackNode(
                                "com.android.dx.dex.code.OutputCollector.<init>()",
                                100,
                                new StackNode("com.android.dx.dex.code.OutputFinisher.<init>()", 100)
                        )
                )
        );

        String html = stackNode.toHtml();

        URL url = getClass().getClassLoader().getResource("test-data/stacknode.xml");
        assertThat(html, isIdenticalTo(Input.fromURL(url)).normalizeWhitespace());
    }
}