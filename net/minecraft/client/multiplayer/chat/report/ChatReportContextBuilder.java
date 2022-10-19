/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.multiplayer.chat.report;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.LoggedChatEvent;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.PlayerChatMessage;

@Environment(value=EnvType.CLIENT)
public class ChatReportContextBuilder {
    final int leadingCount;
    private final List<Collector> activeCollectors = new ArrayList<Collector>();

    public ChatReportContextBuilder(int i) {
        this.leadingCount = i;
    }

    public void collectAllContext(ChatLog chatLog, IntCollection intCollection, Handler handler) {
        IntRBTreeSet intSortedSet = new IntRBTreeSet(intCollection);
        for (int i = intSortedSet.lastInt(); i >= chatLog.start() && (this.isActive() || !intSortedSet.isEmpty()); --i) {
            LoggedChatEvent loggedChatEvent = chatLog.lookup(i);
            if (!(loggedChatEvent instanceof LoggedChatMessage.Player)) continue;
            LoggedChatMessage.Player player = (LoggedChatMessage.Player)loggedChatEvent;
            boolean bl = this.acceptContext(player.message());
            if (intSortedSet.remove(i)) {
                this.trackContext(player.message());
                handler.accept(i, player);
                continue;
            }
            if (!bl) continue;
            handler.accept(i, player);
        }
    }

    public void trackContext(PlayerChatMessage playerChatMessage) {
        this.activeCollectors.add(new Collector(playerChatMessage));
    }

    public boolean acceptContext(PlayerChatMessage playerChatMessage) {
        boolean bl = false;
        Iterator<Collector> iterator = this.activeCollectors.iterator();
        while (iterator.hasNext()) {
            Collector collector = iterator.next();
            if (!collector.accept(playerChatMessage)) continue;
            bl = true;
            if (!collector.isComplete()) continue;
            iterator.remove();
        }
        return bl;
    }

    public boolean isActive() {
        return !this.activeCollectors.isEmpty();
    }

    @Environment(value=EnvType.CLIENT)
    public static interface Handler {
        public void accept(int var1, LoggedChatMessage.Player var2);
    }

    @Environment(value=EnvType.CLIENT)
    class Collector {
        private final Set<MessageSignature> lastSeenSignatures;
        private PlayerChatMessage lastChainMessage;
        private boolean collectingChain = true;
        private int count;

        Collector(PlayerChatMessage playerChatMessage) {
            this.lastSeenSignatures = new ObjectOpenHashSet<MessageSignature>(playerChatMessage.signedBody().lastSeen().entries());
            this.lastChainMessage = playerChatMessage;
        }

        boolean accept(PlayerChatMessage playerChatMessage) {
            if (playerChatMessage.equals(this.lastChainMessage)) {
                return false;
            }
            boolean bl = this.lastSeenSignatures.remove(playerChatMessage.signature());
            if (this.collectingChain && this.lastChainMessage.sender().equals(playerChatMessage.sender())) {
                if (this.lastChainMessage.link().isDescendantOf(playerChatMessage.link())) {
                    bl = true;
                    this.lastChainMessage = playerChatMessage;
                } else {
                    this.collectingChain = false;
                }
            }
            if (bl) {
                ++this.count;
            }
            return bl;
        }

        boolean isComplete() {
            return this.count >= ChatReportContextBuilder.this.leadingCount || !this.collectingChain && this.lastSeenSignatures.isEmpty();
        }
    }
}

