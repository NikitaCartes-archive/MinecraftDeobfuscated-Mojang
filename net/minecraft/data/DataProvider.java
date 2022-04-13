/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.data.CachedOutput;

public interface DataProvider {
    public static final HashFunction SHA1 = Hashing.sha1();

    public void run(CachedOutput var1) throws IOException;

    public String getName();

    public static void save(Gson gson, CachedOutput cachedOutput, JsonElement jsonElement, Path path) throws IOException {
        String string = gson.toJson(jsonElement);
        cachedOutput.writeIfNeeded(path, string);
    }
}

