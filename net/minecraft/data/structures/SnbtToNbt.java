/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.structures;

import com.google.common.collect.Lists;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.structures.NbtToSnbt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class SnbtToNbt
implements DataProvider {
    @Nullable
    private static final Path DUMP_SNBT_TO = null;
    private static final Logger LOGGER = LogUtils.getLogger();
    private final PackOutput output;
    private final Iterable<Path> inputFolders;
    private final List<Filter> filters = Lists.newArrayList();

    public SnbtToNbt(PackOutput packOutput, Iterable<Path> iterable) {
        this.output = packOutput;
        this.inputFolders = iterable;
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
    public void run(CachedOutput cachedOutput) throws IOException {
        Path path3 = this.output.getOutputFolder();
        ArrayList<CompletableFuture> list = Lists.newArrayList();
        for (Path path22 : this.inputFolders) {
            Files.walk(path22, new FileVisitOption[0]).filter(path -> path.toString().endsWith(".snbt")).forEach(path2 -> list.add(CompletableFuture.supplyAsync(() -> this.readStructure((Path)path2, this.getName(path22, (Path)path2)), Util.backgroundExecutor())));
        }
        boolean bl = false;
        for (CompletableFuture completableFuture : list) {
            try {
                this.storeStructureIfChanged(cachedOutput, (TaskResult)completableFuture.get(), path3);
            } catch (Exception exception) {
                LOGGER.error("Failed to process structure", exception);
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
                HashingOutputStream hashingOutputStream = new HashingOutputStream(Hashing.sha1(), byteArrayOutputStream);
                NbtIo.writeCompressed(compoundTag, hashingOutputStream);
                byte[] bs = byteArrayOutputStream.toByteArray();
                HashCode hashCode = hashingOutputStream.hash();
                String string3 = DUMP_SNBT_TO != null ? NbtUtils.structureToSnbt(compoundTag) : null;
                taskResult = new TaskResult(string, bs, string3, hashCode);
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

    private void storeStructureIfChanged(CachedOutput cachedOutput, TaskResult taskResult, Path path) {
        Path path2;
        if (taskResult.snbtPayload != null) {
            path2 = DUMP_SNBT_TO.resolve(taskResult.name + ".snbt");
            try {
                NbtToSnbt.writeSnbt(CachedOutput.NO_CACHE, path2, taskResult.snbtPayload);
            } catch (IOException iOException) {
                LOGGER.error("Couldn't write structure SNBT {} at {}", taskResult.name, path2, iOException);
            }
        }
        path2 = path.resolve(taskResult.name + ".nbt");
        try {
            cachedOutput.writeIfNeeded(path2, taskResult.payload, taskResult.hash);
        } catch (IOException iOException) {
            LOGGER.error("Couldn't write structure {} at {}", taskResult.name, path2, iOException);
        }
    }

    @FunctionalInterface
    public static interface Filter {
        public CompoundTag apply(String var1, CompoundTag var2);
    }

    record TaskResult(String name, byte[] payload, @Nullable String snbtPayload, HashCode hash) {
        @Nullable
        public String snbtPayload() {
            return this.snbtPayload;
        }
    }

    static class StructureConversionException
    extends RuntimeException {
        public StructureConversionException(Path path, Throwable throwable) {
            super(path.toAbsolutePath().toString(), throwable);
        }
    }
}

