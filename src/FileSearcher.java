import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Utility class that offers a method to search for a file by name starting at a root directory.
 */
public final class FileSearcher {

    private FileSearcher() {
        throw new AssertionError("No instances");
    }

    /**
     * Recursively searches for the first file that matches {@code targetFileName}, scanning the file
     * system starting at {@code rootDir}. The search stops when the first match is found.
     *
     * @param rootDir        directory to start searching from
     * @param targetFileName file name to search for (case-sensitive)
     * @return optional path to the matching file if found; otherwise {@link Optional#empty()}
     * @throws IOException if an I/O error occurs while traversing the file system
     * @throws NullPointerException if any argument is {@code null}
     */
    public static Optional<Path> findFile(Path rootDir, String targetFileName) throws IOException {
        Objects.requireNonNull(rootDir, "rootDir is required");
        Objects.requireNonNull(targetFileName, "targetFileName is required");

        if (!Files.isDirectory(rootDir)) {
            throw new IllegalArgumentException("rootDir must be an existing directory");
        }

        AtomicReference<Path> result = new AtomicReference<>();

        Files.walkFileTree(rootDir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (attrs.isRegularFile() && file.getFileName().toString().equals(targetFileName)) {
                    result.compareAndSet(null, file);
                    return FileVisitResult.TERMINATE;
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if (result.get() != null) {
                    return FileVisitResult.TERMINATE;
                }
                return FileVisitResult.CONTINUE;
            }
        });

        return Optional.ofNullable(result.get());
    }
}
