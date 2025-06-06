package com.savostov.git_manager.service;

import lombok.Data;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GitService {

    @Data
    public static class Node {
        @Getter
        private String name;
        @Getter
        private String path;
        private boolean isDirectory;
        @Getter
        private List<Node> children = new ArrayList<>();

        public Node(String name, String path, boolean isDirectory) {
            this.name = name;
            this.path = path;
            this.isDirectory = isDirectory;
        }

        public boolean isDirectory() {

            return isDirectory;
        }

        public void setDirectory(boolean directory) {
            isDirectory = directory;
        }

        public void addChild(Node child) {
            this.children.add(child);
        }
    }

    private final String gitCommand = System.getenv().getOrDefault("GIT_COMMAND", "git");


    public Node getFileStructure(String repositoryPath) throws IOException, InterruptedException {
        Path repoPath = Paths.get(repositoryPath);
        Node root = new Node("", "", true);
        System.out.println("Getting file structure for repository: " + repositoryPath);

        try {
            List<String> gitOutput = executeGitCommand(repositoryPath, gitCommand, "ls-tree", "-r", "HEAD", "--name-only", "--full-tree");
            System.out.println("Git output size: " + gitOutput.size());
            processGitOutput(gitOutput, root, repoPath);
        } catch (IOException e) {
            System.out.println("No commits found, using ls-files");
            List<String> files = getFiles(repositoryPath);
            System.out.println("Found files: " + files.size());
            processGitOutput(files, root, repoPath);
        }


        printTree(root, "");

        return root;
    }

    private void printTree(Node node, String prefix) {
        System.out.println(prefix + "- " + node.getName() + " (" + node.getPath() + ") [" + (node.isDirectory() ? "dir" : "file") + "]");
        for (Node child : node.getChildren()) {
            printTree(child, prefix + "  ");
        }
    }

    private void processGitOutput(List<String> paths, Node root, Path repoPath) throws IOException {

        for (String path : paths) {
            if (path.trim().isEmpty() || path.startsWith(".git/")) continue;

            String[] parts = path.split("/");
            Node current = root;
            StringBuilder currentPath = new StringBuilder();


            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];
                if (currentPath.length() > 0) {
                    currentPath.append("/");
                }
                currentPath.append(part);

                boolean isDirectory = i < parts.length - 1;

                Node existingNode = findChild(current, part);
                if (existingNode == null) {
                    Node newNode = new Node(part, currentPath.toString(), isDirectory);
                    current.addChild(newNode);
                    current = newNode;
                } else {
                    if (isDirectory) {
                        existingNode.setDirectory(true);
                    }
                    current = existingNode;
                }
            }
        }

        try {
            Files.walk(repoPath)
                    .filter(Files::isDirectory)
                    .filter(path -> !path.equals(repoPath))
                    .filter(path -> !path.toString().contains(".git"))
                    .forEach(path -> {
                        String relativePath = repoPath.relativize(path).toString();
                        String[] parts = relativePath.split("/");
                        Node current = root;
                        StringBuilder currentPath = new StringBuilder();

                        for (String part : parts) {
                            if (currentPath.length() > 0) {
                                currentPath.append("/");
                            }
                            currentPath.append(part);

                            Node existingNode = findChild(current, part);
                            if (existingNode == null) {
                                Node newNode = new Node(part, currentPath.toString(), true);
                                current.addChild(newNode);
                                current = newNode;
                            } else {
                                existingNode.setDirectory(true);
                                current = existingNode;
                            }
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        sortNodes(root);
    }

    private void sortNodes(Node node) {
        if (node.getChildren().isEmpty()) {
            return;
        }


        node.getChildren().sort((a, b) -> {
            if (a.isDirectory() && !b.isDirectory()) {
                return -1;
            }
            if (!a.isDirectory() && b.isDirectory()) {
                return 1;
            }
            return a.getName().compareTo(b.getName());
        });


        for (Node child : node.getChildren()) {
            sortNodes(child);
        }
    }

    private Node findChild(Node parent, String name) {
        for (Node child : parent.getChildren()) {
            if (child.getName().equals(name)) {
                return child;
            }
        }
        return null;
    }

    private List<String> executeGitCommand(String repositoryPath, String... command) throws IOException, InterruptedException {
        List<String> output = new ArrayList<>();
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(new java.io.File(repositoryPath));
        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.add(line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("git command failed with exit code: " + exitCode + ", command: " + String.join(" ", command));
        }
        return output;
    }

    public String getFileContent(String repositoryPath, String filePath) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(gitCommand, "show", "HEAD:" + filePath);
        processBuilder.directory(new File(repositoryPath));
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        process.waitFor();
        return content.toString();
    }

    public void addFiles(String repositoryPath) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(gitCommand, "add", ".");
        processBuilder.directory(new File(repositoryPath));
        Process process = processBuilder.start();
        process.waitFor();
    }

    public void commitFiles(String repositoryPath, String commitMessage) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(gitCommand, "commit", "-m", commitMessage);
        processBuilder.directory(new File(repositoryPath));
        Process process = processBuilder.start();
        process.waitFor();
    }

    public List<String> getFiles(String repositoryPath) throws IOException, InterruptedException {
        Set<String> files = new HashSet<>();
        Path repoPath = Paths.get(repositoryPath);

        ProcessBuilder pb = new ProcessBuilder(gitCommand, "ls-files", "--others", "--exclude-standard", "--cached");
        pb.directory(new File(repositoryPath));
        pb.redirectErrorStream(true);
        Process p = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith(".git/")) {
                    files.add(line);

                    Path filePath = Paths.get(line);
                    while (filePath.getParent() != null) {
                        String parentPath = filePath.getParent().toString();
                        if (!parentPath.startsWith(".git")) {
                            files.add(parentPath);
                        }
                        filePath = filePath.getParent();
                    }
                }
            }
        }


//        Files.walk(repoPath)
//                .filter(path -> !path.equals(repoPath))
//                .filter(path -> {
//                    String relativePath = repoPath.relativize(path).toString();
//                    return !relativePath.startsWith(".git");
//                })
//                .forEach(path -> {
//                    String relativePath = repoPath.relativize(path).toString();
//                    files.add(relativePath);
//
//
//                    Path parent = path.getParent();
//                    while (parent != null && !parent.equals(repoPath)) {
//                        String parentPath = repoPath.relativize(parent).toString();
//                        if (!parentPath.startsWith(".git")) {
//                            files.add(parentPath);
//                        }
//                        parent = parent.getParent();
//                    }
//                });

        return new ArrayList<>(files);
    }

    public List<Node> getSubtreeAsList(String repositoryPath, String subPath) throws IOException, InterruptedException {
        Node root = getFileStructure(repositoryPath);
        if (subPath == null || subPath.isEmpty() || subPath.equals("root")) {
            return flattenTree(root);
        }

        Node subTree = findNodeByPath(root, subPath);
        if (subTree == null) {
            return new ArrayList<>();
        }

        return flattenTree(subTree);
    }

    private Node findNodeByPath(Node root, String path) {
        if (path == null || path.isEmpty()) {
            return root;
        }

        String[] parts = path.split("/");
        Node current = root;

        for (String part : parts) {
            Node found = null;
            for (Node child : current.getChildren()) {
                if (child.getName().equals(part)) {
                    found = child;
                    break;
                }
            }
            if (found == null) {
                return null;
            }
            current = found;
        }
        return current;
    }

    private List<Node> flattenTree(Node node) {
        List<Node> result = new ArrayList<>();
        if (node == null) {
            return result;
        }

        for (Node child : node.getChildren()) {
            if (child.isDirectory()) {
                result.add(child);
            }
        }


        for (Node child : node.getChildren()) {
            if (!child.isDirectory()) {
                result.add(child);
            }
        }

        return result;
    }

    public long getFileSize(String repositoryPath, String filePath) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(gitCommand, "ls-tree", "-l", "HEAD", filePath);
        processBuilder.directory(new File(repositoryPath));
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line = reader.readLine();
            if (line != null) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 4) {
                    try {
                        return Long.parseLong(parts[3]);
                    } catch (NumberFormatException e) {

                    }
                }
            }
        }

        Path path = Paths.get(repositoryPath, filePath);
        if (Files.exists(path)) {
            return Files.size(path);
        }

        throw new IOException("Cannot determine file size");
    }
}
