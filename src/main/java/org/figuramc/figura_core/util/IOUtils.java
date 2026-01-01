package org.figuramc.figura_core.util;

import org.figuramc.figura_core.util.data_structures.DataTree;
import org.figuramc.figura_core.util.functional.BiThrowingBiFunction;
import org.figuramc.figura_core.util.functional.BiThrowingFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
    // If "process" returns null, the file will be ignored and treated like it's not there.
    // If the root file does not exist, returns null.
    // Ignores "hidden" files (starting with dot, or hidden however your OS defines it)
    public static <T, E extends Throwable> @Nullable T recursiveProcess(File root, BiThrowingFunction<File, @Nullable T, E, IOException> process, BiThrowingBiFunction<File, LinkedHashMap<String, T>, T, E, IOException> gather, @Nullable Predicate<File> shouldIgnore) throws E, IOException {
        return recursiveProcess(root, process, gather, null, false, shouldIgnore);
    }
    public static <T, E extends Throwable> @Nullable T recursiveProcess(
            File root,
            BiThrowingFunction<File, @Nullable T, E, IOException> process,
            BiThrowingBiFunction<File, LinkedHashMap<String, T>, T, E, IOException> gather,
            @Nullable String extension, // The file extension we're looking for
            boolean stripExtension, // Whether to strip the file extension from terminal files
            @Nullable Predicate<File> shouldIgnore
    ) throws E, IOException {

        // Check if we should ignore this file and return null
        if (root.isHidden()) return null;
        if (root.getName().startsWith(".")) return null;
        if (shouldIgnore != null && shouldIgnore.test(root)) return null;

        if (root.isDirectory()) {
            // Sort files by name, so iteration order is consistent!
            List<File> children = Arrays.asList(root.listFiles());
            children.sort(Comparator.comparing(File::getName));
            LinkedHashMap<String, @NotNull T> map = new LinkedHashMap<>();
            for (File child : children) {
                String childName = child.getName();
                if (extension != null && !child.isDirectory()) {
                    // If the extension is wrong, skip the file
                    if (!extension.equals(IOUtils.getExtension(childName))) continue;
                    // If we strip extension, strip it from the name
                    if (stripExtension) childName = IOUtils.stripExtension(childName, extension);
                }
                T result = recursiveProcess(child, process, gather, extension, stripExtension, shouldIgnore);
                if (result != null) map.put(childName, result);
            }
            return gather.apply(root, map);
        } else if (root.exists()) {
            return process.apply(root);
        }
        return null;
    }

    // Traverse the file structure, processing leaves (non-directory files) into V, and return a DataTree with String keys.
    // If tryProcessingFolders is true, then "process" will treat directories as leaves by default and call process() on them.
    // However, if process() returns null for these directories, then they will be instead recursed into.
    // Unlike recursiveProcess, the initial root is always expected to exist, will not be hidden, and will not be ignored.
    // If the initial root does not exist, an empty data tree will be returned.
    // If the initial root exists but is not a directory, an IllegalArgumentException will be thrown.
    public static <V, E extends Throwable> DataTree<String, V> recursiveProcessToDataTree(
            File root,
            BiThrowingFunction<File, @Nullable V, E, IOException> process,
            @Nullable String extension,
            boolean stripExtension,
            boolean tryProcessingFolders, // Whether to try processing folders before recursing into them
            @Nullable Predicate<File> shouldIgnore
    ) throws IOException, E {
        if (!Files.exists(root.toPath()))
            return new DataTree<>();
        if (!root.isDirectory())
            throw new IllegalArgumentException("Root argument to recursiveProcessToDataTree must be a directory");

        List<File> children = Arrays.asList(root.listFiles());
        children.sort(Comparator.comparing(File::getName));

        DataTree<String, V> output = new DataTree<>();
        for (File child : children) {
            if (child.isHidden()) continue;
            String name = child.getName();
            if (name.startsWith(".")) continue;
            if (shouldIgnore != null && shouldIgnore.test(child)) continue;

            if (child.isDirectory()) {
                // Child is a directory
                if (!tryProcessingFolders) {
                    output.addNode(name, recursiveProcessToDataTree(child, process, extension, stripExtension, tryProcessingFolders, shouldIgnore));
                    continue;
                }
                // Try processing this folder
                V applied = process.apply(child);
                if (applied != null) {
                    output.addLeaf(name, applied);
                    continue;
                }
            }

            // If the extension is wrong, skip the file
            if (extension != null && !extension.equals(IOUtils.getExtension(name))) continue;
            // If we strip extension, strip it from the name
            if (extension != null && stripExtension) name = IOUtils.stripExtension(name, extension);
            // Add leaf if not null
            V applied = process.apply(child);
            if (applied != null) output.addLeaf(name, applied);
        }
        return output;
    }


    public static String stringRelativeTo(File subfile, File root) {
        // Get the path relative to the root
        Path subfilePath = subfile.toPath();
        Path rootPath = root.toPath();
        Path relativePath = rootPath.relativize(subfilePath);
        // Convert to a string
        String asString = relativePath.toString().replace(File.separatorChar, '/');
        // If the subfile is a directory, then ensure there's a / at the end, and vice versa
        if (subfile.isDirectory() && !asString.endsWith("/")) asString += "/";
        else if (!subfile.isDirectory() && asString.endsWith("/")) asString = asString.substring(0, asString.length() - 1);
        // Done
        return asString;
    }

    // Parse a glob pattern into a regex, with additional rules to try to conform to gitignore
    // Tries to follow guidelines from here: https://git-scm.com/docs/gitignore
    // Assumes "/" as the file separator, as is returned from stringRelativeTo()
    // Based mostly on Globs.toRegexPattern()
    // We also include helper functions copy pasted from there
    private static final String regexMetaChars = ".^$+{[]|()";
    private static final String globMetaChars = "\\*?[";
    private static boolean isRegexMeta(char c) { return regexMetaChars.indexOf(c) != -1; }
    private static boolean isGlobMeta(char c) { return globMetaChars.indexOf(c) != -1; }
    private static char EOL = 0;  //TBD
    private static char next(String glob, int i) {
        if (i < glob.length()) {
            return glob.charAt(i);
        }
        return EOL;
    }

    // Accepts a line from a .figignore file
    // If this line contains a pattern, return an IgnorePattern
    // If the line does not contain a pattern (blank or comment) then return null
    // The pattern should test against a string returned from IOUtils.stringRelativeTo() function
    public record IgnorePattern(Pattern regex, boolean negated) {}
    public static @Nullable IgnorePattern parseGitIgnorePattern(String line) throws PatternSyntaxException {
        // Totally blank lines do not match anything
        if (line.isBlank()) return null;
        // Comments do not match anything
        if (line.charAt(0) == '#') return null;

        // Strip ending whitespace unless escaped
        String globPattern;
        int lastBackslash = line.lastIndexOf('\\');
        if (lastBackslash != -1 && line.substring(lastBackslash + 1).isBlank()) {
            globPattern = line.substring(0, lastBackslash + 2);
        } else {
             globPattern = line.stripTrailing();
        }

        // Start with ^ so matching starts at the beginning of the input
        StringBuilder regex = new StringBuilder("^");

        // If the pattern has a slash at the beginning or in the middle, it's relative.
        int firstSlash = globPattern.indexOf('/');
        boolean slashAtBeginningOrMiddle = firstSlash >= 0 && firstSlash != globPattern.length() - 1;
        // Otherwise, it can begin matching at any depth, so add an unlimited number of levels at the start
        if (!slashAtBeginningOrMiddle) {
            regex.append("([^/]*/)*");
            // If it starts with **/, then remove that since it's already been handled
            if (globPattern.startsWith("**/"))
                globPattern = globPattern.substring(3);
        }

        int i = 0;
//        boolean inGroup = false;
        boolean negated = false;

        // An exclamation mark as the first character can negate a pattern
        if (globPattern.charAt(i) == '!') {
            negated = true;
            i++;
        }

        while (i < globPattern.length()) {
            char c = globPattern.charAt(i++);
            switch (c) {
                case '\\' -> {
                    // Escape special characters
                    if (i == globPattern.length())
                        throw new PatternSyntaxException("No character to escape", globPattern, i - 1);
                    char next = globPattern.charAt(i++);
                    if (isGlobMeta(next) || isRegexMeta(next))
                        regex.append('\\');
                    regex.append(next);
                }
                case '/' -> {
                    if (next(globPattern, i) == '*') {
                        if (next(globPattern, i+1) == EOL) {
                            // We have /* ending the matcher, so instead of having * expand to [^/]*, expand to [^/]+ to require at least 1 item
                            regex.append("/[^/]+");
                            i += 2;
                        } else if (next(globPattern, i+1) == '*') {
                            if (next(globPattern, i+2) == '/') {
                                // Special "/**/" syntax matches any number of directories deep
                                regex.append("/.*/");
                                i += 3;
                            } else if (next(globPattern, i+2) == EOL) {
                                // The pattern ends with "/**, so we can match anything
                                regex.append("/.*");
                                i += 3;
                            } else {
                                // Normal slash
                                regex.append('/');
                            }
                        } else {
                            // Normal slash
                            regex.append('/');
                        }
                    } else {
                        // Normal slash
                        regex.append('/');
                    }
                }
                case '[' -> {
                    regex.append("[[^/]&&[");
                    if (next(globPattern, i) == '^') {
                        regex.append("\\^");
                        i++;
                    } else {
                        if (next(globPattern, i) == '!') {
                            regex.append('^');
                            i++;
                        }
                        if (next(globPattern, i) == '-') {
                            regex.append("-");
                            i++;
                        }
                    }
                    boolean hasRangeStart = false;
                    char last = 0;
                    while (i < globPattern.length()) {
                        c = globPattern.charAt(i++);
                        if (c == ']') break;
                        if (c == '/') throw new PatternSyntaxException("Explicit name separator in class", globPattern, i - 1);
                        if (c == '\\' || c == '[' || c == '&' && next(globPattern, i) == '&') regex.append('\\');
                        regex.append(c);

                        if (c == '-') {
                            if (!hasRangeStart) throw new PatternSyntaxException("Invalid range", globPattern, i - 1);
                            if ((c = next(globPattern, i++)) != EOL || c == ']') break;
                            if (c < last) throw new PatternSyntaxException("Invalid range", globPattern, i - 3);
                            regex.append(c);
                            hasRangeStart = false;
                        } else {
                            hasRangeStart = true;
                            last = c;
                        }
                    }
                    if (c != ']') throw new PatternSyntaxException("Missing ']'", globPattern, i - 1);
                    regex.append("]]");
                }
                // Curly brace glob groups are not supported by gitignore
//                case '{' -> {
//                    if (inGroup) throw new PatternSyntaxException("Cannot nest groups", globPattern, i - 1);
//                    regex.append("(?:(?:");
//                    inGroup = true;
//                }
//                case '}' -> {
//                    if (inGroup) {
//                        regex.append("))");
//                        inGroup = false;
//                    } else {
//                        regex.append('}');
//                    }
//                }
//                case ',' -> {
//                    if (inGroup) regex.append(")|(?:");
//                    else regex.append(',');
//                }
                // Double stars are not special except when leading or trailing, or between two slashes
                case '*' -> regex.append("[^/]*");
                case '?' -> regex.append("[^/]");
                default -> {
                    if (isRegexMeta(c))
                        regex.append('\\');
                    regex.append(c);
                }
            }
        }

        // If the pattern didn't end with a slash, emit an optional slash, so it can match files or directories
        if (globPattern.charAt(globPattern.length() - 1) != '/') {
            regex.append("/?");
        }

        // End with $ to fully match the string
        regex.append('$');

        return new IgnorePattern(
                Pattern.compile(regex.toString(), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE),
                negated
        );
    }

}
