package net.minecraft.client.multiplayer;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.advancements.DisplayInfo;
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
	private final AdvancementTree tree = new AdvancementTree();
	private final Map<AdvancementHolder, AdvancementProgress> progress = new Object2ObjectOpenHashMap<>();
	@Nullable
	private ClientAdvancements.Listener listener;
	@Nullable
	private AdvancementHolder selectedTab;

	public ClientAdvancements(Minecraft minecraft, WorldSessionTelemetryManager worldSessionTelemetryManager) {
		this.minecraft = minecraft;
		this.telemetryManager = worldSessionTelemetryManager;
	}

	public void update(ClientboundUpdateAdvancementsPacket clientboundUpdateAdvancementsPacket) {
		if (clientboundUpdateAdvancementsPacket.shouldReset()) {
			this.tree.clear();
			this.progress.clear();
		}

		this.tree.remove(clientboundUpdateAdvancementsPacket.getRemoved());
		this.tree.addAll(clientboundUpdateAdvancementsPacket.getAdded());

		for (Entry<ResourceLocation, AdvancementProgress> entry : clientboundUpdateAdvancementsPacket.getProgress().entrySet()) {
			AdvancementNode advancementNode = this.tree.get((ResourceLocation)entry.getKey());
			if (advancementNode != null) {
				AdvancementProgress advancementProgress = (AdvancementProgress)entry.getValue();
				advancementProgress.update(advancementNode.advancement().requirements());
				this.progress.put(advancementNode.holder(), advancementProgress);
				if (this.listener != null) {
					this.listener.onUpdateAdvancementProgress(advancementNode, advancementProgress);
				}

				if (!clientboundUpdateAdvancementsPacket.shouldReset() && advancementProgress.isDone()) {
					if (this.minecraft.level != null) {
						this.telemetryManager.onAdvancementDone(this.minecraft.level, advancementNode.holder());
					}

					Optional<DisplayInfo> optional = advancementNode.advancement().display();
					if (optional.isPresent() && ((DisplayInfo)optional.get()).shouldShowToast()) {
						this.minecraft.getToasts().addToast(new AdvancementToast(advancementNode.holder()));
					}
				}
			} else {
				LOGGER.warn("Server informed client about progress for unknown advancement {}", entry.getKey());
			}
		}
	}

	public AdvancementTree getTree() {
		return this.tree;
	}

	public void setSelectedTab(@Nullable AdvancementHolder advancementHolder, boolean bl) {
		ClientPacketListener clientPacketListener = this.minecraft.getConnection();
		if (clientPacketListener != null && advancementHolder != null && bl) {
			clientPacketListener.send(ServerboundSeenAdvancementsPacket.openedTab(advancementHolder));
		}

		if (this.selectedTab != advancementHolder) {
			this.selectedTab = advancementHolder;
			if (this.listener != null) {
				this.listener.onSelectedTabChanged(advancementHolder);
			}
		}
	}

	public void setListener(@Nullable ClientAdvancements.Listener listener) {
		this.listener = listener;
		this.tree.setListener(listener);
		if (listener != null) {
			this.progress.forEach((advancementHolder, advancementProgress) -> {
				AdvancementNode advancementNode = this.tree.get(advancementHolder);
				if (advancementNode != null) {
					listener.onUpdateAdvancementProgress(advancementNode, advancementProgress);
				}
			});
			listener.onSelectedTabChanged(this.selectedTab);
		}
	}

	@Nullable
	public AdvancementHolder get(ResourceLocation resourceLocation) {
		AdvancementNode advancementNode = this.tree.get(resourceLocation);
		return advancementNode != null ? advancementNode.holder() : null;
	}

	@Environment(EnvType.CLIENT)
	public interface Listener extends AdvancementTree.Listener {
		void onUpdateAdvancementProgress(AdvancementNode advancementNode, AdvancementProgress advancementProgress);

		void onSelectedTabChanged(@Nullable AdvancementHolder advancementHolder);
	}
}
