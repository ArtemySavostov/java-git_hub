package com.savostov.git_manager.service;


import com.savostov.git_manager.model.Repo;
import com.savostov.git_manager.repository.RepositoryRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FileStructureService {
    private final RepositoryRepository repositoryRepository;
    private final GitService gitService;

    public FileStructureService(RepositoryRepository repositoryRepository, GitService gitService) {
        this.repositoryRepository = repositoryRepository;
        this.gitService = gitService;
    }

    public List<Map<String, Object>> getFileStructure(Long repoId, String subPath) {
        Repo repo = repositoryRepository.getReferenceById(repoId);
        Path repoPath = Paths.get(repo.getPath());

        try {
            System.out.println("Getting file structure for path: " + subPath);
            GitService.Node rootNode = gitService.getFileStructure(repoPath.toString());
            List<Map<String, Object>> flatList = flatten(rootNode, repoPath, subPath);
            System.out.println("Flattened list size: " + flatList.size());
            return flatList;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private List<Map<String, Object>> flatten(GitService.Node node, Path repoPath, String subPath) {
        System.out.println("Flattening node: " + node.getName() + " for subPath: " + subPath);
        List<Map<String, Object>> result = new ArrayList<>();

        // Если subPath пустой или "root", возвращаем содержимое корневой директории
        if (subPath == null || subPath.isEmpty() || subPath.equals("root")) {
            for (GitService.Node child : node.getChildren()) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", child.getPath());
                item.put("text", child.getName());
                item.put("type", child.isDirectory() ? "directory" : "file");
                // Если это директория и в ней есть файлы, помечаем что есть дочерние элементы
                if (child.isDirectory() && !child.getChildren().isEmpty()) {
                    item.put("children", true);
                } else {
                    item.put("children", false);
                }
                result.add(item);
            }
            System.out.println("Returning list of size: " + result.size() + " for path: " + subPath);
            return result;
        }

        // Ищем нужную поддиректорию
        GitService.Node targetNode = findNodeByPath(node, subPath);
        if (targetNode != null && targetNode.isDirectory()) {
            for (GitService.Node child : targetNode.getChildren()) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", child.getPath());
                item.put("text", child.getName());
                item.put("type", child.isDirectory() ? "directory" : "file");
                // Если это директория и в ней есть файлы, помечаем что есть дочерние элементы
                if (child.isDirectory() && !child.getChildren().isEmpty()) {
                    item.put("children", true);
                } else {
                    item.put("children", false);
                }
                result.add(item);
            }
        }

        return result;
    }

    private GitService.Node findNodeByPath(GitService.Node root, String path) {
        if (path == null || path.isEmpty()) {
            return root;
        }

        String[] parts = path.split("/");
        GitService.Node current = root;

        for (String part : parts) {
            GitService.Node found = null;
            for (GitService.Node child : current.getChildren()) {
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

    private boolean shouldShowNode(String nodePath, String requestedPath) {
        System.out.println("Checking node: " + nodePath + " for requested path: " + requestedPath);
        
        // Для корневого уровня
        if (requestedPath.isEmpty()) {
            boolean shouldShow = !nodePath.contains("/");
            System.out.println("Root level check for " + nodePath + ": " + shouldShow);
            return shouldShow;
        }

        // Для точного совпадения
        if (nodePath.equals(requestedPath)) {
            System.out.println("Exact match for " + nodePath);
            return true;
        }

        // Для прямых потомков
        if (nodePath.startsWith(requestedPath + "/")) {
            String relativePath = nodePath.substring(requestedPath.length() + 1);
            boolean isDirectChild = !relativePath.contains("/");
            System.out.println("Child check for " + nodePath + ": " + isDirectChild);
            return isDirectChild;
        }

        System.out.println("Node " + nodePath + " will not be shown");
        return false;
    }
}
