package no.difi.vefa.validator.api;

import java.nio.file.FileSystem;
import java.util.List;

/**
 * An instance representing a source.
 * <p/>
 * Implementations in need of close() method should implement java.io.Closeable.
 */
@Deprecated
public interface SourceInstance {

    /**
     * Access to the filesystem delivered by the instance.
     *
     * @return Filesystem instance.
     */
    FileSystem getFileSystem();

    List<String> getConfigs();

}
