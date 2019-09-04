/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.structures;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class NbtToSnbt
implements DataProvider {
    private static final Logger LOGGER = LogManager.getLogger();
    private final DataGenerator generator;

    public NbtToSnbt(DataGenerator dataGenerator) {
        this.generator = dataGenerator;
    }

    @Override
    public void run(HashCache hashCache) throws IOException {
        Path path2 = this.generator.getOutputFolder();
        for (Path path22 : this.generator.getInputFolders()) {
            Files.walk(path22, new FileVisitOption[0]).filter(path -> path.toString().endsWith(".nbt")).forEach(path3 -> NbtToSnbt.convertStructure(path3, this.getName(path22, (Path)path3), path2));
        }
    }

    @Override
    public String getName() {
        return "NBT to SNBT";
    }

    private String getName(Path path, Path path2) {
        String string = path.relativize(path2).toString().replaceAll("\\\\", "/");
        return string.substring(0, string.length() - ".nbt".length());
    }

    @Nullable
    public static Path convertStructure(Path path, String string, Path path2) {
        try {
            CompoundTag compoundTag = NbtIo.readCompressed(Files.newInputStream(path, new OpenOption[0]));
            Component component = compoundTag.getPrettyDisplay("    ", 0);
            String string2 = component.getString() + "\n";
            Path path3 = path2.resolve(string + ".snbt");
            Files.createDirectories(path3.getParent(), new FileAttribute[0]);
            try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path3, new OpenOption[0]);){
                bufferedWriter.write(string2);
            }
            LOGGER.info("Converted {} from NBT to SNBT", (Object)string);
            return path3;
        } catch (IOException iOException) {
            LOGGER.error("Couldn't convert {} from NBT to SNBT at {}", (Object)string, (Object)path, (Object)iOException);
            return null;
        }
    }
}

