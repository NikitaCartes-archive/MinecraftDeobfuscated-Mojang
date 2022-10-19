package net.minecraft.data.structures;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.mojang.logging.LogUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import org.slf4j.Logger;

public class NbtToSnbt implements DataProvider {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Iterable<Path> inputFolders;
	private final PackOutput output;

	public NbtToSnbt(PackOutput packOutput, Collection<Path> collection) {
		this.inputFolders = collection;
		this.output = packOutput;
	}

	@Override
	public void run(CachedOutput cachedOutput) throws IOException {
		Path path = this.output.getOutputFolder();

		for (Path path2 : this.inputFolders) {
			Files.walk(path2)
				.filter(pathx -> pathx.toString().endsWith(".nbt"))
				.forEach(path3 -> convertStructure(cachedOutput, path3, this.getName(path2, path3), path));
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
	public static Path convertStructure(CachedOutput cachedOutput, Path path, String string, Path path2) {
		try {
			InputStream inputStream = Files.newInputStream(path);

			Path var6;
			try {
				Path path3 = path2.resolve(string + ".snbt");
				writeSnbt(cachedOutput, path3, NbtUtils.structureToSnbt(NbtIo.readCompressed(inputStream)));
				LOGGER.info("Converted {} from NBT to SNBT", string);
				var6 = path3;
			} catch (Throwable var8) {
				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (Throwable var7) {
						var8.addSuppressed(var7);
					}
				}

				throw var8;
			}

			if (inputStream != null) {
				inputStream.close();
			}

			return var6;
		} catch (IOException var9) {
			LOGGER.error("Couldn't convert {} from NBT to SNBT at {}", string, path, var9);
			return null;
		}
	}

	public static void writeSnbt(CachedOutput cachedOutput, Path path, String string) throws IOException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		HashingOutputStream hashingOutputStream = new HashingOutputStream(Hashing.sha1(), byteArrayOutputStream);
		hashingOutputStream.write(string.getBytes(StandardCharsets.UTF_8));
		hashingOutputStream.write(10);
		cachedOutput.writeIfNeeded(path, byteArrayOutputStream.toByteArray(), hashingOutputStream.hash());
	}
}
