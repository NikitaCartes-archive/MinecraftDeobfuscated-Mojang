package com.mojang.realmsclient.client;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.mojang.realmsclient.dto.WorldDownload;
import com.mojang.realmsclient.exception.RealmsDefaultUncaughtExceptionHandler;
import com.mojang.realmsclient.gui.screens.RealmsDownloadLatestWorldScreen;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Environment(EnvType.CLIENT)
public class FileDownload {
	private static final Logger LOGGER = LogManager.getLogger();
	private volatile boolean cancelled;
	private volatile boolean finished;
	private volatile boolean error;
	private volatile boolean extracting;
	private volatile File tempFile;
	private volatile File resourcePackPath;
	private volatile HttpGet request;
	private Thread currentThread;
	private final RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(120000).setConnectTimeout(120000).build();
	private static final String[] INVALID_FILE_NAMES = new String[]{
		"CON",
		"COM",
		"PRN",
		"AUX",
		"CLOCK$",
		"NUL",
		"COM1",
		"COM2",
		"COM3",
		"COM4",
		"COM5",
		"COM6",
		"COM7",
		"COM8",
		"COM9",
		"LPT1",
		"LPT2",
		"LPT3",
		"LPT4",
		"LPT5",
		"LPT6",
		"LPT7",
		"LPT8",
		"LPT9"
	};

	public long contentLength(String string) {
		CloseableHttpClient closeableHttpClient = null;
		HttpGet httpGet = null;

		long var5;
		try {
			httpGet = new HttpGet(string);
			closeableHttpClient = HttpClientBuilder.create().setDefaultRequestConfig(this.requestConfig).build();
			CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpGet);
			return Long.parseLong(closeableHttpResponse.getFirstHeader("Content-Length").getValue());
		} catch (Throwable var16) {
			LOGGER.error("Unable to get content length for download");
			var5 = 0L;
		} finally {
			if (httpGet != null) {
				httpGet.releaseConnection();
			}

			if (closeableHttpClient != null) {
				try {
					closeableHttpClient.close();
				} catch (IOException var15) {
					LOGGER.error("Could not close http client", (Throwable)var15);
				}
			}
		}

		return var5;
	}

	public void download(
		WorldDownload worldDownload, String string, RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus, LevelStorageSource levelStorageSource
	) {
		if (this.currentThread == null) {
			this.currentThread = new Thread(
				() -> {
					CloseableHttpClient closeableHttpClient = null;

					try {
						this.tempFile = File.createTempFile("backup", ".tar.gz");
						this.request = new HttpGet(worldDownload.downloadLink);
						closeableHttpClient = HttpClientBuilder.create().setDefaultRequestConfig(this.requestConfig).build();
						HttpResponse httpResponse = closeableHttpClient.execute(this.request);
						downloadStatus.totalBytes = Long.parseLong(httpResponse.getFirstHeader("Content-Length").getValue());
						if (httpResponse.getStatusLine().getStatusCode() == 200) {
							OutputStream outputStream2 = new FileOutputStream(this.tempFile);
							FileDownload.ProgressListener progressListener = new FileDownload.ProgressListener(string.trim(), this.tempFile, levelStorageSource, downloadStatus);
							FileDownload.DownloadCountingOutputStream downloadCountingOutputStream2 = new FileDownload.DownloadCountingOutputStream(outputStream2);
							downloadCountingOutputStream2.setListener(progressListener);
							IOUtils.copy(httpResponse.getEntity().getContent(), downloadCountingOutputStream2);
							return;
						}

						this.error = true;
						this.request.abort();
					} catch (Exception var93) {
						LOGGER.error("Caught exception while downloading: " + var93.getMessage());
						this.error = true;
						return;
					} finally {
						this.request.releaseConnection();
						if (this.tempFile != null) {
							this.tempFile.delete();
						}

						if (!this.error) {
							if (!worldDownload.resourcePackUrl.isEmpty() && !worldDownload.resourcePackHash.isEmpty()) {
								try {
									this.tempFile = File.createTempFile("resources", ".tar.gz");
									this.request = new HttpGet(worldDownload.resourcePackUrl);
									HttpResponse httpResponse3 = closeableHttpClient.execute(this.request);
									downloadStatus.totalBytes = Long.parseLong(httpResponse3.getFirstHeader("Content-Length").getValue());
									if (httpResponse3.getStatusLine().getStatusCode() != 200) {
										this.error = true;
										this.request.abort();
										return;
									}

									OutputStream outputStream3 = new FileOutputStream(this.tempFile);
									FileDownload.ResourcePackProgressListener resourcePackProgressListener3 = new FileDownload.ResourcePackProgressListener(
										this.tempFile, downloadStatus, worldDownload
									);
									FileDownload.DownloadCountingOutputStream downloadCountingOutputStream3 = new FileDownload.DownloadCountingOutputStream(outputStream3);
									downloadCountingOutputStream3.setListener(resourcePackProgressListener3);
									IOUtils.copy(httpResponse3.getEntity().getContent(), downloadCountingOutputStream3);
								} catch (Exception var91) {
									LOGGER.error("Caught exception while downloading: " + var91.getMessage());
									this.error = true;
								} finally {
									this.request.releaseConnection();
									if (this.tempFile != null) {
										this.tempFile.delete();
									}
								}
							} else {
								this.finished = true;
							}
						}

						if (closeableHttpClient != null) {
							try {
								closeableHttpClient.close();
							} catch (IOException var90) {
								LOGGER.error("Failed to close Realms download client");
							}
						}
					}
				}
			);
			this.currentThread.setUncaughtExceptionHandler(new RealmsDefaultUncaughtExceptionHandler(LOGGER));
			this.currentThread.start();
		}
	}

	public void cancel() {
		if (this.request != null) {
			this.request.abort();
		}

		if (this.tempFile != null) {
			this.tempFile.delete();
		}

		this.cancelled = true;
	}

	public boolean isFinished() {
		return this.finished;
	}

	public boolean isError() {
		return this.error;
	}

	public boolean isExtracting() {
		return this.extracting;
	}

	public static String findAvailableFolderName(String string) {
		string = string.replaceAll("[\\./\"]", "_");

		for (String string2 : INVALID_FILE_NAMES) {
			if (string.equalsIgnoreCase(string2)) {
				string = "_" + string + "_";
			}
		}

		return string;
	}

	private void untarGzipArchive(String string, File file, LevelStorageSource levelStorageSource) throws IOException {
		Pattern pattern = Pattern.compile(".*-([0-9]+)$");
		int i = 1;

		for (char c : SharedConstants.ILLEGAL_FILE_CHARACTERS) {
			string = string.replace(c, '_');
		}

		if (StringUtils.isEmpty(string)) {
			string = "Realm";
		}

		string = findAvailableFolderName(string);

		try {
			for (LevelSummary levelSummary : levelStorageSource.getLevelList()) {
				if (levelSummary.getLevelId().toLowerCase(Locale.ROOT).startsWith(string.toLowerCase(Locale.ROOT))) {
					Matcher matcher = pattern.matcher(levelSummary.getLevelId());
					if (matcher.matches()) {
						if (Integer.valueOf(matcher.group(1)) > i) {
							i = Integer.valueOf(matcher.group(1));
						}
					} else {
						i++;
					}
				}
			}
		} catch (Exception var128) {
			LOGGER.error("Error getting level list", (Throwable)var128);
			this.error = true;
			return;
		}

		String string2;
		if (levelStorageSource.isNewLevelIdAcceptable(string) && i <= 1) {
			string2 = string;
		} else {
			string2 = string + (i == 1 ? "" : "-" + i);
			if (!levelStorageSource.isNewLevelIdAcceptable(string2)) {
				boolean bl = false;

				while (!bl) {
					i++;
					string2 = string + (i == 1 ? "" : "-" + i);
					if (levelStorageSource.isNewLevelIdAcceptable(string2)) {
						bl = true;
					}
				}
			}
		}

		TarArchiveInputStream tarArchiveInputStream = null;
		File file2 = new File(Minecraft.getInstance().gameDirectory.getAbsolutePath(), "saves");

		try {
			file2.mkdir();
			tarArchiveInputStream = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(file))));

			for (TarArchiveEntry tarArchiveEntry = tarArchiveInputStream.getNextTarEntry();
				tarArchiveEntry != null;
				tarArchiveEntry = tarArchiveInputStream.getNextTarEntry()
			) {
				File file3 = new File(file2, tarArchiveEntry.getName().replace("world", string2));
				if (tarArchiveEntry.isDirectory()) {
					file3.mkdirs();
				} else {
					file3.createNewFile();
					FileOutputStream fileOutputStream = new FileOutputStream(file3);
					Throwable var12 = null;

					try {
						IOUtils.copy(tarArchiveInputStream, fileOutputStream);
					} catch (Throwable var122) {
						var12 = var122;
						throw var122;
					} finally {
						if (fileOutputStream != null) {
							if (var12 != null) {
								try {
									fileOutputStream.close();
								} catch (Throwable var121) {
									var12.addSuppressed(var121);
								}
							} else {
								fileOutputStream.close();
							}
						}
					}
				}
			}
		} catch (Exception var126) {
			LOGGER.error("Error extracting world", (Throwable)var126);
			this.error = true;
		} finally {
			if (tarArchiveInputStream != null) {
				tarArchiveInputStream.close();
			}

			if (file != null) {
				file.delete();
			}

			try (LevelStorageSource.LevelStorageAccess levelStorageAccess2 = levelStorageSource.createAccess(string2)) {
				levelStorageAccess2.renameLevel(string2.trim());
				Path path2 = levelStorageAccess2.getLevelPath(LevelResource.LEVEL_DATA_FILE);
				deletePlayerTag(path2.toFile());
			} catch (IOException var124) {
				LOGGER.error("Failed to rename unpacked realms level {}", string2, var124);
			}

			this.resourcePackPath = new File(file2, string2 + File.separator + "resources.zip");
		}
	}

	private static void deletePlayerTag(File file) {
		if (file.exists()) {
			try {
				CompoundTag compoundTag = NbtIo.readCompressed(file);
				CompoundTag compoundTag2 = compoundTag.getCompound("Data");
				compoundTag2.remove("Player");
				NbtIo.writeCompressed(compoundTag, file);
			} catch (Exception var3) {
				var3.printStackTrace();
			}
		}
	}

	@Environment(EnvType.CLIENT)
	class DownloadCountingOutputStream extends CountingOutputStream {
		private ActionListener listener;

		public DownloadCountingOutputStream(OutputStream outputStream) {
			super(outputStream);
		}

		public void setListener(ActionListener actionListener) {
			this.listener = actionListener;
		}

		@Override
		protected void afterWrite(int i) throws IOException {
			super.afterWrite(i);
			if (this.listener != null) {
				this.listener.actionPerformed(new ActionEvent(this, 0, null));
			}
		}
	}

	@Environment(EnvType.CLIENT)
	class ProgressListener implements ActionListener {
		private final String worldName;
		private final File tempFile;
		private final LevelStorageSource levelStorageSource;
		private final RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus;

		private ProgressListener(String string, File file, LevelStorageSource levelStorageSource, RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus) {
			this.worldName = string;
			this.tempFile = file;
			this.levelStorageSource = levelStorageSource;
			this.downloadStatus = downloadStatus;
		}

		public void actionPerformed(ActionEvent actionEvent) {
			this.downloadStatus.bytesWritten = ((FileDownload.DownloadCountingOutputStream)actionEvent.getSource()).getByteCount();
			if (this.downloadStatus.bytesWritten >= this.downloadStatus.totalBytes && !FileDownload.this.cancelled && !FileDownload.this.error) {
				try {
					FileDownload.this.extracting = true;
					FileDownload.this.untarGzipArchive(this.worldName, this.tempFile, this.levelStorageSource);
				} catch (IOException var3) {
					FileDownload.LOGGER.error("Error extracting archive", (Throwable)var3);
					FileDownload.this.error = true;
				}
			}
		}
	}

	@Environment(EnvType.CLIENT)
	class ResourcePackProgressListener implements ActionListener {
		private final File tempFile;
		private final RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus;
		private final WorldDownload worldDownload;

		private ResourcePackProgressListener(File file, RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus, WorldDownload worldDownload) {
			this.tempFile = file;
			this.downloadStatus = downloadStatus;
			this.worldDownload = worldDownload;
		}

		public void actionPerformed(ActionEvent actionEvent) {
			this.downloadStatus.bytesWritten = ((FileDownload.DownloadCountingOutputStream)actionEvent.getSource()).getByteCount();
			if (this.downloadStatus.bytesWritten >= this.downloadStatus.totalBytes && !FileDownload.this.cancelled) {
				try {
					String string = Hashing.sha1().hashBytes(Files.toByteArray(this.tempFile)).toString();
					if (string.equals(this.worldDownload.resourcePackHash)) {
						FileUtils.copyFile(this.tempFile, FileDownload.this.resourcePackPath);
						FileDownload.this.finished = true;
					} else {
						FileDownload.LOGGER.error("Resourcepack had wrong hash (expected " + this.worldDownload.resourcePackHash + ", found " + string + "). Deleting it.");
						FileUtils.deleteQuietly(this.tempFile);
						FileDownload.this.error = true;
					}
				} catch (IOException var3) {
					FileDownload.LOGGER.error("Error copying resourcepack file", var3.getMessage());
					FileDownload.this.error = true;
				}
			}
		}
	}
}
