package com.hidngemini.langlocked.utils;

import java.io.File;
import java.net.URI;
import java.nio.file.*;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Stream;

public class BundledModelExtractor {

    public static File extractModelIfNeeded(String resourcePath, File targetDir) {
        if (!targetDir.exists()) {
            targetDir.mkdirs();
            try {
                URI uri = BundledModelExtractor.class.getResource(resourcePath).toURI();
                Path myPath;
                if (uri.getScheme().equals("jar")) {
                    try (FileSystem fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
                        myPath = fileSystem.getPath(resourcePath);
                        copyDirectory(myPath, targetDir.toPath());
                    }
                } else {
                    myPath = Paths.get(uri);
                    copyDirectory(myPath, targetDir.toPath());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return targetDir;
    }

    private static void copyDirectory(Path source, Path target) throws java.io.IOException {
        try (Stream<Path> paths = Files.walk(source)) {
            for (Iterator<Path> it = paths.iterator(); it.hasNext(); ) {
                Path path = it.next();
                Path dest = target.resolve(source.relativize(path).toString());
                if (Files.isDirectory(path)) {
                    Files.createDirectories(dest);
                } else {
                    try {
                        Files.copy(path, dest, StandardCopyOption.REPLACE_EXISTING);
                    } catch (Exception ignored) {}
                }
            }
        }
    }
}