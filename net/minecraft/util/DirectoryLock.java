/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import com.google.common.base.Charsets;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;

public class DirectoryLock
implements AutoCloseable {
    public static final String LOCK_FILE = "session.lock";
    private final FileChannel lockFile;
    private final FileLock lock;
    private static final ByteBuffer DUMMY;

    public static DirectoryLock create(Path path) throws IOException {
        Path path2 = path.resolve(LOCK_FILE);
        if (!Files.isDirectory(path, new LinkOption[0])) {
            Files.createDirectories(path, new FileAttribute[0]);
        }
        FileChannel fileChannel = FileChannel.open(path2, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        try {
            fileChannel.write(DUMMY.duplicate());
            fileChannel.force(true);
            FileLock fileLock = fileChannel.tryLock();
            if (fileLock == null) {
                throw LockException.alreadyLocked(path2);
            }
            return new DirectoryLock(fileChannel, fileLock);
        } catch (IOException iOException) {
            try {
                fileChannel.close();
            } catch (IOException iOException2) {
                iOException.addSuppressed(iOException2);
            }
            throw iOException;
        }
    }

    private DirectoryLock(FileChannel fileChannel, FileLock fileLock) {
        this.lockFile = fileChannel;
        this.lock = fileLock;
    }

    @Override
    public void close() throws IOException {
        try {
            if (this.lock.isValid()) {
                this.lock.release();
            }
        } finally {
            if (this.lockFile.isOpen()) {
                this.lockFile.close();
            }
        }
    }

    public boolean isValid() {
        return this.lock.isValid();
    }

    /*
     * Enabled aggressive exception aggregation
     */
    public static boolean isLocked(Path path) throws IOException {
        Path path2 = path.resolve(LOCK_FILE);
        try (FileChannel fileChannel = FileChannel.open(path2, StandardOpenOption.WRITE);){
            boolean bl;
            block15: {
                FileLock fileLock = fileChannel.tryLock();
                try {
                    boolean bl2 = bl = fileLock == null;
                    if (fileLock == null) break block15;
                } catch (Throwable throwable) {
                    if (fileLock != null) {
                        try {
                            fileLock.close();
                        } catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                fileLock.close();
            }
            return bl;
        } catch (AccessDeniedException accessDeniedException) {
            return true;
        } catch (NoSuchFileException noSuchFileException) {
            return false;
        }
    }

    static {
        byte[] bs = "\u2603".getBytes(Charsets.UTF_8);
        DUMMY = ByteBuffer.allocateDirect(bs.length);
        DUMMY.put(bs);
        DUMMY.flip();
    }

    public static class LockException
    extends IOException {
        private LockException(Path path, String string) {
            super(path.toAbsolutePath() + ": " + string);
        }

        public static LockException alreadyLocked(Path path) {
            return new LockException(path, "already locked (possibly by other Minecraft instance?)");
        }
    }
}

