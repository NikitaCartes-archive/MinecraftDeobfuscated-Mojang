package net.minecraft.client.multiplayer;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.telemetry.WorldSessionTelemetryManager;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class ClientAdvancements {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Minecraft minecraft;
	private final WorldSessionTelemetryManager telemetryManager;
	private final AdvancementList advancements = new AdvancementList();
	private final Map<Advancement, AdvancementProgress> progress = Maps.<Advancement, AdvancementProgress>newHashMap();
	@Nullable
	private ClientAdvancements.Listener listener;
	@Nullable
	private Advancement selectedTab;

	public ClientAdvancements(Minecraft minecraft, WorldSessionTelemetryManager worldSessionTelemetryManager) {
		this.minecraft = minecraft;
		this.telemetryManager = worldSessionTelemetryManager;
	}

	public void update(ClientboundUpdateAdvancementsPacket clientboundUpdateAdvancementsPacket) {
		if (clientboundUpdateAdvancementsPacket.shouldReset()) {
			this.advancements.clear();
			this.progress.clear();
		}

		this.advancements.remove(clientboundUpdateAdvancementsPacket.getRemoved());
		this.advancements.add(clientboundUpdateAdvancementsPacket.getAdded());

		for (Entry<ResourceLocation, AdvancementProgress> entry : clientboundUpdateAdvancementsPacket.getProgress().entrySet()) {
			Advancement advancement = this.advancements.get((ResourceLocation)entry.getKey());
			if (advancement != null) {
				AdvancementProgress advancementProgress = (AdvancementProgress)entry.getValue();
				advancementProgress.update(advancement.getCriteria(), advancement.getRequirements());
				this.progress.put(advancement, advancementProgress);
				if (this.listener != null) {
					this.listener.onUpdateAdvancementProgress(advancement, advancementProgress);
				}

				if (!clientboundUpdateAdvancementsPacket.shouldReset() && advancementProgress.isDone()) {
					if (this.minecraft.level != null) {
						this.telemetryManager.onAdvancementDone(this.minecraft.level, advancement);
					}

					if (advancement.getDisplay() != null && advancement.getDisplay().shouldShowToast()) {
						this.minecraft.getToasts().addToast(new AdvancementToast(advancement));
					}
				}
			} else {
				LOGGER.warn("Server informed client about progress for unknown advancement {}", entry.getKey());
			}
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

	public void setListener(@Nullable ClientAdvancements.Listener listener) {
		this.listener = listener;
		this.advancements.setListener(listener);
		if (listener != null) {
			for (Entry<Advancement, AdvancementProgress> entry : this.progress.entrySet()) {
				listener.onUpdateAdvancementProgress((Advancement)entry.getKey(), (AdvancementProgress)entry.getValue());
			}

			listener.onSelectedTabChanged(this.selectedTab);
		}
	}

	@Environment(EnvType.CLIENT)
	public interface Listener extends AdvancementList.Listener {
		void onUpdateAdvancementProgress(Advancement advancement, AdvancementProgress advancementProgress);

		void onSelectedTabChanged(@Nullable Advancement advancement);
	}
}
