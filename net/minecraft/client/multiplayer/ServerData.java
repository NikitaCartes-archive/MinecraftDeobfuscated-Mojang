/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.multiplayer;

import java.util.Collections;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ServerData {
    public String name;
    public String ip;
    public Component status;
    public Component motd;
    public long ping;
    public int protocol = SharedConstants.getCurrentVersion().getProtocolVersion();
    public Component version = Component.literal(SharedConstants.getCurrentVersion().getName());
    public boolean pinged;
    public List<Component> playerList = Collections.emptyList();
    private ServerPackStatus packStatus = ServerPackStatus.PROMPT;
    @Nullable
    private String iconB64;
    private boolean lan;

    public ServerData(String string, String string2, boolean bl) {
        this.name = string;
        this.ip = string2;
        this.lan = bl;
    }

    public CompoundTag write() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("name", this.name);
        compoundTag.putString("ip", this.ip);
        if (this.iconB64 != null) {
            compoundTag.putString("icon", this.iconB64);
        }
        if (this.packStatus == ServerPackStatus.ENABLED) {
            compoundTag.putBoolean("acceptTextures", true);
        } else if (this.packStatus == ServerPackStatus.DISABLED) {
            compoundTag.putBoolean("acceptTextures", false);
        }
        return compoundTag;
    }

    public ServerPackStatus getResourcePackStatus() {
        return this.packStatus;
    }

    public void setResourcePackStatus(ServerPackStatus serverPackStatus) {
        this.packStatus = serverPackStatus;
    }

    public static ServerData read(CompoundTag compoundTag) {
        ServerData serverData = new ServerData(compoundTag.getString("name"), compoundTag.getString("ip"), false);
        if (compoundTag.contains("icon", 8)) {
            serverData.setIconB64(compoundTag.getString("icon"));
        }
        if (compoundTag.contains("acceptTextures", 1)) {
            if (compoundTag.getBoolean("acceptTextures")) {
                serverData.setResourcePackStatus(ServerPackStatus.ENABLED);
            } else {
                serverData.setResourcePackStatus(ServerPackStatus.DISABLED);
            }
        } else {
            serverData.setResourcePackStatus(ServerPackStatus.PROMPT);
        }
        return serverData;
    }

    @Nullable
    public String getIconB64() {
        return this.iconB64;
    }

    public void setIconB64(@Nullable String string) {
        this.iconB64 = string;
    }

    public boolean isLan() {
        return this.lan;
    }

    public void copyFrom(ServerData serverData) {
        this.ip = serverData.ip;
        this.name = serverData.name;
        this.setResourcePackStatus(serverData.getResourcePackStatus());
        this.iconB64 = serverData.iconB64;
        this.lan = serverData.lan;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum ServerPackStatus {
        ENABLED("enabled"),
        DISABLED("disabled"),
        PROMPT("prompt");

        private final Component name;

        private ServerPackStatus(String string2) {
            this.name = Component.translatable("addServer.resourcePack." + string2);
        }

        public Component getName() {
            return this.name;
        }
    }
}

