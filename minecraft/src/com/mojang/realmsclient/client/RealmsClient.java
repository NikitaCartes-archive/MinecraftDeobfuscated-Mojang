package com.mojang.realmsclient.client;

import com.mojang.realmsclient.dto.BackupList;
import com.mojang.realmsclient.dto.GuardedSerializer;
import com.mojang.realmsclient.dto.Ops;
import com.mojang.realmsclient.dto.PendingInvitesList;
import com.mojang.realmsclient.dto.PingResult;
import com.mojang.realmsclient.dto.PlayerInfo;
import com.mojang.realmsclient.dto.RealmsDescriptionDto;
import com.mojang.realmsclient.dto.RealmsNews;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsServerAddress;
import com.mojang.realmsclient.dto.RealmsServerList;
import com.mojang.realmsclient.dto.RealmsServerPlayerLists;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.dto.RealmsWorldResetDto;
import com.mojang.realmsclient.dto.Subscription;
import com.mojang.realmsclient.dto.UploadInfo;
import com.mojang.realmsclient.dto.WorldDownload;
import com.mojang.realmsclient.dto.WorldTemplatePaginatedList;
import com.mojang.realmsclient.exception.RealmsHttpException;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@net.fabricmc.api.Environment(EnvType.CLIENT)
public class RealmsClient {
	public static RealmsClient.Environment currentEnvironment = RealmsClient.Environment.PRODUCTION;
	private static boolean initialized;
	private static final Logger LOGGER = LogManager.getLogger();
	private final String sessionId;
	private final String username;
	private static final GuardedSerializer GSON = new GuardedSerializer();

	public static RealmsClient create() {
		Minecraft minecraft = Minecraft.getInstance();
		String string = minecraft.getUser().getName();
		String string2 = minecraft.getUser().getSessionId();
		if (!initialized) {
			initialized = true;
			String string3 = System.getenv("realms.environment");
			if (string3 == null) {
				string3 = System.getProperty("realms.environment");
			}

			if (string3 != null) {
				if ("LOCAL".equals(string3)) {
					switchToLocal();
				} else if ("STAGE".equals(string3)) {
					switchToStage();
				}
			}
		}

		return new RealmsClient(string2, string, minecraft.getProxy());
	}

	public static void switchToStage() {
		currentEnvironment = RealmsClient.Environment.STAGE;
	}

	public static void switchToProd() {
		currentEnvironment = RealmsClient.Environment.PRODUCTION;
	}

	public static void switchToLocal() {
		currentEnvironment = RealmsClient.Environment.LOCAL;
	}

	public RealmsClient(String string, String string2, Proxy proxy) {
		this.sessionId = string;
		this.username = string2;
		RealmsClientConfig.setProxy(proxy);
	}

	public RealmsServerList listWorlds() throws RealmsServiceException {
		String string = this.url("worlds");
		String string2 = this.execute(Request.get(string));
		return RealmsServerList.parse(string2);
	}

	public RealmsServer getOwnWorld(long l) throws RealmsServiceException {
		String string = this.url("worlds" + "/$ID".replace("$ID", String.valueOf(l)));
		String string2 = this.execute(Request.get(string));
		return RealmsServer.parse(string2);
	}

	public RealmsServerPlayerLists getLiveStats() throws RealmsServiceException {
		String string = this.url("activities/liveplayerlist");
		String string2 = this.execute(Request.get(string));
		return RealmsServerPlayerLists.parse(string2);
	}

	public RealmsServerAddress join(long l) throws RealmsServiceException {
		String string = this.url("worlds" + "/v1/$ID/join/pc".replace("$ID", "" + l));
		String string2 = this.execute(Request.get(string, 5000, 30000));
		return RealmsServerAddress.parse(string2);
	}

	public void initializeWorld(long l, String string, String string2) throws RealmsServiceException {
		RealmsDescriptionDto realmsDescriptionDto = new RealmsDescriptionDto(string, string2);
		String string3 = this.url("worlds" + "/$WORLD_ID/initialize".replace("$WORLD_ID", String.valueOf(l)));
		String string4 = GSON.toJson(realmsDescriptionDto);
		this.execute(Request.post(string3, string4, 5000, 10000));
	}

	public Boolean mcoEnabled() throws RealmsServiceException {
		String string = this.url("mco/available");
		String string2 = this.execute(Request.get(string));
		return Boolean.valueOf(string2);
	}

	public Boolean stageAvailable() throws RealmsServiceException {
		String string = this.url("mco/stageAvailable");
		String string2 = this.execute(Request.get(string));
		return Boolean.valueOf(string2);
	}

	public RealmsClient.CompatibleVersionResponse clientCompatible() throws RealmsServiceException {
		String string = this.url("mco/client/compatible");
		String string2 = this.execute(Request.get(string));

		try {
			return RealmsClient.CompatibleVersionResponse.valueOf(string2);
		} catch (IllegalArgumentException var5) {
			throw new RealmsServiceException(500, "Could not check compatible version, got response: " + string2, -1, "");
		}
	}

	public void uninvite(long l, String string) throws RealmsServiceException {
		String string2 = this.url("invites" + "/$WORLD_ID/invite/$UUID".replace("$WORLD_ID", String.valueOf(l)).replace("$UUID", string));
		this.execute(Request.delete(string2));
	}

	public void uninviteMyselfFrom(long l) throws RealmsServiceException {
		String string = this.url("invites" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(l)));
		this.execute(Request.delete(string));
	}

	public RealmsServer invite(long l, String string) throws RealmsServiceException {
		PlayerInfo playerInfo = new PlayerInfo();
		playerInfo.setName(string);
		String string2 = this.url("invites" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(l)));
		String string3 = this.execute(Request.post(string2, GSON.toJson(playerInfo)));
		return RealmsServer.parse(string3);
	}

	public BackupList backupsFor(long l) throws RealmsServiceException {
		String string = this.url("worlds" + "/$WORLD_ID/backups".replace("$WORLD_ID", String.valueOf(l)));
		String string2 = this.execute(Request.get(string));
		return BackupList.parse(string2);
	}

	public void update(long l, String string, String string2) throws RealmsServiceException {
		RealmsDescriptionDto realmsDescriptionDto = new RealmsDescriptionDto(string, string2);
		String string3 = this.url("worlds" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(l)));
		this.execute(Request.post(string3, GSON.toJson(realmsDescriptionDto)));
	}

	public void updateSlot(long l, int i, RealmsWorldOptions realmsWorldOptions) throws RealmsServiceException {
		String string = this.url("worlds" + "/$WORLD_ID/slot/$SLOT_ID".replace("$WORLD_ID", String.valueOf(l)).replace("$SLOT_ID", String.valueOf(i)));
		String string2 = realmsWorldOptions.toJson();
		this.execute(Request.post(string, string2));
	}

	public boolean switchSlot(long l, int i) throws RealmsServiceException {
		String string = this.url("worlds" + "/$WORLD_ID/slot/$SLOT_ID".replace("$WORLD_ID", String.valueOf(l)).replace("$SLOT_ID", String.valueOf(i)));
		String string2 = this.execute(Request.put(string, ""));
		return Boolean.valueOf(string2);
	}

	public void restoreWorld(long l, String string) throws RealmsServiceException {
		String string2 = this.url("worlds" + "/$WORLD_ID/backups".replace("$WORLD_ID", String.valueOf(l)), "backupId=" + string);
		this.execute(Request.put(string2, "", 40000, 600000));
	}

	public WorldTemplatePaginatedList fetchWorldTemplates(int i, int j, RealmsServer.WorldType worldType) throws RealmsServiceException {
		String string = this.url("worlds" + "/templates/$WORLD_TYPE".replace("$WORLD_TYPE", worldType.toString()), String.format("page=%d&pageSize=%d", i, j));
		String string2 = this.execute(Request.get(string));
		return WorldTemplatePaginatedList.parse(string2);
	}

	public Boolean putIntoMinigameMode(long l, String string) throws RealmsServiceException {
		String string2 = "/minigames/$MINIGAME_ID/$WORLD_ID".replace("$MINIGAME_ID", string).replace("$WORLD_ID", String.valueOf(l));
		String string3 = this.url("worlds" + string2);
		return Boolean.valueOf(this.execute(Request.put(string3, "")));
	}

	public Ops op(long l, String string) throws RealmsServiceException {
		String string2 = "/$WORLD_ID/$PROFILE_UUID".replace("$WORLD_ID", String.valueOf(l)).replace("$PROFILE_UUID", string);
		String string3 = this.url("ops" + string2);
		return Ops.parse(this.execute(Request.post(string3, "")));
	}

	public Ops deop(long l, String string) throws RealmsServiceException {
		String string2 = "/$WORLD_ID/$PROFILE_UUID".replace("$WORLD_ID", String.valueOf(l)).replace("$PROFILE_UUID", string);
		String string3 = this.url("ops" + string2);
		return Ops.parse(this.execute(Request.delete(string3)));
	}

	public Boolean open(long l) throws RealmsServiceException {
		String string = this.url("worlds" + "/$WORLD_ID/open".replace("$WORLD_ID", String.valueOf(l)));
		String string2 = this.execute(Request.put(string, ""));
		return Boolean.valueOf(string2);
	}

	public Boolean close(long l) throws RealmsServiceException {
		String string = this.url("worlds" + "/$WORLD_ID/close".replace("$WORLD_ID", String.valueOf(l)));
		String string2 = this.execute(Request.put(string, ""));
		return Boolean.valueOf(string2);
	}

	public Boolean resetWorldWithSeed(long l, String string, Integer integer, boolean bl) throws RealmsServiceException {
		RealmsWorldResetDto realmsWorldResetDto = new RealmsWorldResetDto(string, -1L, integer, bl);
		String string2 = this.url("worlds" + "/$WORLD_ID/reset".replace("$WORLD_ID", String.valueOf(l)));
		String string3 = this.execute(Request.post(string2, GSON.toJson(realmsWorldResetDto), 30000, 80000));
		return Boolean.valueOf(string3);
	}

	public Boolean resetWorldWithTemplate(long l, String string) throws RealmsServiceException {
		RealmsWorldResetDto realmsWorldResetDto = new RealmsWorldResetDto(null, Long.valueOf(string), -1, false);
		String string2 = this.url("worlds" + "/$WORLD_ID/reset".replace("$WORLD_ID", String.valueOf(l)));
		String string3 = this.execute(Request.post(string2, GSON.toJson(realmsWorldResetDto), 30000, 80000));
		return Boolean.valueOf(string3);
	}

	public Subscription subscriptionFor(long l) throws RealmsServiceException {
		String string = this.url("subscriptions" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(l)));
		String string2 = this.execute(Request.get(string));
		return Subscription.parse(string2);
	}

	public int pendingInvitesCount() throws RealmsServiceException {
		String string = this.url("invites/count/pending");
		String string2 = this.execute(Request.get(string));
		return Integer.parseInt(string2);
	}

	public PendingInvitesList pendingInvites() throws RealmsServiceException {
		String string = this.url("invites/pending");
		String string2 = this.execute(Request.get(string));
		return PendingInvitesList.parse(string2);
	}

	public void acceptInvitation(String string) throws RealmsServiceException {
		String string2 = this.url("invites" + "/accept/$INVITATION_ID".replace("$INVITATION_ID", string));
		this.execute(Request.put(string2, ""));
	}

	public WorldDownload requestDownloadInfo(long l, int i) throws RealmsServiceException {
		String string = this.url("worlds" + "/$WORLD_ID/slot/$SLOT_ID/download".replace("$WORLD_ID", String.valueOf(l)).replace("$SLOT_ID", String.valueOf(i)));
		String string2 = this.execute(Request.get(string));
		return WorldDownload.parse(string2);
	}

	@Nullable
	public UploadInfo requestUploadInfo(long l, @Nullable String string) throws RealmsServiceException {
		String string2 = this.url("worlds" + "/$WORLD_ID/backups/upload".replace("$WORLD_ID", String.valueOf(l)));
		return UploadInfo.parse(this.execute(Request.put(string2, UploadInfo.createRequest(string))));
	}

	public void rejectInvitation(String string) throws RealmsServiceException {
		String string2 = this.url("invites" + "/reject/$INVITATION_ID".replace("$INVITATION_ID", string));
		this.execute(Request.put(string2, ""));
	}

	public void agreeToTos() throws RealmsServiceException {
		String string = this.url("mco/tos/agreed");
		this.execute(Request.post(string, ""));
	}

	public RealmsNews getNews() throws RealmsServiceException {
		String string = this.url("mco/v1/news");
		String string2 = this.execute(Request.get(string, 5000, 10000));
		return RealmsNews.parse(string2);
	}

	public void sendPingResults(PingResult pingResult) throws RealmsServiceException {
		String string = this.url("regions/ping/stat");
		this.execute(Request.post(string, GSON.toJson(pingResult)));
	}

	public Boolean trialAvailable() throws RealmsServiceException {
		String string = this.url("trial");
		String string2 = this.execute(Request.get(string));
		return Boolean.valueOf(string2);
	}

	public void deleteWorld(long l) throws RealmsServiceException {
		String string = this.url("worlds" + "/$WORLD_ID".replace("$WORLD_ID", String.valueOf(l)));
		this.execute(Request.delete(string));
	}

	@Nullable
	private String url(String string) {
		return this.url(string, null);
	}

	@Nullable
	private String url(String string, @Nullable String string2) {
		try {
			return new URI(currentEnvironment.protocol, currentEnvironment.baseUrl, "/" + string, string2, null).toASCIIString();
		} catch (URISyntaxException var4) {
			var4.printStackTrace();
			return null;
		}
	}

	private String execute(Request<?> request) throws RealmsServiceException {
		request.cookie("sid", this.sessionId);
		request.cookie("user", this.username);
		request.cookie("version", SharedConstants.getCurrentVersion().getName());

		try {
			int i = request.responseCode();
			if (i == 503) {
				int j = request.getRetryAfterHeader();
				throw new RetryCallException(j);
			} else {
				String string = request.text();
				if (i >= 200 && i < 300) {
					return string;
				} else if (i == 401) {
					String string2 = request.getHeader("WWW-Authenticate");
					LOGGER.info("Could not authorize you against Realms server: " + string2);
					throw new RealmsServiceException(i, string2, -1, string2);
				} else if (string != null && string.length() != 0) {
					RealmsError realmsError = RealmsError.create(string);
					LOGGER.error(
						"Realms http code: " + i + " -  error code: " + realmsError.getErrorCode() + " -  message: " + realmsError.getErrorMessage() + " - raw body: " + string
					);
					throw new RealmsServiceException(i, string, realmsError);
				} else {
					LOGGER.error("Realms error code: " + i + " message: " + string);
					throw new RealmsServiceException(i, string, i, "");
				}
			}
		} catch (RealmsHttpException var5) {
			throw new RealmsServiceException(500, "Could not connect to Realms: " + var5.getMessage(), -1, "");
		}
	}

	@net.fabricmc.api.Environment(EnvType.CLIENT)
	public static enum CompatibleVersionResponse {
		COMPATIBLE,
		OUTDATED,
		OTHER;
	}

	@net.fabricmc.api.Environment(EnvType.CLIENT)
	public static enum Environment {
		PRODUCTION("pc.realms.minecraft.net", "https"),
		STAGE("pc-stage.realms.minecraft.net", "https"),
		LOCAL("localhost:8080", "http");

		public String baseUrl;
		public String protocol;

		private Environment(String string2, String string3) {
			this.baseUrl = string2;
			this.protocol = string3;
		}
	}
}
