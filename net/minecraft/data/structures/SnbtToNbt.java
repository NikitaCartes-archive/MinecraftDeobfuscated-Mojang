/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.structures;

import com.google.common.collect.Lists;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.minecraft.Util;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.data.structures.NbtToSnbt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class SnbtToNbt
implements DataProvider {
    @Nullable
    private static final Path DUMP_SNBT_TO = null;
    private static final Logger LOGGER = LogManager.getLogger();
    private final DataGenerator generator;
    private final List<Filter> filters = Lists.newArrayList();

    public SnbtToNbt(DataGenerator dataGenerator) {
        this.generator = dataGenerator;
    }

    public SnbtToNbt addFilter(Filter filter) {
        this.filters.add(filter);
        return this;
    }

    private CompoundTag applyFilters(String string, CompoundTag compoundTag) {
        CompoundTag compoundTag2 = compoundTag;
        for (Filter filter : this.filters) {
            compoundTag2 = filter.apply(string, compoundTag2);
        }
        return compoundTag2;
    }

    @Override
    public void run(HashCache hashCache) throws IOException {
        Path path3 = this.generator.getOutputFolder();
        ArrayList<CompletableFuture> list = Lists.newArrayList();
        for (Path path22 : this.generator.getInputFolders()) {
            Files.walk(path22, new FileVisitOption[0]).filter(path -> path.toString().endsWith(".snbt")).forEach(path2 -> list.add(CompletableFuture.supplyAsync(() -> this.readStructure((Path)path2, this.getName(path22, (Path)path2)), Util.backgroundExecutor())));
        }
        boolean bl = false;
        for (CompletableFuture completableFuture : list) {
            try {
                this.storeStructureIfChanged(hashCache, (TaskResult)completableFuture.get(), path3);
            } catch (Exception exception) {
                LOGGER.error("Failed to process structure", (Throwable)exception);
                bl = true;
            }
        }
        if (bl) {
            throw new IllegalStateException("Failed to convert all structures, aborting");
        }
    }

    @Override
    public String getName() {
        return "SNBT -> NBT";
    }

    private String getName(Path path, Path path2) {
        String string = path.relativize(path2).toString().replaceAll("\\\\", "/");
        return string.substring(0, string.length() - ".snbt".length());
    }

    private TaskResult readStructure(Path path, String string) {
        TaskResult taskResult;
        block8: {
            BufferedReader bufferedReader = Files.newBufferedReader(path);
            try {
                String string2 = IOUtils.toString(bufferedReader);
                CompoundTag compoundTag = this.applyFilters(string, NbtUtils.snbtToStructure(string2));
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                NbtIo.writeCompressed(compoundTag, byteArrayOutputStream);
                byte[] bs = byteArrayOutputStream.toByteArray();
                String string3 = SHA1.hashBytes(bs).toString();
                String string4 = DUMP_SNBT_TO != null ? NbtUtils.structureToSnbt(compoundTag) : null;
                taskResult = new TaskResult(string, bs, string4, string3);
                if (bufferedReader == null) break block8;
            } catch (Throwable throwable) {
                try {
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                } catch (Throwable throwable3) {
                    throw new StructureConversionException(path, throwable3);
                }
            }
            bufferedReader.close();
        }
        return taskResult;
    }

    private void storeStructureIfChanged(HashCache hashCache, TaskResult taskResult, Path path) {
        Path path2;
        if (taskResult.snbtPayload != null) {
            path2 = DUMP_SNBT_TO.resolve(taskResult.name + ".snbt");
            try {
                NbtToSnbt.writeSnbt(path2, taskResult.snbtPayload);
            } catch (IOException iOException) {
                LOGGER.error("Couldn't write structure SNBT {} at {}", (Object)taskResult.name, (Object)path2, (Object)iOException);
            }
        }
        path2 = path.resolve(taskResult.name + ".nbt");
        try {
            if (!Objects.equals(hashCache.getHash(path2), taskResult.hash) || !Files.exists(path2, new LinkOption[0])) {
                Files.createDirectories(path2.getParent(), new FileAttribute[0]);
                try (OutputStream outputStream = Files.newOutputStream(path2, new OpenOption[0]);){
                    outputStream.write(taskResult.payload);
                }
            }
            hashCache.putNew(path2, taskResult.hash);
        } catch (IOException iOException) {
            LOGGER.error("Couldn't write structure {} at {}", (Object)taskResult.name, (Object)path2, (Object)iOException);
        }
    }

    @FunctionalInterface
    public static interface Filter {
        public CompoundTag apply(String var1, CompoundTag var2);
    }

    static class TaskResult {
        final String name;
        final byte[] payload;
        @Nullable
        final String snbtPayload;
        final String hash;

        public TaskResult(String string, byte[] bs, @Nullable String string2, String string3) {
            this.name = string;
            this.payload = bs;
            this.snbtPayload = string2;
            this.hash = string3;
        }
    }

    static class StructureConversionException
    extends RuntimeException {
        public StructureConversionException(Path path, Throwable throwable) {
            super(path.toAbsolutePath().toString(), throwable);
        }
    }
}

