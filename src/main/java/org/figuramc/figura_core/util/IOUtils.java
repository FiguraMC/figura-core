package org.figuramc.figura_core.util;

import org.figuramc.figura_core.util.functional.BiThrowingBiFunction;
import org.figuramc.figura_core.util.functional.BiThrowingFunction;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

public class IOUtils {

    public static String stripExtension(String str, String extension) {
        if (str.endsWith("." + extension))
            return str.substring(0, str.length() - extension.length() - 1);
        return str;
    }

    public static @Nullable String getExtension(String str) {
        int idx = str.lastIndexOf('.');
        if (idx == -1) return null;
        return str.substring(idx + 1);
    }

    // Recursively process a directory and the files in it.
    // "process" is called on non-directory files, and "gather" is called with the directory and the results.
    // If the root file does not exist, acts as if it's an empty directory.
    // Ignores "hidden" files (starting with dot, or hidden however your OS defines it)
    public static <T, E extends Throwable> T recursiveProcess(Path root, BiThrowingFunction<Path, @Nullable T, E, IOException> process, BiThrowingBiFunction<Path, LinkedHashMap<String, T>, T, E, IOException> gather) throws E, IOException {
        return recursiveProcess(root, process, gather, null, false);
    }
    public static <T, E extends Throwable> T recursiveProcess(
            Path root,
            BiThrowingFunction<Path, @Nullable T, E, IOException> process,
            BiThrowingBiFunction<Path, LinkedHashMap<String, T>, T, E, IOException> gather,
            @Nullable String extension, // The file extension we're looking for
            boolean stripExtension // Whether to strip the file extension from terminal files
    ) throws E, IOException {
        File f = root.toFile();
        if (f.isDirectory()) {
            // Sort files by name, so iteration order is consistent!
            List<File> files = Arrays.asList(f.listFiles());
            files.sort(Comparator.comparing(File::getName));
            LinkedHashMap<String, T> map = new LinkedHashMap<>();
            for (File child : files) {
                if (child.isHidden()) continue;
                String name = child.getName();
                if (name.startsWith(".")) continue;
                if (extension != null && !child.isDirectory()) {
                    // If the extension is wrong, skip the file
                    if (!extension.equals(IOUtils.getExtension(name))) continue;
                    // If we strip extension, strip it from the name
                    if (stripExtension) name = IOUtils.stripExtension(name, extension);
                }
                map.put(name, recursiveProcess(child.toPath(), process, gather, extension, stripExtension));
            }
            return gather.apply(root, map);
        } else if (f.exists()) {
            return process.apply(root);
        } else return gather.apply(root, new LinkedHashMap<>());
    }

    public static String stringRelativeTo(Path subfile, Path root) {
        subfile = subfile.toAbsolutePath();
        root = root.toAbsolutePath();
        subfile = root.relativize(subfile);
        return subfile.toString().replace(File.separatorChar, '/');
    }

}
