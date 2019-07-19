package net.minecraft.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.minecraft.SharedConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Eula {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Path file;
	private final boolean agreed;

	public Eula(Path path) {
		this.file = path;
		this.agreed = SharedConstants.IS_RUNNING_IN_IDE || this.readFile();
	}

	private boolean readFile() {
		try {
			InputStream inputStream = Files.newInputStream(this.file);
			Throwable var2 = null;

			boolean var4;
			try {
				Properties properties = new Properties();
				properties.load(inputStream);
				var4 = Boolean.parseBoolean(properties.getProperty("eula", "false"));
			} catch (Throwable var14) {
				var2 = var14;
				throw var14;
			} finally {
				if (inputStream != null) {
					if (var2 != null) {
						try {
							inputStream.close();
						} catch (Throwable var13) {
							var2.addSuppressed(var13);
						}
					} else {
						inputStream.close();
					}
				}
			}

			return var4;
		} catch (Exception var16) {
			LOGGER.warn("Failed to load {}", this.file);
			this.saveDefaults();
			return false;
		}
	}

	public boolean hasAgreedToEULA() {
		return this.agreed;
	}

	private void saveDefaults() {
		if (!SharedConstants.IS_RUNNING_IN_IDE) {
			try {
				OutputStream outputStream = Files.newOutputStream(this.file);
				Throwable var2 = null;

				try {
					Properties properties = new Properties();
					properties.setProperty("eula", "false");
					properties.store(
						outputStream,
						"By changing the setting below to TRUE you are indicating your agreement to our EULA (https://account.mojang.com/documents/minecraft_eula)."
					);
				} catch (Throwable var12) {
					var2 = var12;
					throw var12;
				} finally {
					if (outputStream != null) {
						if (var2 != null) {
							try {
								outputStream.close();
							} catch (Throwable var11) {
								var2.addSuppressed(var11);
							}
						} else {
							outputStream.close();
						}
					}
				}
			} catch (Exception var14) {
				LOGGER.warn("Failed to save {}", this.file, var14);
			}
		}
	}
}
