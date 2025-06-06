package com.savostov.git_manager.controller;

import com.savostov.git_manager.model.Repo;
import com.savostov.git_manager.repository.RepositoryRepository;
import com.savostov.git_manager.service.FileStructureService;
import com.savostov.git_manager.service.GitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class JsonRepositoryController {

    private final FileStructureService fileStructureService;
    private final GitService gitService;
    private final RepositoryRepository repositoryRepository;

    @Autowired
    public JsonRepositoryController(FileStructureService fileStructureService, 
                                  GitService gitService,
                                  RepositoryRepository repositoryRepository) {
        this.fileStructureService = fileStructureService;
        this.gitService = gitService;
        this.repositoryRepository = repositoryRepository;
    }

    @GetMapping("/repository/{id}/files")
    public List<Map<String, Object>> getFiles(@PathVariable Long id,
                                              @RequestParam(value = "path", required = false, defaultValue = "") String path) {
        return fileStructureService.getFileStructure(id, path);
    }

    @GetMapping("/repository/{id}/fileContent")
    public ResponseEntity<Object> getFileContent(@PathVariable Long id, @RequestParam String path) {
        try {
            Repo repo = repositoryRepository.getReferenceById(id);
            

            String extension = path.substring(path.lastIndexOf(".") + 1).toLowerCase();
            if (isBinaryExtension(extension)) {
                return ResponseEntity.ok(Map.of(
                    "error", true,
                    "message", "Бинарные файлы не поддерживаются для просмотра в браузере",
                    "type", "binary"
                ));
            }


            long fileSize = gitService.getFileSize(repo.getPath(), path);
            

            if (fileSize > 1024 * 1024) {
                return ResponseEntity.ok(Map.of(
                    "error", true,
                    "message", "Файл слишком большой для просмотра в браузере. Максимальный размер: 1MB",
                    "type", "too_large",
                    "size", fileSize
                ));
            }

            String content = gitService.getFileContent(repo.getPath(), path);
            return ResponseEntity.ok(Map.of(
                "error", false,
                "content", content
            ));
        } catch (IOException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", true,
                "message", "Ошибка при чтении файла: " + e.getMessage()
            ));
        }
    }

    private boolean isBinaryExtension(String extension) {
        Set<String> binaryExtensions = Set.of(

            "exe", "dll", "so", "dylib", "bin",
            "zip", "rar", "7z", "tar", "gz", "bz2",
            "jpg", "jpeg", "png", "gif", "bmp", "ico",
            "mp3", "wav", "ogg", "mp4", "avi", "mov",
            "pdf", "doc", "docx", "xls", "xlsx",
            "class", "jar", "war", "ear",
            "pyc", "pyo", "pyd",
            "o", "obj"
        );
        return binaryExtensions.contains(extension);
    }
}
