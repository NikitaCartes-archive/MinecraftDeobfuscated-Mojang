package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class BrushableBlockEntity extends BlockEntity {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String LOOT_TABLE_TAG = "LootTable";
	private static final String LOOT_TABLE_SEED_TAG = "LootTableSeed";
	private static final String HIT_DIRECTION_TAG = "hit_direction";
	private static final String ITEM_TAG = "item";
	private static final int BRUSH_COOLDOWN_TICKS = 10;
	private static final int BRUSH_RESET_TICKS = 40;
	private static final int REQUIRED_BRUSHES_TO_BREAK = 10;
	private int brushCount;
	private long brushCountResetsAtTick;
	private long coolDownEndsAtTick;
	private ItemStack item = ItemStack.EMPTY;
	@Nullable
	private Direction hitDirection;
	@Nullable
	private ResourceKey<LootTable> lootTable;
	private long lootTableSeed;

	public BrushableBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.BRUSHABLE_BLOCK, blockPos, blockState);
	}

	public boolean brush(long l, Player player, Direction direction) {
		if (this.hitDirection == null) {
			this.hitDirection = direction;
		}

		this.brushCountResetsAtTick = l + 40L;
		if (l >= this.coolDownEndsAtTick && this.level instanceof ServerLevel) {
			this.coolDownEndsAtTick = l + 10L;
			this.unpackLootTable(player);
			int i = this.getCompletionState();
			if (++this.brushCount >= 10) {
				this.brushingCompleted(player);
				return true;
			} else {
				this.level.scheduleTick(this.getBlockPos(), this.getBlockState().getBlock(), 2);
				int j = this.getCompletionState();
				if (i != j) {
					BlockState blockState = this.getBlockState();
					BlockState blockState2 = blockState.setValue(BlockStateProperties.DUSTED, Integer.valueOf(j));
					this.level.setBlock(this.getBlockPos(), blockState2, 3);
				}

				return false;
			}
		} else {
			return false;
		}
	}

	public void unpackLootTable(Player player) {
		if (this.lootTable != null && this.level != null && !this.level.isClientSide() && this.level.getServer() != null) {
			LootTable lootTable = this.level.getServer().reloadableRegistries().getLootTable(this.lootTable);
			if (player instanceof ServerPlayer serverPlayer) {
				CriteriaTriggers.GENERATE_LOOT.trigger(serverPlayer, this.lootTable);
			}

			LootParams lootParams = new LootParams.Builder((ServerLevel)this.level)
				.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.worldPosition))
				.withLuck(player.getLuck())
				.withParameter(LootContextParams.THIS_ENTITY, player)
				.create(LootContextParamSets.CHEST);
			ObjectArrayList<ItemStack> objectArrayList = lootTable.getRandomItems(lootParams, this.lootTableSeed);

			this.item = switch (objectArrayList.size()) {
				case 0 -> ItemStack.EMPTY;
				case 1 -> (ItemStack)objectArrayList.get(0);
				default -> {
					LOGGER.warn("Expected max 1 loot from loot table {}, but got {}", this.lootTable.location(), objectArrayList.size());
					yield objectArrayList.get(0);
				}
			};
			this.lootTable = null;
			this.setChanged();
		}
	}

	private void brushingCompleted(Player player) {
		if (this.level != null && this.level.getServer() != null) {
			this.dropContent(player);
			BlockState blockState = this.getBlockState();
			this.level.levelEvent(3008, this.getBlockPos(), Block.getId(blockState));
			Block block2;
			if (this.getBlockState().getBlock() instanceof BrushableBlock brushableBlock) {
				block2 = brushableBlock.getTurnsInto();
			} else {
				block2 = Blocks.AIR;
			}

			this.level.setBlock(this.worldPosition, block2.defaultBlockState(), 3);
		}
	}

	private void dropContent(Player player) {
		if (this.level != null && this.level.getServer() != null) {
			this.unpackLootTable(player);
			if (!this.item.isEmpty()) {
				double d = (double)EntityType.ITEM.getWidth();
				double e = 1.0 - d;
				double f = d / 2.0;
				Direction direction = (Direction)Objects.requireNonNullElse(this.hitDirection, Direction.UP);
				BlockPos blockPos = this.worldPosition.relative(direction, 1);
				double g = (double)blockPos.getX() + 0.5 * e + f;
				double h = (double)blockPos.getY() + 0.5 + (double)(EntityType.ITEM.getHeight() / 2.0F);
				double i = (double)blockPos.getZ() + 0.5 * e + f;
				ItemEntity itemEntity = new ItemEntity(this.level, g, h, i, this.item.split(this.level.random.nextInt(21) + 10));
				itemEntity.setDeltaMovement(Vec3.ZERO);
				this.level.addFreshEntity(itemEntity);
				this.item = ItemStack.EMPTY;
			}
		}
	}

	public void checkReset() {
		if (this.level != null) {
			if (this.brushCount != 0 && this.level.getGameTime() >= this.brushCountResetsAtTick) {
				int i = this.getCompletionState();
				this.brushCount = Math.max(0, this.brushCount - 2);
				int j = this.getCompletionState();
				if (i != j) {
					this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(BlockStateProperties.DUSTED, Integer.valueOf(j)), 3);
				}

				int k = 4;
				this.brushCountResetsAtTick = this.level.getGameTime() + 4L;
			}

			if (this.brushCount == 0) {
				this.hitDirection = null;
				this.brushCountResetsAtTick = 0L;
				this.coolDownEndsAtTick = 0L;
			} else {
				this.level.scheduleTick(this.getBlockPos(), this.getBlockState().getBlock(), 2);
			}
		}
	}

	private boolean tryLoadLootTable(CompoundTag compoundTag) {
		if (compoundTag.contains("LootTable", 8)) {
			this.lootTable = ResourceKey.create(Registries.LOOT_TABLE, new ResourceLocation(compoundTag.getString("LootTable")));
			this.lootTableSeed = compoundTag.getLong("LootTableSeed");
			return true;
		} else {
			return false;
		}
	}

	private boolean trySaveLootTable(CompoundTag compoundTag) {
		if (this.lootTable == null) {
			return false;
		} else {
			compoundTag.putString("LootTable", this.lootTable.location().toString());
			if (this.lootTableSeed != 0L) {
				compoundTag.putLong("LootTableSeed", this.lootTableSeed);
			}

			return true;
		}
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
		CompoundTag compoundTag = super.getUpdateTag(provider);
		if (this.hitDirection != null) {
			compoundTag.putInt("hit_direction", this.hitDirection.ordinal());
		}

		if (!this.item.isEmpty()) {
			compoundTag.put("item", this.item.save(provider));
		}

		return compoundTag;
	}

	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.loadAdditional(compoundTag, provider);
		if (!this.tryLoadLootTable(compoundTag) && compoundTag.contains("item")) {
			this.item = (ItemStack)ItemStack.parse(provider, compoundTag.getCompound("item")).orElse(ItemStack.EMPTY);
		} else {
			this.item = ItemStack.EMPTY;
		}

		if (compoundTag.contains("hit_direction")) {
			this.hitDirection = Direction.values()[compoundTag.getInt("hit_direction")];
		}
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		super.saveAdditional(compoundTag, provider);
		if (!this.trySaveLootTable(compoundTag) && !this.item.isEmpty()) {
			compoundTag.put("item", this.item.save(provider));
		}
	}

	public void setLootTable(ResourceKey<LootTable> resourceKey, long l) {
		this.lootTable = resourceKey;
		this.lootTableSeed = l;
	}

	private int getCompletionState() {
		if (this.brushCount == 0) {
			return 0;
		} else if (this.brushCount < 3) {
			return 1;
		} else {
			return this.brushCount < 6 ? 2 : 3;
		}
	}

	@Nullable
	public Direction getHitDirection() {
		return this.hitDirection;
	}

	public ItemStack getItem() {
		return this.item;
	}
}
