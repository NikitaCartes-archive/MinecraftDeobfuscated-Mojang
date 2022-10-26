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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
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
	public CompletableFuture<?> run(CachedOutput cachedOutput) {
		Path path = this.output.getOutputFolder();
		List<CompletableFuture<?>> list = new ArrayList();

		for (Path path2 : this.inputFolders) {
			list.add(
				CompletableFuture.supplyAsync(
						() -> {
							try {
								Stream<Path> stream = Files.walk(path2);

								CompletableFuture var4;
								try {
									var4 = CompletableFuture.allOf(
										(CompletableFuture[])stream.filter(pathxx -> pathxx.toString().endsWith(".nbt"))
											.map(path3 -> CompletableFuture.runAsync(() -> convertStructure(cachedOutput, path3, getName(path2, path3), path), Util.ioPool()))
											.toArray(CompletableFuture[]::new)
									);
								} catch (Throwable var7) {
									if (stream != null) {
										try {
											stream.close();
										} catch (Throwable var6) {
											var7.addSuppressed(var6);
										}
									}

									throw var7;
								}

								if (stream != null) {
									stream.close();
								}

								return var4;
							} catch (IOException var8) {
								LOGGER.error("Failed to read structure input directory", (Throwable)var8);
								return CompletableFuture.completedFuture(null);
							}
						},
						Util.backgroundExecutor()
					)
					.thenCompose(completableFuture -> completableFuture)
			);
		}

		return CompletableFuture.allOf((CompletableFuture[])list.toArray(CompletableFuture[]::new));
	}

	@Override
	public final String getName() {
		return "NBT -> SNBT";
	}

	private static String getName(Path path, Path path2) {
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
