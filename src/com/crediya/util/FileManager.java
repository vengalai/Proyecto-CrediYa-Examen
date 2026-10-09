package com.crediya.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/** Reads and writes the plain text files stored in the data/ folder. */
public final class FileManager {
    private static final Path DATA_DIR = Paths.get("data");
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private FileManager() {}

    /** Adds one line at the end of a file (creates the file if needed). */
    public static void append(String fileName, String line) {
        try {
            Files.createDirectories(DATA_DIR);
            Files.writeString(DATA_DIR.resolve(fileName), line + System.lineSeparator(),
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Warning: could not write to " + fileName + " (" + e.getMessage() + ")");
        }
    }

    /** Replaces the whole file content. */
    public static void overwrite(String fileName, List<String> lines) {
        try {
            Files.createDirectories(DATA_DIR);
            Files.write(DATA_DIR.resolve(fileName), lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Warning: could not write to " + fileName + " (" + e.getMessage() + ")");
        }
    }

    /** Reads every line of a file (empty list if it does not exist). */
    public static List<String> readAll(String fileName) {
        try {
            Path p = DATA_DIR.resolve(fileName);
            return Files.exists(p) ? Files.readAllLines(p, StandardCharsets.UTF_8) : Collections.emptyList();
        } catch (IOException e) {
            System.out.println("Warning: could not read " + fileName + " (" + e.getMessage() + ")");
            return Collections.emptyList();
        }
    }

    /** Audit log: every important action is recorded with a timestamp. */
    public static void log(String action) {
        append("audit_log.txt", "[" + LocalDateTime.now().format(STAMP) + "] " + action);
    }
}
