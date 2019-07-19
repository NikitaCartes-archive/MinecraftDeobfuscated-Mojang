/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.multiplayer;

import com.google.common.collect.Maps;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ClientAdvancements {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Minecraft minecraft;
    private final AdvancementList advancements = new AdvancementList();
    private final Map<Advancement, AdvancementProgress> progress = Maps.newHashMap();
    @Nullable
    private Listener listener;
    @Nullable
    private Advancement selectedTab;

    public ClientAdvancements(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void update(ClientboundUpdateAdvancementsPacket clientboundUpdateAdvancementsPacket) {
        if (clientboundUpdateAdvancementsPacket.shouldReset()) {
            this.advancements.clear();
            this.progress.clear();
        }
        this.advancements.remove(clientboundUpdateAdvancementsPacket.getRemoved());
        this.advancements.add(clientboundUpdateAdvancementsPacket.getAdded());
        for (Map.Entry<ResourceLocation, AdvancementProgress> entry : clientboundUpdateAdvancementsPacket.getProgress().entrySet()) {
            Advancement advancement = this.advancements.get(entry.getKey());
            if (advancement != null) {
                AdvancementProgress advancementProgress = entry.getValue();
                advancementProgress.update(advancement.getCriteria(), advancement.getRequirements());
                this.progress.put(advancement, advancementProgress);
                if (this.listener != null) {
                    this.listener.onUpdateAdvancementProgress(advancement, advancementProgress);
                }
                if (clientboundUpdateAdvancementsPacket.shouldReset() || !advancementProgress.isDone() || advancement.getDisplay() == null || !advancement.getDisplay().shouldShowToast()) continue;
                this.minecraft.getToasts().addToast(new AdvancementToast(advancement));
                continue;
            }
            LOGGER.warn("Server informed client about progress for unknown advancement {}", (Object)entry.getKey());
        }
    }

    public AdvancementList getAdvancements() {
        return this.advancements;
    }

    public void setSelectedTab(@Nullable Advancement advancement, boolean bl) {
        ClientPacketListener clientPacketListener = this.minecraft.getConnection();
        if (clientPacketListener != null && advancement != null && bl) {
            clientPacketListener.send(ServerboundSeenAdvancementsPacket.openedTab(advancement));
        }
        if (this.selectedTab != advancement) {
            this.selectedTab = advancement;
            if (this.listener != null) {
                this.listener.onSelectedTabChanged(advancement);
            }
        }
    }

    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
        this.advancements.setListener(listener);
        if (listener != null) {
            for (Map.Entry<Advancement, AdvancementProgress> entry : this.progress.entrySet()) {
                listener.onUpdateAdvancementProgress(entry.getKey(), entry.getValue());
            }
            listener.onSelectedTabChanged(this.selectedTab);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static interface Listener
    extends AdvancementList.Listener {
        public void onUpdateAdvancementProgress(Advancement var1, AdvancementProgress var2);

        public void onSelectedTabChanged(@Nullable Advancement var1);
    }
}

