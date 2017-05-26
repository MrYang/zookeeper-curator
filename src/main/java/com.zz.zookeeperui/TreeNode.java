package com.zz.zookeeperui;

import java.util.List;

/**
 * @author Mr.Yang
 * @since 2017-05-26
 */
public class TreeNode {

    private String text;
    private String fullPath;
    private List<TreeNode> nodes;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public List<TreeNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<TreeNode> nodes) {
        this.nodes = nodes;
    }
}
