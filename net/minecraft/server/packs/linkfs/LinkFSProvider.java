/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.packs.linkfs;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessDeniedException;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.nio.file.ReadOnlyFileSystemException;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.spi.FileSystemProvider;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.server.packs.linkfs.LinkFSPath;
import net.minecraft.server.packs.linkfs.PathContents;
import org.jetbrains.annotations.Nullable;

class LinkFSProvider
extends FileSystemProvider {
    public static final String SCHEME = "x-mc-link";

    LinkFSProvider() {
    }

    @Override
    public String getScheme() {
        return SCHEME;
    }

    @Override
    public FileSystem newFileSystem(URI uRI, Map<String, ?> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileSystem getFileSystem(URI uRI) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path getPath(URI uRI) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> set, FileAttribute<?> ... fileAttributes) throws IOException {
        if (set.contains(StandardOpenOption.CREATE_NEW) || set.contains(StandardOpenOption.CREATE) || set.contains(StandardOpenOption.APPEND) || set.contains(StandardOpenOption.WRITE)) {
            throw new UnsupportedOperationException();
        }
        Path path2 = LinkFSProvider.toLinkPath(path).toAbsolutePath().getTargetPath();
        if (path2 == null) {
            throw new NoSuchFileException(path.toString());
        }
        return Files.newByteChannel(path2, set, fileAttributes);
    }

    @Override
    public DirectoryStream<Path> newDirectoryStream(Path path, final DirectoryStream.Filter<? super Path> filter) throws IOException {
        final PathContents.DirectoryContents directoryContents = LinkFSProvider.toLinkPath(path).toAbsolutePath().getDirectoryContents();
        if (directoryContents == null) {
            throw new NotDirectoryException(path.toString());
        }
        return new DirectoryStream<Path>(){

            @Override
            public Iterator<Path> iterator() {
                return directoryContents.children().values().stream().filter(linkFSPath -> {
                    try {
                        return filter.accept(linkFSPath);
                    } catch (IOException iOException) {
                        throw new DirectoryIteratorException(iOException);
                    }
                }).map(linkFSPath -> linkFSPath).iterator();
            }

            @Override
            public void close() {
            }
        };
    }

    @Override
    public void createDirectory(Path path, FileAttribute<?> ... fileAttributes) {
        throw new ReadOnlyFileSystemException();
    }

    @Override
    public void delete(Path path) {
        throw new ReadOnlyFileSystemException();
    }

    @Override
    public void copy(Path path, Path path2, CopyOption ... copyOptions) {
        throw new ReadOnlyFileSystemException();
    }

    @Override
    public void move(Path path, Path path2, CopyOption ... copyOptions) {
        throw new ReadOnlyFileSystemException();
    }

    @Override
    public boolean isSameFile(Path path, Path path2) {
        return path instanceof LinkFSPath && path2 instanceof LinkFSPath && path.equals(path2);
    }

    @Override
    public boolean isHidden(Path path) {
        return false;
    }

    @Override
    public FileStore getFileStore(Path path) {
        return LinkFSProvider.toLinkPath(path).getFileSystem().store();
    }

    @Override
    public void checkAccess(Path path, AccessMode ... accessModes) throws IOException {
        if (accessModes.length == 0 && !LinkFSProvider.toLinkPath(path).exists()) {
            throw new NoSuchFileException(path.toString());
        }
        block4: for (AccessMode accessMode : accessModes) {
            switch (accessMode) {
                case READ: {
                    if (LinkFSProvider.toLinkPath(path).exists()) continue block4;
                    throw new NoSuchFileException(path.toString());
                }
                case EXECUTE: 
                case WRITE: {
                    throw new AccessDeniedException(accessMode.toString());
                }
            }
        }
    }

    @Override
    @Nullable
    public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> class_, LinkOption ... linkOptions) {
        LinkFSPath linkFSPath = LinkFSProvider.toLinkPath(path);
        if (class_ == BasicFileAttributeView.class) {
            return (V)linkFSPath.getBasicAttributeView();
        }
        return null;
    }

    @Override
    public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> class_, LinkOption ... linkOptions) throws IOException {
        LinkFSPath linkFSPath = LinkFSProvider.toLinkPath(path).toAbsolutePath();
        if (class_ == BasicFileAttributes.class) {
            return (A)linkFSPath.getBasicAttributes();
        }
        throw new UnsupportedOperationException("Attributes of type " + class_.getName() + " not supported");
    }

    @Override
    public Map<String, Object> readAttributes(Path path, String string, LinkOption ... linkOptions) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAttribute(Path path, String string, Object object, LinkOption ... linkOptions) {
        throw new ReadOnlyFileSystemException();
    }

    private static LinkFSPath toLinkPath(@Nullable Path path) {
        if (path == null) {
            throw new NullPointerException();
        }
        if (path instanceof LinkFSPath) {
            LinkFSPath linkFSPath = (LinkFSPath)path;
            return linkFSPath;
        }
        throw new ProviderMismatchException();
    }
}

