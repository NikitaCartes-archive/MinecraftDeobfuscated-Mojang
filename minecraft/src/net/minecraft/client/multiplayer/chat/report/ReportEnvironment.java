package net.minecraft.client.multiplayer.chat.report;

import com.mojang.authlib.yggdrasil.request.AbuseReportRequest.ClientInfo;
import com.mojang.authlib.yggdrasil.request.AbuseReportRequest.RealmInfo;
import com.mojang.authlib.yggdrasil.request.AbuseReportRequest.ThirdPartyServerInfo;
import com.mojang.realmsclient.dto.RealmsServer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public record ReportEnvironment(String clientVersion, @Nullable ReportEnvironment.Server server) {
	public static ReportEnvironment local() {
		return create(null);
	}

	public static ReportEnvironment thirdParty(String string) {
		return create(new ReportEnvironment.Server.ThirdParty(string));
	}

	public static ReportEnvironment realm(RealmsServer realmsServer) {
		return create(new ReportEnvironment.Server.Realm(realmsServer));
	}

	public static ReportEnvironment create(@Nullable ReportEnvironment.Server server) {
		return new ReportEnvironment(getClientVersion(), server);
	}

	public ClientInfo clientInfo() {
		return new ClientInfo(this.clientVersion);
	}

	@Nullable
	public ThirdPartyServerInfo thirdPartyServerInfo() {
		return this.server instanceof ReportEnvironment.Server.ThirdParty thirdParty ? new ThirdPartyServerInfo(thirdParty.ip) : null;
	}

	@Nullable
	public RealmInfo realmInfo() {
		return this.server instanceof ReportEnvironment.Server.Realm realm ? new RealmInfo(String.valueOf(realm.realmId()), realm.slotId()) : null;
	}

	private static String getClientVersion() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("23w03a");
		if (Minecraft.checkModStatus().shouldReportAsModified()) {
			stringBuilder.append(" (modded)");
		}

		return stringBuilder.toString();
	}

	@Environment(EnvType.CLIENT)
	public interface Server {
		@Environment(EnvType.CLIENT)
		public static record Realm(long realmId, int slotId) implements ReportEnvironment.Server {
			public Realm(RealmsServer realmsServer) {
				this(realmsServer.id, realmsServer.activeSlot);
			}
		}

		@Environment(EnvType.CLIENT)
		public static record ThirdParty(String ip) implements ReportEnvironment.Server {
		}
	}
}
