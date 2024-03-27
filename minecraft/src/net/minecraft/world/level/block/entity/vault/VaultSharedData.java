package net.minecraft.world.level.block.entity.vault;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

public class VaultSharedData {
	static final String TAG_NAME = "shared_data";
	static Codec<VaultSharedData> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ItemStack.lenientOptionalFieldOf("display_item").forGetter(vaultSharedData -> vaultSharedData.displayItem),
					UUIDUtil.CODEC_LINKED_SET.lenientOptionalFieldOf("connected_players", Set.of()).forGetter(vaultSharedData -> vaultSharedData.connectedPlayers),
					Codec.DOUBLE
						.lenientOptionalFieldOf("connected_particles_range", Double.valueOf(VaultConfig.DEFAULT.deactivationRange()))
						.forGetter(vaultSharedData -> vaultSharedData.connectedParticlesRange)
				)
				.apply(instance, VaultSharedData::new)
	);
	private ItemStack displayItem = ItemStack.EMPTY;
	private Set<UUID> connectedPlayers = new ObjectLinkedOpenHashSet<>();
	private double connectedParticlesRange = VaultConfig.DEFAULT.deactivationRange();
	boolean isDirty;

	VaultSharedData(ItemStack itemStack, Set<UUID> set, double d) {
		this.displayItem = itemStack;
		this.connectedPlayers.addAll(set);
		this.connectedParticlesRange = d;
	}

	VaultSharedData() {
	}

	public ItemStack getDisplayItem() {
		return this.displayItem;
	}

	public boolean hasDisplayItem() {
		return !this.displayItem.isEmpty();
	}

	public void setDisplayItem(ItemStack itemStack) {
		if (!ItemStack.matches(this.displayItem, itemStack)) {
			this.displayItem = itemStack.copy();
			this.markDirty();
		}
	}

	boolean hasConnectedPlayers() {
		return !this.connectedPlayers.isEmpty();
	}

	Set<UUID> getConnectedPlayers() {
		return this.connectedPlayers;
	}

	double connectedParticlesRange() {
		return this.connectedParticlesRange;
	}

	void updateConnectedPlayersWithinRange(ServerLevel serverLevel, BlockPos blockPos, VaultServerData vaultServerData, VaultConfig vaultConfig, double d) {
		Set<UUID> set = (Set<UUID>)vaultConfig.playerDetector()
			.detect(serverLevel, vaultConfig.entitySelector(), blockPos, d, false)
			.stream()
			.filter(uUID -> !vaultServerData.getRewardedPlayers().contains(uUID))
			.collect(Collectors.toSet());
		if (!this.connectedPlayers.equals(set)) {
			this.connectedPlayers = set;
			this.markDirty();
		}
	}

	private void markDirty() {
		this.isDirty = true;
	}

	void set(VaultSharedData vaultSharedData) {
		this.displayItem = vaultSharedData.displayItem;
		this.connectedPlayers = vaultSharedData.connectedPlayers;
		this.connectedParticlesRange = vaultSharedData.connectedParticlesRange;
	}
}
