package net.minecraft.data.structures;

import com.mojang.logging.LogUtils;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nullable;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import org.slf4j.Logger;

public class NbtToSnbt implements DataProvider {
	private static final Logger LOGGER = LogUtils.getLogger();
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
			writeSnbt(path2.resolve(string + ".snbt"), NbtUtils.structureToSnbt(NbtIo.readCompressed(Files.newInputStream(path))));
			LOGGER.info("Converted {} from NBT to SNBT", string);
			return path2.resolve(string + ".snbt");
		} catch (IOException var4) {
			LOGGER.error("Couldn't convert {} from NBT to SNBT at {}", string, path, var4);
			return null;
		}
	}

	public static void writeSnbt(Path path, String string) throws IOException {
		Files.createDirectories(path.getParent());
		BufferedWriter bufferedWriter = Files.newBufferedWriter(path);

		try {
			bufferedWriter.write(string);
			bufferedWriter.write(10);
		} catch (Throwable var6) {
			if (bufferedWriter != null) {
				try {
					bufferedWriter.close();
				} catch (Throwable var5) {
					var6.addSuppressed(var5);
				}
			}

			throw var6;
		}

		if (bufferedWriter != null) {
			bufferedWriter.close();
		}
	}
}
