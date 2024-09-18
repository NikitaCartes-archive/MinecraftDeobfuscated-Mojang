package com.mojang.realmsclient.client.worldupload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.BooleanSupplier;
import java.util.zip.GZIPOutputStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

@Environment(EnvType.CLIENT)
public class RealmsUploadWorldPacker {
	private static final long SIZE_LIMIT = 5368709120L;
	private static final String WORLD_FOLDER_NAME = "world";
	private final BooleanSupplier isCanceled;
	private final Path directoryToPack;

	public static File pack(Path path, BooleanSupplier booleanSupplier) throws IOException {
		return new RealmsUploadWorldPacker(path, booleanSupplier).tarGzipArchive();
	}

	private RealmsUploadWorldPacker(Path path, BooleanSupplier booleanSupplier) {
		this.isCanceled = booleanSupplier;
		this.directoryToPack = path;
	}

	private File tarGzipArchive() throws IOException {
		TarArchiveOutputStream tarArchiveOutputStream = null;

		File var3;
		try {
			File file = File.createTempFile("realms-upload-file", ".tar.gz");
			tarArchiveOutputStream = new TarArchiveOutputStream(new GZIPOutputStream(new FileOutputStream(file)));
			tarArchiveOutputStream.setLongFileMode(3);
			this.addFileToTarGz(tarArchiveOutputStream, this.directoryToPack, "world", true);
			if (this.isCanceled.getAsBoolean()) {
				throw new RealmsUploadCanceledException();
			}

			tarArchiveOutputStream.finish();
			this.verifyBelowSizeLimit(file.length());
			var3 = file;
		} finally {
			if (tarArchiveOutputStream != null) {
				tarArchiveOutputStream.close();
			}
		}

		return var3;
	}

	private void addFileToTarGz(TarArchiveOutputStream tarArchiveOutputStream, Path path, String string, boolean bl) throws IOException {
		if (this.isCanceled.getAsBoolean()) {
			throw new RealmsUploadCanceledException();
		} else {
			this.verifyBelowSizeLimit(tarArchiveOutputStream.getBytesWritten());
			File file = path.toFile();
			String string2 = bl ? string : string + file.getName();
			TarArchiveEntry tarArchiveEntry = new TarArchiveEntry(file, string2);
			tarArchiveOutputStream.putArchiveEntry(tarArchiveEntry);
			if (file.isFile()) {
				InputStream inputStream = new FileInputStream(file);

				try {
					inputStream.transferTo(tarArchiveOutputStream);
				} catch (Throwable var14) {
					try {
						inputStream.close();
					} catch (Throwable var13) {
						var14.addSuppressed(var13);
					}

					throw var14;
				}

				inputStream.close();
				tarArchiveOutputStream.closeArchiveEntry();
			} else {
				tarArchiveOutputStream.closeArchiveEntry();
				File[] files = file.listFiles();
				if (files != null) {
					for (File file2 : files) {
						this.addFileToTarGz(tarArchiveOutputStream, file2.toPath(), string2 + "/", false);
					}
				}
			}
		}
	}

	private void verifyBelowSizeLimit(long l) {
		if (l > 5368709120L) {
			throw new RealmsUploadTooLargeException(5368709120L);
		}
	}
}
