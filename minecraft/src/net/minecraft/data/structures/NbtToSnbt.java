package net.minecraft.data.structures;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nullable;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NbtToSnbt implements DataProvider {
	private static final Logger LOGGER = LogManager.getLogger();
	private final DataGenerator generator;

	public NbtToSnbt(DataGenerator dataGenerator) {
		this.generator = dataGenerator;
	}

	@Override
	public void run(HashCache hashCache) throws IOException {
		Path path = this.generator.getOutputFolder();

		for (Path path2 : this.generator.getInputFolders()) {
			Files.walk(path2).filter(pathx -> pathx.toString().endsWith(".nbt")).forEach(path3 -> convertStructure(path3, this.getName(path2, path3), path));
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
			CompoundTag compoundTag = NbtIo.readCompressed(Files.newInputStream(path));
			Component component = compoundTag.getPrettyDisplay("    ", 0);
			String string2 = component.getString() + "\n";
			Path path3 = path2.resolve(string + ".snbt");
			Files.createDirectories(path3.getParent());
			BufferedWriter bufferedWriter = Files.newBufferedWriter(path3);
			Throwable var8 = null;

			try {
				bufferedWriter.write(string2);
			} catch (Throwable var18) {
				var8 = var18;
				throw var18;
			} finally {
				if (bufferedWriter != null) {
					if (var8 != null) {
						try {
							bufferedWriter.close();
						} catch (Throwable var17) {
							var8.addSuppressed(var17);
						}
					} else {
						bufferedWriter.close();
					}
				}
			}

			LOGGER.info("Converted {} from NBT to SNBT", string);
			return path3;
		} catch (IOException var20) {
			LOGGER.error("Couldn't convert {} from NBT to SNBT at {}", string, path, var20);
			return null;
		}
	}
}
