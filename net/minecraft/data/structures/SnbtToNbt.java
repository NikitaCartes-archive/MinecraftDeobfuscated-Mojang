/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.structures;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.TagParser;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class SnbtToNbt
implements DataProvider {
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
        ArrayList list = Lists.newArrayList();
        for (Path path22 : this.generator.getInputFolders()) {
            Files.walk(path22, new FileVisitOption[0]).filter(path -> path.toString().endsWith(".snbt")).forEach(path2 -> list.add(CompletableFuture.supplyAsync(() -> this.readStructure((Path)path2, this.getName(path22, (Path)path2)), Util.backgroundExecutor())));
        }
        Util.sequence(list).join().stream().filter(Objects::nonNull).forEach(taskResult -> this.storeStructureIfChanged(hashCache, (TaskResult)taskResult, path3));
    }

    @Override
    public String getName() {
        return "SNBT -> NBT";
    }

    private String getName(Path path, Path path2) {
        String string = path.relativize(path2).toString().replaceAll("\\\\", "/");
        return string.substring(0, string.length() - ".snbt".length());
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Nullable
    private TaskResult readStructure(Path path, String string) {
        try (BufferedReader bufferedReader = Files.newBufferedReader(path);){
            String string2 = IOUtils.toString(bufferedReader);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            NbtIo.writeCompressed(this.applyFilters(string, TagParser.parseTag(string2)), byteArrayOutputStream);
            byte[] bs = byteArrayOutputStream.toByteArray();
            String string3 = SHA1.hashBytes(bs).toString();
            TaskResult taskResult = new TaskResult(string, bs, string3);
            return taskResult;
        } catch (CommandSyntaxException commandSyntaxException) {
            LOGGER.error("Couldn't convert {} from SNBT to NBT at {} as it's invalid SNBT", (Object)string, (Object)path, (Object)commandSyntaxException);
            return null;
        } catch (IOException iOException) {
            LOGGER.error("Couldn't convert {} from SNBT to NBT at {}", (Object)string, (Object)path, (Object)iOException);
        }
        return null;
    }

    private void storeStructureIfChanged(HashCache hashCache, TaskResult taskResult, Path path) {
        Path path2 = path.resolve(taskResult.name + ".nbt");
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
        private final String name;
        private final byte[] payload;
        private final String hash;

        public TaskResult(String string, byte[] bs, String string2) {
            this.name = string;
            this.payload = bs;
            this.hash = string2;
        }
    }
}

