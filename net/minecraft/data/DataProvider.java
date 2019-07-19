/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.Objects;
import net.minecraft.data.HashCache;

public interface DataProvider {
    public static final HashFunction SHA1 = Hashing.sha1();

    public void run(HashCache var1) throws IOException;

    public String getName();

    public static void save(Gson gson, HashCache hashCache, JsonElement jsonElement, Path path) throws IOException {
        String string = gson.toJson(jsonElement);
        String string2 = SHA1.hashUnencodedChars(string).toString();
        if (!Objects.equals(hashCache.getHash(path), string2) || !Files.exists(path, new LinkOption[0])) {
            Files.createDirectories(path.getParent(), new FileAttribute[0]);
            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, new OpenOption[0]);){
                bufferedWriter.write(string);
            }
        }
        hashCache.putNew(path, string2);
    }
}

