/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.lttng.scope.common;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.lttng.scope.common.StreamUtils.StreamFlattener;

/**
 * Test for {@link StreamFlattener}.
 *
 * @author Alexandre Montplaisir
 */
public class StreamFlattenerTest {

    /**
     * Test flattening a tree.
     *
     * Each node has a String value, and they will be organized as:
     *
     * <pre>
     *     A
     *    / \
     *   B   C
     *  /|   |\
     * D E   F G
     * </pre>
     *
     * In-order, depth-first visiting should give the following sequence:<br>
     * A -> B -> D -> E -> C -> F -> G
     */
    @Test
    public void testFlattenTree() {
        /* Prepare the tree */
        TreeNode nodeD = new TreeNode(new TreeNode[0], "D");
        TreeNode nodeE = new TreeNode(new TreeNode[0], "E");
        TreeNode nodeF = new TreeNode(new TreeNode[0], "F");
        TreeNode nodeG = new TreeNode(new TreeNode[0], "G");
        TreeNode nodeB = new TreeNode(new TreeNode[] {nodeD, nodeE}, "B");
        TreeNode nodeC = new TreeNode(new TreeNode[] {nodeF, nodeG}, "C");
        TreeNode nodeA = new TreeNode(new TreeNode[] {nodeB, nodeC}, "A");

        /* Run the test */
        StreamFlattener<TreeNode> sf = new StreamFlattener<>(node -> Arrays.stream(node.getChildren()));

        List<String> expected = Arrays.asList("A", "B", "D", "E", "C", "F", "G");
        List<String> results = sf.flatten(nodeA).map(TreeNode::getValue).collect(Collectors.toList());

        assertEquals(expected, results);
    }

    private static class TreeNode {

        private final @NotNull TreeNode[] children;
        private final String value;

        public TreeNode(@NotNull TreeNode[] children, String value) {
            this.children = children;
            this.value = value;
        }

        public @NotNull TreeNode[] getChildren() {
            return children;
        }

        public String getValue() {
            return value;
        }
    }
}