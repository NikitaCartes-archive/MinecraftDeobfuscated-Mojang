package net.minecraft;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.bridge.game.GameVersion;
import com.mojang.bridge.game.PackType;
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
	private final int resourcePackVersion;
	private final int dataPackVersion;
	private final Date buildTime;
	private final String releaseTarget;

	private DetectedVersion() {
		this.id = UUID.randomUUID().toString().replaceAll("-", "");
		this.name = "1.17-pre1";
		this.stable = false;
		this.worldVersion = 2716;
		this.protocolVersion = SharedConstants.getProtocolVersion();
		this.resourcePackVersion = 7;
		this.dataPackVersion = 7;
		this.buildTime = new Date();
		this.releaseTarget = "1.17";
	}

	private DetectedVersion(JsonObject jsonObject) {
		this.id = GsonHelper.getAsString(jsonObject, "id");
		this.name = GsonHelper.getAsString(jsonObject, "name");
		this.releaseTarget = GsonHelper.getAsString(jsonObject, "release_target");
		this.stable = GsonHelper.getAsBoolean(jsonObject, "stable");
		this.worldVersion = GsonHelper.getAsInt(jsonObject, "world_version");
		this.protocolVersion = GsonHelper.getAsInt(jsonObject, "protocol_version");
		JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "pack_version");
		this.resourcePackVersion = GsonHelper.getAsInt(jsonObject2, "resource");
		this.dataPackVersion = GsonHelper.getAsInt(jsonObject2, "data");
		this.buildTime = Date.from(ZonedDateTime.parse(GsonHelper.getAsString(jsonObject, "build_time")).toInstant());
	}

	public static GameVersion tryDetectVersion() {
		try {
			InputStream inputStream = DetectedVersion.class.getResourceAsStream("/version.json");

			GameVersion var9;
			label63: {
				DetectedVersion var2;
				try {
					if (inputStream == null) {
						LOGGER.warn("Missing version information!");
						var9 = BUILT_IN;
						break label63;
					}

					InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

					try {
						var2 = new DetectedVersion(GsonHelper.parse(inputStreamReader));
					} catch (Throwable var6) {
						try {
							inputStreamReader.close();
						} catch (Throwable var5) {
							var6.addSuppressed(var5);
						}

						throw var6;
					}

					inputStreamReader.close();
				} catch (Throwable var7) {
					if (inputStream != null) {
						try {
							inputStream.close();
						} catch (Throwable var4) {
							var7.addSuppressed(var4);
						}
					}

					throw var7;
				}

				if (inputStream != null) {
					inputStream.close();
				}

				return var2;
			}

			if (inputStream != null) {
				inputStream.close();
			}

			return var9;
		} catch (JsonParseException | IOException var8) {
			throw new IllegalStateException("Game version information is corrupt", var8);
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
	public int getPackVersion(PackType packType) {
		return packType == PackType.DATA ? this.dataPackVersion : this.resourcePackVersion;
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
