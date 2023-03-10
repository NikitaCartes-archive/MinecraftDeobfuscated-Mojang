/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.gameevent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.GameEventListenerRegistry;
import net.minecraft.world.phys.Vec3;

public class GameEventDispatcher {
    private final ServerLevel level;

    public GameEventDispatcher(ServerLevel serverLevel) {
        this.level = serverLevel;
    }

    public void post(GameEvent gameEvent, Vec3 vec3, GameEvent.Context context) {
        int i = gameEvent.getNotificationRadius();
        BlockPos blockPos = BlockPos.containing(vec3);
        int j = SectionPos.blockToSectionCoord(blockPos.getX() - i);
        int k = SectionPos.blockToSectionCoord(blockPos.getY() - i);
        int l = SectionPos.blockToSectionCoord(blockPos.getZ() - i);
        int m = SectionPos.blockToSectionCoord(blockPos.getX() + i);
        int n = SectionPos.blockToSectionCoord(blockPos.getY() + i);
        int o = SectionPos.blockToSectionCoord(blockPos.getZ() + i);
        ArrayList<GameEvent.ListenerInfo> list = new ArrayList<GameEvent.ListenerInfo>();
        GameEventListenerRegistry.ListenerVisitor listenerVisitor = (gameEventListener, vec32) -> {
            if (gameEventListener.getDeliveryMode() == GameEventListener.DeliveryMode.BY_DISTANCE) {
                list.add(new GameEvent.ListenerInfo(gameEvent, vec3, context, gameEventListener, vec32));
            } else {
                gameEventListener.handleGameEvent(this.level, gameEvent, context, vec3);
            }
        };
        boolean bl = false;
        for (int p = j; p <= m; ++p) {
            for (int q = l; q <= o; ++q) {
                LevelChunk chunkAccess = this.level.getChunkSource().getChunkNow(p, q);
                if (chunkAccess == null) continue;
                for (int r = k; r <= n; ++r) {
                    bl |= ((ChunkAccess)chunkAccess).getListenerRegistry(r).visitInRangeListeners(gameEvent, vec3, context, listenerVisitor);
                }
            }
        }
        if (!list.isEmpty()) {
            this.handleGameEventMessagesInQueue(list);
        }
        if (bl) {
            DebugPackets.sendGameEventInfo(this.level, gameEvent, vec3);
        }
    }

    private void handleGameEventMessagesInQueue(List<GameEvent.ListenerInfo> list) {
        Collections.sort(list);
        for (GameEvent.ListenerInfo listenerInfo : list) {
            GameEventListener gameEventListener = listenerInfo.recipient();
            gameEventListener.handleGameEvent(this.level, listenerInfo.gameEvent(), listenerInfo.context(), listenerInfo.source());
        }
    }
}

