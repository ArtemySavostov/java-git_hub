package com.savostov.git_manager.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class GitService {
    private final String gitCommand = "/usr/bin/git";


    public List<String> getFiles(String repositoryPath) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(gitCommand, "ls-files");
        pb.directory(new File(repositoryPath));
        pb.redirectErrorStream(true);
        Process p = pb.start();
        List<String> files = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                files.add(line);
            }
        }
        p.waitFor();
        return files;
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
        ProcessBuilder processBuilder = new ProcessBuilder(gitCommand, "commit", "-m", "\"" + commitMessage + "\"");
        processBuilder.directory(new File(repositoryPath));
        Process process = processBuilder.start();
        process.waitFor();
    }
}
