/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.realmsclient.dto;

import com.google.common.base.Joiner;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.PlayerInfo;
import com.mojang.realmsclient.dto.RealmsServerPing;
import com.mojang.realmsclient.dto.RealmsServerPlayerList;
import com.mojang.realmsclient.dto.RealmsWorldOptions;
import com.mojang.realmsclient.dto.ValueObject;
import com.mojang.realmsclient.util.JsonUtils;
import com.mojang.realmsclient.util.RealmsUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsServer
extends ValueObject {
    private static final Logger LOGGER = LogUtils.getLogger();
    public long id;
    public String remoteSubscriptionId;
    public String name;
    public String motd;
    public State state;
    public String owner;
    public String ownerUUID;
    public List<PlayerInfo> players;
    public Map<Integer, RealmsWorldOptions> slots;
    public boolean expired;
    public boolean expiredTrial;
    public int daysLeft;
    public WorldType worldType;
    public int activeSlot;
    public String minigameName;
    public int minigameId;
    public String minigameImage;
    public RealmsServerPing serverPing = new RealmsServerPing();

    public String getDescription() {
        return this.motd;
    }

    public String getName() {
        return this.name;
    }

    public String getMinigameName() {
        return this.minigameName;
    }

    public void setName(String string) {
        this.name = string;
    }

    public void setDescription(String string) {
        this.motd = string;
    }

    public void updateServerPing(RealmsServerPlayerList realmsServerPlayerList) {
        ArrayList<String> list = Lists.newArrayList();
        int i = 0;
        for (String string : realmsServerPlayerList.players) {
            if (string.equals(Minecraft.getInstance().getUser().getUuid())) continue;
            String string2 = "";
            try {
                string2 = RealmsUtil.uuidToName(string);
            } catch (Exception exception) {
                LOGGER.error("Could not get name for {}", (Object)string, (Object)exception);
                continue;
            }
            list.add(string2);
            ++i;
        }
        this.serverPing.nrOfPlayers = String.valueOf(i);
        this.serverPing.playerList = Joiner.on('\n').join(list);
    }

    public static RealmsServer parse(JsonObject jsonObject) {
        RealmsServer realmsServer = new RealmsServer();
        try {
            realmsServer.id = JsonUtils.getLongOr("id", jsonObject, -1L);
            realmsServer.remoteSubscriptionId = JsonUtils.getStringOr("remoteSubscriptionId", jsonObject, null);
            realmsServer.name = JsonUtils.getStringOr("name", jsonObject, null);
            realmsServer.motd = JsonUtils.getStringOr("motd", jsonObject, null);
            realmsServer.state = RealmsServer.getState(JsonUtils.getStringOr("state", jsonObject, State.CLOSED.name()));
            realmsServer.owner = JsonUtils.getStringOr("owner", jsonObject, null);
            if (jsonObject.get("players") != null && jsonObject.get("players").isJsonArray()) {
                realmsServer.players = RealmsServer.parseInvited(jsonObject.get("players").getAsJsonArray());
                RealmsServer.sortInvited(realmsServer);
            } else {
                realmsServer.players = Lists.newArrayList();
            }
            realmsServer.daysLeft = JsonUtils.getIntOr("daysLeft", jsonObject, 0);
            realmsServer.expired = JsonUtils.getBooleanOr("expired", jsonObject, false);
            realmsServer.expiredTrial = JsonUtils.getBooleanOr("expiredTrial", jsonObject, false);
            realmsServer.worldType = RealmsServer.getWorldType(JsonUtils.getStringOr("worldType", jsonObject, WorldType.NORMAL.name()));
            realmsServer.ownerUUID = JsonUtils.getStringOr("ownerUUID", jsonObject, "");
            realmsServer.slots = jsonObject.get("slots") != null && jsonObject.get("slots").isJsonArray() ? RealmsServer.parseSlots(jsonObject.get("slots").getAsJsonArray()) : RealmsServer.createEmptySlots();
            realmsServer.minigameName = JsonUtils.getStringOr("minigameName", jsonObject, null);
            realmsServer.activeSlot = JsonUtils.getIntOr("activeSlot", jsonObject, -1);
            realmsServer.minigameId = JsonUtils.getIntOr("minigameId", jsonObject, -1);
            realmsServer.minigameImage = JsonUtils.getStringOr("minigameImage", jsonObject, null);
        } catch (Exception exception) {
            LOGGER.error("Could not parse McoServer: {}", (Object)exception.getMessage());
        }
        return realmsServer;
    }

    private static void sortInvited(RealmsServer realmsServer) {
        realmsServer.players.sort((playerInfo, playerInfo2) -> ComparisonChain.start().compareFalseFirst(playerInfo2.getAccepted(), playerInfo.getAccepted()).compare((Comparable<?>)((Object)playerInfo.getName().toLowerCase(Locale.ROOT)), (Comparable<?>)((Object)playerInfo2.getName().toLowerCase(Locale.ROOT))).result());
    }

    private static List<PlayerInfo> parseInvited(JsonArray jsonArray) {
        ArrayList<PlayerInfo> list = Lists.newArrayList();
        for (JsonElement jsonElement : jsonArray) {
            try {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                PlayerInfo playerInfo = new PlayerInfo();
                playerInfo.setName(JsonUtils.getStringOr("name", jsonObject, null));
                playerInfo.setUuid(JsonUtils.getStringOr("uuid", jsonObject, null));
                playerInfo.setOperator(JsonUtils.getBooleanOr("operator", jsonObject, false));
                playerInfo.setAccepted(JsonUtils.getBooleanOr("accepted", jsonObject, false));
                playerInfo.setOnline(JsonUtils.getBooleanOr("online", jsonObject, false));
                list.add(playerInfo);
            } catch (Exception exception) {}
        }
        return list;
    }

    private static Map<Integer, RealmsWorldOptions> parseSlots(JsonArray jsonArray) {
        HashMap<Integer, RealmsWorldOptions> map = Maps.newHashMap();
        for (JsonElement jsonElement : jsonArray) {
            try {
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                JsonParser jsonParser = new JsonParser();
                JsonElement jsonElement2 = jsonParser.parse(jsonObject.get("options").getAsString());
                RealmsWorldOptions realmsWorldOptions = jsonElement2 == null ? RealmsWorldOptions.createDefaults() : RealmsWorldOptions.parse(jsonElement2.getAsJsonObject());
                int i = JsonUtils.getIntOr("slotId", jsonObject, -1);
                map.put(i, realmsWorldOptions);
            } catch (Exception exception) {}
        }
        for (int j = 1; j <= 3; ++j) {
            if (map.containsKey(j)) continue;
            map.put(j, RealmsWorldOptions.createEmptyDefaults());
        }
        return map;
    }

    private static Map<Integer, RealmsWorldOptions> createEmptySlots() {
        HashMap<Integer, RealmsWorldOptions> map = Maps.newHashMap();
        map.put(1, RealmsWorldOptions.createEmptyDefaults());
        map.put(2, RealmsWorldOptions.createEmptyDefaults());
        map.put(3, RealmsWorldOptions.createEmptyDefaults());
        return map;
    }

    public static RealmsServer parse(String string) {
        try {
            return RealmsServer.parse(new JsonParser().parse(string).getAsJsonObject());
        } catch (Exception exception) {
            LOGGER.error("Could not parse McoServer: {}", (Object)exception.getMessage());
            return new RealmsServer();
        }
    }

    private static State getState(String string) {
        try {
            return State.valueOf(string);
        } catch (Exception exception) {
            return State.CLOSED;
        }
    }

    private static WorldType getWorldType(String string) {
        try {
            return WorldType.valueOf(string);
        } catch (Exception exception) {
            return WorldType.NORMAL;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.id, this.name, this.motd, this.state, this.owner, this.expired});
    }

    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (object == this) {
            return true;
        }
        if (object.getClass() != this.getClass()) {
            return false;
        }
        RealmsServer realmsServer = (RealmsServer)object;
        return new EqualsBuilder().append(this.id, realmsServer.id).append(this.name, realmsServer.name).append(this.motd, realmsServer.motd).append((Object)this.state, (Object)realmsServer.state).append(this.owner, realmsServer.owner).append(this.expired, realmsServer.expired).append((Object)this.worldType, (Object)this.worldType).isEquals();
    }

    public RealmsServer clone() {
        RealmsServer realmsServer = new RealmsServer();
        realmsServer.id = this.id;
        realmsServer.remoteSubscriptionId = this.remoteSubscriptionId;
        realmsServer.name = this.name;
        realmsServer.motd = this.motd;
        realmsServer.state = this.state;
        realmsServer.owner = this.owner;
        realmsServer.players = this.players;
        realmsServer.slots = this.cloneSlots(this.slots);
        realmsServer.expired = this.expired;
        realmsServer.expiredTrial = this.expiredTrial;
        realmsServer.daysLeft = this.daysLeft;
        realmsServer.serverPing = new RealmsServerPing();
        realmsServer.serverPing.nrOfPlayers = this.serverPing.nrOfPlayers;
        realmsServer.serverPing.playerList = this.serverPing.playerList;
        realmsServer.worldType = this.worldType;
        realmsServer.ownerUUID = this.ownerUUID;
        realmsServer.minigameName = this.minigameName;
        realmsServer.activeSlot = this.activeSlot;
        realmsServer.minigameId = this.minigameId;
        realmsServer.minigameImage = this.minigameImage;
        return realmsServer;
    }

    public Map<Integer, RealmsWorldOptions> cloneSlots(Map<Integer, RealmsWorldOptions> map) {
        HashMap<Integer, RealmsWorldOptions> map2 = Maps.newHashMap();
        for (Map.Entry<Integer, RealmsWorldOptions> entry : map.entrySet()) {
            map2.put(entry.getKey(), entry.getValue().clone());
        }
        return map2;
    }

    public String getWorldName(int i) {
        return this.name + " (" + this.slots.get(i).getSlotName(i) + ")";
    }

    public ServerData toServerData(String string) {
        return new ServerData(this.name, string, false);
    }

    public /* synthetic */ Object clone() throws CloneNotSupportedException {
        return this.clone();
    }

    @Environment(value=EnvType.CLIENT)
    public static enum State {
        CLOSED,
        OPEN,
        UNINITIALIZED;

    }

    @Environment(value=EnvType.CLIENT)
    public static enum WorldType {
        NORMAL,
        MINIGAME,
        ADVENTUREMAP,
        EXPERIENCE,
        INSPIRATION;

    }

    @Environment(value=EnvType.CLIENT)
    public static class McoServerComparator
    implements Comparator<RealmsServer> {
        private final String refOwner;

        public McoServerComparator(String string) {
            this.refOwner = string;
        }

        @Override
        public int compare(RealmsServer realmsServer, RealmsServer realmsServer2) {
            return ComparisonChain.start().compareTrueFirst(realmsServer.state == State.UNINITIALIZED, realmsServer2.state == State.UNINITIALIZED).compareTrueFirst(realmsServer.expiredTrial, realmsServer2.expiredTrial).compareTrueFirst(realmsServer.owner.equals(this.refOwner), realmsServer2.owner.equals(this.refOwner)).compareFalseFirst(realmsServer.expired, realmsServer2.expired).compareTrueFirst(realmsServer.state == State.OPEN, realmsServer2.state == State.OPEN).compare(realmsServer.id, realmsServer2.id).result();
        }

        @Override
        public /* synthetic */ int compare(Object object, Object object2) {
            return this.compare((RealmsServer)object, (RealmsServer)object2);
        }
    }
}

