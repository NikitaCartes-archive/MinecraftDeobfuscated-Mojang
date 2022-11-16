/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.eventlog;

import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class EventLogDirectory {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int COMPRESS_BUFFER_SIZE = 4096;
    private static final String COMPRESSED_EXTENSION = ".gz";
    private final Path root;
    private final String extension;

    private EventLogDirectory(Path path, String string) {
        this.root = path;
        this.extension = string;
    }

    public static EventLogDirectory open(Path path, String string) throws IOException {
        Files.createDirectories(path, new FileAttribute[0]);
        return new EventLogDirectory(path, string);
    }

    public FileList listFiles() throws IOException {
        try (Stream<Path> stream = Files.list(this.root);){
            FileList fileList = new FileList(stream.filter(path -> Files.isRegularFile(path, new LinkOption[0])).map(this::parseFile).filter(Objects::nonNull).toList());
            return fileList;
        }
    }

    @Nullable
    private File parseFile(Path path) {
        String string = path.getFileName().toString();
        int i = string.indexOf(46);
        if (i == -1) {
            return null;
        }
        FileId fileId = FileId.parse(string.substring(0, i));
        if (fileId != null) {
            String string2 = string.substring(i);
            if (string2.equals(this.extension)) {
                return new RawFile(path, fileId);
            }
            if (string2.equals(this.extension + COMPRESSED_EXTENSION)) {
                return new CompressedFile(path, fileId);
            }
        }
        return null;
    }

    static void tryCompress(Path path, Path path2) throws IOException {
        if (Files.exists(path2, new LinkOption[0])) {
            throw new IOException("Compressed target file already exists: " + path2);
        }
        try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.WRITE, StandardOpenOption.READ);){
            FileLock fileLock = fileChannel.tryLock();
            if (fileLock == null) {
                throw new IOException("Raw log file is already locked, cannot compress: " + path);
            }
            EventLogDirectory.writeCompressed(fileChannel, path2);
            fileChannel.truncate(0L);
        }
        Files.delete(path);
    }

    private static void writeCompressed(ReadableByteChannel readableByteChannel, Path path) throws IOException {
        try (GZIPOutputStream outputStream = new GZIPOutputStream(Files.newOutputStream(path, new OpenOption[0]));){
            byte[] bs = new byte[4096];
            ByteBuffer byteBuffer = ByteBuffer.wrap(bs);
            while (readableByteChannel.read(byteBuffer) >= 0) {
                byteBuffer.flip();
                ((OutputStream)outputStream).write(bs, 0, byteBuffer.limit());
                byteBuffer.clear();
            }
        }
    }

    public RawFile createNewFile(LocalDate localDate) throws IOException {
        FileId fileId;
        int i = 1;
        Set<FileId> set = this.listFiles().ids();
        while (set.contains(fileId = new FileId(localDate, i++))) {
        }
        RawFile rawFile = new RawFile(this.root.resolve(fileId.toFileName(this.extension)), fileId);
        Files.createFile(rawFile.path(), new FileAttribute[0]);
        return rawFile;
    }

    public static class FileList
    implements Iterable<File> {
        private final List<File> files;

        FileList(List<File> list) {
            this.files = new ArrayList<File>(list);
        }

        public FileList prune(LocalDate localDate, int i) {
            this.files.removeIf(file -> {
                FileId fileId = file.id();
                LocalDate localDate2 = fileId.date().plusDays(i);
                if (!localDate.isBefore(localDate2)) {
                    try {
                        Files.delete(file.path());
                        return true;
                    } catch (IOException iOException) {
                        LOGGER.warn("Failed to delete expired event log file: {}", (Object)file.path(), (Object)iOException);
                    }
                }
                return false;
            });
            return this;
        }

        public FileList compressAll() {
            ListIterator<File> listIterator = this.files.listIterator();
            while (listIterator.hasNext()) {
                File file = listIterator.next();
                try {
                    listIterator.set(file.compress());
                } catch (IOException iOException) {
                    LOGGER.warn("Failed to compress event log file: {}", (Object)file.path(), (Object)iOException);
                }
            }
            return this;
        }

        @Override
        public Iterator<File> iterator() {
            return this.files.iterator();
        }

        public Stream<File> stream() {
            return this.files.stream();
        }

        public Set<FileId> ids() {
            return this.files.stream().map(File::id).collect(Collectors.toSet());
        }
    }

    public record FileId(LocalDate date, int index) {
        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

        @Nullable
        public static FileId parse(String string) {
            int i = string.indexOf("-");
            if (i == -1) {
                return null;
            }
            String string2 = string.substring(0, i);
            String string3 = string.substring(i + 1);
            try {
                return new FileId(LocalDate.parse(string2, DATE_FORMATTER), Integer.parseInt(string3));
            } catch (NumberFormatException | DateTimeParseException runtimeException) {
                return null;
            }
        }

        @Override
        public String toString() {
            return DATE_FORMATTER.format(this.date) + "-" + this.index;
        }

        public String toFileName(String string) {
            return this + string;
        }
    }

    public record RawFile(Path path, FileId id) implements File
    {
        public FileChannel openChannel() throws IOException {
            return FileChannel.open(this.path, StandardOpenOption.WRITE, StandardOpenOption.READ);
        }

        @Override
        @Nullable
        public Reader openReader() throws IOException {
            return Files.exists(this.path, new LinkOption[0]) ? Files.newBufferedReader(this.path) : null;
        }

        @Override
        public CompressedFile compress() throws IOException {
            Path path = this.path.resolveSibling(this.path.getFileName().toString() + EventLogDirectory.COMPRESSED_EXTENSION);
            EventLogDirectory.tryCompress(this.path, path);
            return new CompressedFile(path, this.id);
        }
    }

    public record CompressedFile(Path path, FileId id) implements File
    {
        @Override
        @Nullable
        public Reader openReader() throws IOException {
            if (!Files.exists(this.path, new LinkOption[0])) {
                return null;
            }
            return new BufferedReader(new InputStreamReader(new GZIPInputStream(Files.newInputStream(this.path, new OpenOption[0]))));
        }

        @Override
        public CompressedFile compress() {
            return this;
        }
    }

    public static interface File {
        public Path path();

        public FileId id();

        @Nullable
        public Reader openReader() throws IOException;

        public CompressedFile compress() throws IOException;
    }
}

