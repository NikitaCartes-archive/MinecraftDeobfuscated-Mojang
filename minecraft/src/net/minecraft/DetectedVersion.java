package net.minecraft;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.bridge.game.GameVersion;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DetectedVersion implements GameVersion {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final GameVersion BUILT_IN = new DetectedVersion();
	private final String id;
	private final String name;
	private final boolean stable;
	private final int worldVersion;
	private final int protocolVersion;
	private final int packVersion;
	private final Date buildTime;
	private final String releaseTarget;

	private DetectedVersion() {
		this.id = UUID.randomUUID().toString().replaceAll("-", "");
		this.name = "1.16.5";
		this.stable = true;
		this.worldVersion = 2586;
		this.protocolVersion = SharedConstants.getProtocolVersion();
		this.packVersion = 6;
		this.buildTime = new Date();
		this.releaseTarget = "1.16.5";
	}

	private DetectedVersion(JsonObject jsonObject) {
		this.id = GsonHelper.getAsString(jsonObject, "id");
		this.name = GsonHelper.getAsString(jsonObject, "name");
		this.releaseTarget = GsonHelper.getAsString(jsonObject, "release_target");
		this.stable = GsonHelper.getAsBoolean(jsonObject, "stable");
		this.worldVersion = GsonHelper.getAsInt(jsonObject, "world_version");
		this.protocolVersion = GsonHelper.getAsInt(jsonObject, "protocol_version");
		this.packVersion = GsonHelper.getAsInt(jsonObject, "pack_version");
		this.buildTime = Date.from(ZonedDateTime.parse(GsonHelper.getAsString(jsonObject, "build_time")).toInstant());
	}

	public static GameVersion tryDetectVersion() {
		try {
			InputStream inputStream = DetectedVersion.class.getResourceAsStream("/version.json");
			Throwable var1 = null;

			DetectedVersion var4;
			try {
				if (inputStream == null) {
					LOGGER.warn("Missing version information!");
					return BUILT_IN;
				}

				InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
				Throwable var3 = null;

				try {
					var4 = new DetectedVersion(GsonHelper.parse(inputStreamReader));
				} catch (Throwable var30) {
					var3 = var30;
					throw var30;
				} finally {
					if (inputStreamReader != null) {
						if (var3 != null) {
							try {
								inputStreamReader.close();
							} catch (Throwable var29) {
								var3.addSuppressed(var29);
							}
						} else {
							inputStreamReader.close();
						}
					}
				}
			} catch (Throwable var32) {
				var1 = var32;
				throw var32;
			} finally {
				if (inputStream != null) {
					if (var1 != null) {
						try {
							inputStream.close();
						} catch (Throwable var28) {
							var1.addSuppressed(var28);
						}
					} else {
						inputStream.close();
					}
				}
			}

			return var4;
		} catch (JsonParseException | IOException var34) {
			throw new IllegalStateException("Game version information is corrupt", var34);
		}
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getReleaseTarget() {
		return this.releaseTarget;
	}

	@Override
	public int getWorldVersion() {
		return this.worldVersion;
	}

	@Override
	public int getProtocolVersion() {
		return this.protocolVersion;
	}

	@Override
	public int getPackVersion() {
		return this.packVersion;
	}

	@Override
	public Date getBuildTime() {
		return this.buildTime;
	}

	@Override
	public boolean isStable() {
		return this.stable;
	}
}
