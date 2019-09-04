/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import net.minecraft.Util;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionBitmap;
import net.minecraft.world.level.chunk.storage.RegionFileVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class RegionFile
implements AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ByteBuffer PADDING_BUFFER = ByteBuffer.allocateDirect(1);
    private final FileChannel file;
    private final Path externalFileDir;
    private final RegionFileVersion version;
    private final ByteBuffer header = ByteBuffer.allocateDirect(8192);
    private final IntBuffer offsets;
    private final IntBuffer timestamps;
    private final RegionBitmap usedSectors = new RegionBitmap();

    public RegionFile(File file, File file2) throws IOException {
        this(file.toPath(), file2.toPath(), RegionFileVersion.VERSION_DEFLATE);
    }

    public RegionFile(Path path, Path path2, RegionFileVersion regionFileVersion) throws IOException {
        this.version = regionFileVersion;
        if (!Files.isDirectory(path2, new LinkOption[0])) {
            throw new IllegalArgumentException("Expected directory, got " + path2.toAbsolutePath());
        }
        this.externalFileDir = path2;
        this.offsets = this.header.asIntBuffer();
        this.offsets.limit(1024);
        this.header.position(4096);
        this.timestamps = this.header.asIntBuffer();
        this.file = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
        this.usedSectors.force(0, 2);
        this.header.position(0);
        int i = this.file.read(this.header, 0L);
        if (i != -1) {
            if (i != 8192) {
                LOGGER.warn("Region file {} has truncated header: {}", (Object)path, (Object)i);
            }
            for (int j = 0; j < 1024; ++j) {
                int k = this.offsets.get(j);
                if (k == 0) continue;
                int l = RegionFile.getSectorNumber(k);
                int m = RegionFile.getNumSectors(k);
                this.usedSectors.force(l, m);
            }
        }
    }

    private Path getExternalChunkPath(ChunkPos chunkPos) {
        String string = "c." + chunkPos.x + "." + chunkPos.z + ".mcc";
        return this.externalFileDir.resolve(string);
    }

    @Nullable
    public synchronized DataInputStream getChunkDataInputStream(ChunkPos chunkPos) throws IOException {
        int i = this.getOffset(chunkPos);
        if (i == 0) {
            return null;
        }
        int j = RegionFile.getSectorNumber(i);
        int k = RegionFile.getNumSectors(i);
        int l = k * 4096;
        ByteBuffer byteBuffer = ByteBuffer.allocate(l);
        this.file.read(byteBuffer, j * 4096);
        byteBuffer.flip();
        if (byteBuffer.remaining() < 5) {
            LOGGER.error("Chunk {} header is truncated: expected {} but read {}", (Object)chunkPos, (Object)l, (Object)byteBuffer.remaining());
            return null;
        }
        int m = byteBuffer.getInt();
        byte b = byteBuffer.get();
        if (m == 0) {
            LOGGER.warn("Chunk {} is allocated, but stream is missing", (Object)chunkPos);
            return null;
        }
        int n = m - 1;
        if (RegionFile.isExternalStreamChunk(b)) {
            if (n != 0) {
                LOGGER.warn("Chunk has both internal and external streams");
            }
            return this.createExternalChunkInputStream(chunkPos, RegionFile.getExternalChunkVersion(b));
        }
        if (n > byteBuffer.remaining()) {
            LOGGER.error("Chunk {} stream is truncated: expected {} but read {}", (Object)chunkPos, (Object)n, (Object)byteBuffer.remaining());
            return null;
        }
        if (n < 0) {
            LOGGER.error("Declared size {} of chunk {} is negative", (Object)m, (Object)chunkPos);
            return null;
        }
        return this.createChunkInputStream(chunkPos, b, RegionFile.createStream(byteBuffer, n));
    }

    private static boolean isExternalStreamChunk(byte b) {
        return (b & 0x80) != 0;
    }

    private static byte getExternalChunkVersion(byte b) {
        return (byte)(b & 0xFFFFFF7F);
    }

    @Nullable
    private DataInputStream createChunkInputStream(ChunkPos chunkPos, byte b, InputStream inputStream) throws IOException {
        RegionFileVersion regionFileVersion = RegionFileVersion.fromId(b);
        if (regionFileVersion == null) {
            LOGGER.error("Chunk {} has invalid chunk stream version {}", (Object)chunkPos, (Object)b);
            return null;
        }
        return new DataInputStream(new BufferedInputStream(regionFileVersion.wrap(inputStream)));
    }

    @Nullable
    private DataInputStream createExternalChunkInputStream(ChunkPos chunkPos, byte b) throws IOException {
        Path path = this.getExternalChunkPath(chunkPos);
        if (!Files.isRegularFile(path, new LinkOption[0])) {
            LOGGER.error("External chunk path {} is not file", (Object)path);
            return null;
        }
        return this.createChunkInputStream(chunkPos, b, Files.newInputStream(path, new OpenOption[0]));
    }

    private static ByteArrayInputStream createStream(ByteBuffer byteBuffer, int i) {
        return new ByteArrayInputStream(byteBuffer.array(), byteBuffer.position(), i);
    }

    private int packSectorOffset(int i, int j) {
        return i << 8 | j;
    }

    private static int getNumSectors(int i) {
        return i & 0xFF;
    }

    private static int getSectorNumber(int i) {
        return i >> 8;
    }

    private static int sizeToSectors(int i) {
        return (i + 4096 - 1) / 4096;
    }

    public boolean doesChunkExist(ChunkPos chunkPos) {
        int i = this.getOffset(chunkPos);
        if (i == 0) {
            return false;
        }
        int j = RegionFile.getSectorNumber(i);
        int k = RegionFile.getNumSectors(i);
        ByteBuffer byteBuffer = ByteBuffer.allocate(5);
        try {
            this.file.read(byteBuffer, j * 4096);
            byteBuffer.flip();
            if (byteBuffer.remaining() != 5) {
                return false;
            }
            int l = byteBuffer.getInt();
            byte b = byteBuffer.get();
            if (RegionFile.isExternalStreamChunk(b)) {
                if (!RegionFileVersion.isValidVersion(RegionFile.getExternalChunkVersion(b))) {
                    return false;
                }
                if (!Files.isRegularFile(this.getExternalChunkPath(chunkPos), new LinkOption[0])) {
                    return false;
                }
            } else {
                if (!RegionFileVersion.isValidVersion(b)) {
                    return false;
                }
                if (l == 0) {
                    return false;
                }
                int m = l - 1;
                if (m < 0 || m > 4096 * k) {
                    return false;
                }
            }
        } catch (IOException iOException) {
            return false;
        }
        return true;
    }

    public DataOutputStream getChunkDataOutputStream(ChunkPos chunkPos) throws IOException {
        return new DataOutputStream(new BufferedOutputStream(this.version.wrap(new ChunkBuffer(chunkPos))));
    }

    protected synchronized void write(ChunkPos chunkPos, ByteBuffer byteBuffer) throws IOException {
        CommitOp commitOp;
        int o;
        int i = RegionFile.getOffsetIndex(chunkPos);
        int j = this.offsets.get(i);
        int k = RegionFile.getSectorNumber(j);
        int l = RegionFile.getNumSectors(j);
        int m = byteBuffer.remaining();
        int n = RegionFile.sizeToSectors(m);
        if (n >= 256) {
            Path path = this.getExternalChunkPath(chunkPos);
            LOGGER.warn("Saving oversized chunk {} ({} bytes} to external file {}", (Object)chunkPos, (Object)m, (Object)path);
            n = 1;
            o = this.usedSectors.allocate(n);
            commitOp = this.writeToExternalFile(path, byteBuffer);
            ByteBuffer byteBuffer2 = this.createExternalStub();
            this.file.write(byteBuffer2, o * 4096);
        } else {
            o = this.usedSectors.allocate(n);
            commitOp = () -> Files.deleteIfExists(this.getExternalChunkPath(chunkPos));
            this.file.write(byteBuffer, o * 4096);
        }
        int p = (int)(Util.getEpochMillis() / 1000L);
        this.offsets.put(i, this.packSectorOffset(o, n));
        this.timestamps.put(i, p);
        this.writeHeader();
        commitOp.run();
        if (k != 0) {
            this.usedSectors.free(k, l);
        }
    }

    private ByteBuffer createExternalStub() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(5);
        byteBuffer.putInt(1);
        byteBuffer.put((byte)(this.version.getId() | 0x80));
        byteBuffer.flip();
        return byteBuffer;
    }

    private CommitOp writeToExternalFile(Path path, ByteBuffer byteBuffer) throws IOException {
        Path path2 = Files.createTempFile(this.externalFileDir, "tmp", null, new FileAttribute[0]);
        try (FileChannel fileChannel = FileChannel.open(path2, StandardOpenOption.CREATE, StandardOpenOption.WRITE);){
            byteBuffer.position(5);
            fileChannel.write(byteBuffer);
        }
        return () -> Files.move(path2, path, StandardCopyOption.REPLACE_EXISTING);
    }

    private void writeHeader() throws IOException {
        this.header.position(0);
        this.file.write(this.header, 0L);
    }

    private int getOffset(ChunkPos chunkPos) {
        return this.offsets.get(RegionFile.getOffsetIndex(chunkPos));
    }

    public boolean hasChunk(ChunkPos chunkPos) {
        return this.getOffset(chunkPos) != 0;
    }

    private static int getOffsetIndex(ChunkPos chunkPos) {
        return chunkPos.getRegionLocalX() + chunkPos.getRegionLocalZ() * 32;
    }

    @Override
    public void close() throws IOException {
        try {
            this.padToFullSector();
        } finally {
            try {
                this.writeHeader();
            } finally {
                this.file.close();
            }
        }
    }

    private void padToFullSector() throws IOException {
        int j;
        int i = (int)this.file.size();
        if (i != (j = RegionFile.sizeToSectors(i) * 4096)) {
            ByteBuffer byteBuffer = PADDING_BUFFER.duplicate();
            byteBuffer.position(0);
            this.file.write(byteBuffer, j - 1);
        }
    }

    static interface CommitOp {
        public void run() throws IOException;
    }

    class ChunkBuffer
    extends ByteArrayOutputStream {
        private final ChunkPos pos;

        public ChunkBuffer(ChunkPos chunkPos) {
            super(8096);
            super.write(0);
            super.write(0);
            super.write(0);
            super.write(0);
            super.write(RegionFile.this.version.getId());
            this.pos = chunkPos;
        }

        @Override
        public void close() throws IOException {
            ByteBuffer byteBuffer = ByteBuffer.wrap(this.buf, 0, this.count);
            byteBuffer.putInt(0, this.count - 5 + 1);
            RegionFile.this.write(this.pos, byteBuffer);
        }
    }
}

