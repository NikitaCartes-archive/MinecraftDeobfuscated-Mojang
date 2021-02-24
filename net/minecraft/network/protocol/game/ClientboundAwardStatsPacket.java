/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;

public class ClientboundAwardStatsPacket
implements Packet<ClientGamePacketListener> {
    private final Object2IntMap<Stat<?>> stats;

    public ClientboundAwardStatsPacket(Object2IntMap<Stat<?>> object2IntMap) {
        this.stats = object2IntMap;
    }

    public ClientboundAwardStatsPacket(FriendlyByteBuf friendlyByteBuf2) {
        this.stats = friendlyByteBuf2.readMap(Object2IntOpenHashMap::new, friendlyByteBuf -> {
            int i = friendlyByteBuf.readVarInt();
            int j = friendlyByteBuf.readVarInt();
            return ClientboundAwardStatsPacket.readStatCap((StatType)Registry.STAT_TYPE.byId(i), j);
        }, FriendlyByteBuf::readVarInt);
    }

    private static <T> Stat<T> readStatCap(StatType<T> statType, int i) {
        return statType.get(statType.getRegistry().byId(i));
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleAwardStats(this);
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeMap(this.stats, (friendlyByteBuf, stat) -> {
            friendlyByteBuf.writeVarInt(Registry.STAT_TYPE.getId(stat.getType()));
            friendlyByteBuf.writeVarInt(this.getStatIdCap((Stat)stat));
        }, FriendlyByteBuf::writeVarInt);
    }

    private <T> int getStatIdCap(Stat<T> stat) {
        return stat.getType().getRegistry().getId(stat.getValue());
    }

    @Environment(value=EnvType.CLIENT)
    public Map<Stat<?>, Integer> getStats() {
        return this.stats;
    }
}

