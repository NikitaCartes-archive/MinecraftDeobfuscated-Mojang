/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.JsonElement;
import com.google.gson.stream.JsonWriter;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import java.util.function.ToIntFunction;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.util.GsonHelper;
import org.slf4j.Logger;

public interface DataProvider {
    public static final ToIntFunction<String> FIXED_ORDER_FIELDS = Util.make(new Object2IntOpenHashMap(), object2IntOpenHashMap -> {
        object2IntOpenHashMap.put("type", 0);
        object2IntOpenHashMap.put("parent", 1);
        object2IntOpenHashMap.defaultReturnValue(2);
    });
    public static final Comparator<String> KEY_COMPARATOR = Comparator.comparingInt(FIXED_ORDER_FIELDS).thenComparing(string -> string);
    public static final Logger LOGGER = LogUtils.getLogger();

    public CompletableFuture<?> run(CachedOutput var1);

    public String getName();

    public static CompletableFuture<?> saveStable(CachedOutput cachedOutput, JsonElement jsonElement, Path path) {
        return CompletableFuture.runAsync(() -> {
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                HashingOutputStream hashingOutputStream = new HashingOutputStream(Hashing.sha1(), byteArrayOutputStream);
                try (JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter((OutputStream)hashingOutputStream, StandardCharsets.UTF_8));){
                    jsonWriter.setSerializeNulls(false);
                    jsonWriter.setIndent("  ");
                    GsonHelper.writeValue(jsonWriter, jsonElement, KEY_COMPARATOR);
                }
                cachedOutput.writeIfNeeded(path, byteArrayOutputStream.toByteArray(), hashingOutputStream.hash());
            } catch (IOException iOException) {
                LOGGER.error("Failed to save file to {}", (Object)path, (Object)iOException);
            }
        }, Util.backgroundExecutor());
    }

    @FunctionalInterface
    public static interface Factory<T extends DataProvider> {
        public T create(PackOutput var1);
    }
}

