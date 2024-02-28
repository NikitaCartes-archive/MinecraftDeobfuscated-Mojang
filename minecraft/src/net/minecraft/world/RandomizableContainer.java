package net.minecraft.world;

import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public interface RandomizableContainer extends Container {
	String LOOT_TABLE_TAG = "LootTable";
	String LOOT_TABLE_SEED_TAG = "LootTableSeed";

	@Nullable
	ResourceLocation getLootTable();

	void setLootTable(@Nullable ResourceLocation resourceLocation);

	default void setLootTable(ResourceLocation resourceLocation, long l) {
		this.setLootTable(resourceLocation);
		this.setLootTableSeed(l);
	}

	long getLootTableSeed();

	void setLootTableSeed(long l);

	BlockPos getBlockPos();

	@Nullable
	Level getLevel();

	static void setBlockEntityLootTable(BlockGetter blockGetter, RandomSource randomSource, BlockPos blockPos, ResourceLocation resourceLocation) {
		if (blockGetter.getBlockEntity(blockPos) instanceof RandomizableContainer randomizableContainer) {
			randomizableContainer.setLootTable(resourceLocation, randomSource.nextLong());
		}
	}

	default boolean tryLoadLootTable(CompoundTag compoundTag) {
		if (compoundTag.contains("LootTable", 8)) {
			this.setLootTable(new ResourceLocation(compoundTag.getString("LootTable")));
			if (compoundTag.contains("LootTableSeed", 4)) {
				this.setLootTableSeed(compoundTag.getLong("LootTableSeed"));
			} else {
				this.setLootTableSeed(0L);
			}

			return true;
		} else {
			return false;
		}
	}

	default boolean trySaveLootTable(CompoundTag compoundTag) {
		ResourceLocation resourceLocation = this.getLootTable();
		if (resourceLocation == null) {
			return false;
		} else {
			compoundTag.putString("LootTable", resourceLocation.toString());
			long l = this.getLootTableSeed();
			if (l != 0L) {
				compoundTag.putLong("LootTableSeed", l);
			}

			return true;
		}
	}

	default void unpackLootTable(@Nullable Player player) {
		Level level = this.getLevel();
		BlockPos blockPos = this.getBlockPos();
		ResourceLocation resourceLocation = this.getLootTable();
		if (resourceLocation != null && level != null && level.getServer() != null) {
			LootTable lootTable = level.getServer().getLootData().getLootTable(resourceLocation);
			if (player instanceof ServerPlayer) {
				CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayer)player, resourceLocation);
			}

			this.setLootTable(null);
			LootParams.Builder builder = new LootParams.Builder((ServerLevel)level).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos));
			if (player != null) {
				builder.withLuck(player.getLuck()).withParameter(LootContextParams.THIS_ENTITY, player);
			}

			lootTable.fill(this, builder.create(LootContextParamSets.CHEST), this.getLootTableSeed());
		}
	}
}
