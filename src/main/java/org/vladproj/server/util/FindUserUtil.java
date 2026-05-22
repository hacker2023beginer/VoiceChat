package org.vladproj.server.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FindUserUtil {
    class Node {
        Map<Character, Node> children = new HashMap<>();
        boolean isEnd;
        String value;
    }

    private final Node root = new Node();

    public void insert(String username) {
        Node node = root;
        for (char c : username.toCharArray()) {
            node = node.children.computeIfAbsent(Character.toLowerCase(c), k -> new Node());
        }
        node.isEnd = true;
        node.value = username;
    }

    public List<String> searchPrefix(String prefix) {
        Node node = root;
        for (char c : prefix.toCharArray()) {
            node = node.children.get(Character.toLowerCase(c));
            if (node == null) return List.of();
        }
        List<String> result = new ArrayList<>();
        collect(node, result);
        return result;
    }

    private void collect(Node node, List<String> result) {
        if (node.isEnd) {
            result.add(node.value);
        }
        for (var entry : node.children.entrySet()) {
            collect(entry.getValue(), result);
        }
    }
}
