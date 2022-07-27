/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.multiplayer.chat.report;

import com.mojang.authlib.yggdrasil.request.AbuseReportRequest;
import com.mojang.realmsclient.dto.RealmsServer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record ReportEnvironment(String clientVersion, @Nullable Server server) {
    public static ReportEnvironment local() {
        return ReportEnvironment.create(null);
    }

    public static ReportEnvironment thirdParty(String string) {
        return ReportEnvironment.create(new Server.ThirdParty(string));
    }

    public static ReportEnvironment realm(RealmsServer realmsServer) {
        return ReportEnvironment.create(new Server.Realm(realmsServer));
    }

    public static ReportEnvironment create(@Nullable Server server) {
        return new ReportEnvironment(ReportEnvironment.getClientVersion(), server);
    }

    public AbuseReportRequest.ClientInfo clientInfo() {
        return new AbuseReportRequest.ClientInfo(this.clientVersion);
    }

    @Nullable
    public AbuseReportRequest.ThirdPartyServerInfo thirdPartyServerInfo() {
        Server server = this.server;
        if (server instanceof Server.ThirdParty) {
            Server.ThirdParty thirdParty = (Server.ThirdParty)server;
            return new AbuseReportRequest.ThirdPartyServerInfo(thirdParty.ip);
        }
        return null;
    }

    @Nullable
    public AbuseReportRequest.RealmInfo realmInfo() {
        Server server = this.server;
        if (server instanceof Server.Realm) {
            Server.Realm realm = (Server.Realm)server;
            return new AbuseReportRequest.RealmInfo(String.valueOf(realm.realmId()), realm.slotId());
        }
        return null;
    }

    private static String getClientVersion() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("1.19.1");
        if (Minecraft.checkModStatus().shouldReportAsModified()) {
            stringBuilder.append(" (modded)");
        }
        return stringBuilder.toString();
    }

    @Nullable
    public Server server() {
        return this.server;
    }

    @Environment(value=EnvType.CLIENT)
    public static interface Server {

        @Environment(value=EnvType.CLIENT)
        public record Realm(long realmId, int slotId) implements Server
        {
            public Realm(RealmsServer realmsServer) {
                this(realmsServer.id, realmsServer.activeSlot);
            }
        }

        @Environment(value=EnvType.CLIENT)
        public record ThirdParty(String ip) implements Server
        {
        }
    }
}

