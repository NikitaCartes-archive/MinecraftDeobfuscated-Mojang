package net.minecraft.world.item;

import com.mojang.logging.LogUtils;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.Logger;

public class CompassItem extends Item {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final String TAG_LODESTONE_POS = "LodestonePos";
	public static final String TAG_LODESTONE_DIMENSION = "LodestoneDimension";
	public static final String TAG_LODESTONE_TRACKED = "LodestoneTracked";

	public CompassItem(Item.Properties properties) {
		super(properties);
	}

	public static boolean isLodestoneCompass(ItemStack itemStack) {
		CompoundTag compoundTag = itemStack.getTag();
		return compoundTag != null && (compoundTag.contains("LodestoneDimension") || compoundTag.contains("LodestonePos"));
	}

	private static Optional<ResourceKey<Level>> getLodestoneDimension(CompoundTag compoundTag) {
		return Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, compoundTag.get("LodestoneDimension")).result();
	}

	@Nullable
	public static GlobalPos getLodestonePosition(CompoundTag compoundTag) {
		boolean bl = compoundTag.contains("LodestonePos");
		boolean bl2 = compoundTag.contains("LodestoneDimension");
		if (bl && bl2) {
			Optional<ResourceKey<Level>> optional = getLodestoneDimension(compoundTag);
			if (optional.isPresent()) {
				Optional<BlockPos> optional2 = NbtUtils.readBlockPos(compoundTag, "LodestonePos");
				if (optional2.isPresent()) {
					return GlobalPos.of((ResourceKey<Level>)optional.get(), (BlockPos)optional2.get());
				}
			}
		}

		return null;
	}

	@Nullable
	public static GlobalPos getSpawnPosition(Level level) {
		return level.dimensionType().natural() ? GlobalPos.of(level.dimension(), level.getSharedSpawnPos()) : null;
	}

	@Override
	public boolean isFoil(ItemStack itemStack) {
		return isLodestoneCompass(itemStack) || super.isFoil(itemStack);
	}

	@Override
	public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int i, boolean bl) {
		if (!level.isClientSide) {
			if (isLodestoneCompass(itemStack)) {
				CompoundTag compoundTag = itemStack.getOrCreateTag();
				if (compoundTag.contains("LodestoneTracked") && !compoundTag.getBoolean("LodestoneTracked")) {
					return;
				}

				Optional<ResourceKey<Level>> optional = getLodestoneDimension(compoundTag);
				if (optional.isPresent() && optional.get() == level.dimension() && compoundTag.contains("LodestonePos")) {
					Optional<BlockPos> optional2 = NbtUtils.readBlockPos(compoundTag, "LodestonePos");
					if (optional2.isEmpty()
						|| !level.isInWorldBounds((BlockPos)optional2.get())
						|| !((ServerLevel)level).getPoiManager().existsAtPosition(PoiTypes.LODESTONE, (BlockPos)optional2.get())) {
						compoundTag.remove("LodestonePos");
					}
				}
			}
		}
	}

	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		BlockPos blockPos = useOnContext.getClickedPos();
		Level level = useOnContext.getLevel();
		if (!level.getBlockState(blockPos).is(Blocks.LODESTONE)) {
			return super.useOn(useOnContext);
		} else {
			level.playSound(null, blockPos, SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 1.0F, 1.0F);
			Player player = useOnContext.getPlayer();
			ItemStack itemStack = useOnContext.getItemInHand();
			boolean bl = !player.hasInfiniteMaterials() && itemStack.getCount() == 1;
			if (bl) {
				this.addLodestoneTags(level.dimension(), blockPos, itemStack.getOrCreateTag());
			} else {
				ItemStack itemStack2 = itemStack.transmuteCopy(Items.COMPASS, 1);
				itemStack.consume(1, player);
				this.addLodestoneTags(level.dimension(), blockPos, itemStack2.getOrCreateTag());
				if (!player.getInventory().add(itemStack2)) {
					player.drop(itemStack2, false);
				}
			}

			return InteractionResult.sidedSuccess(level.isClientSide);
		}
	}

	private void addLodestoneTags(ResourceKey<Level> resourceKey, BlockPos blockPos, CompoundTag compoundTag) {
		compoundTag.put("LodestonePos", NbtUtils.writeBlockPos(blockPos));
		Level.RESOURCE_KEY_CODEC
			.encodeStart(NbtOps.INSTANCE, resourceKey)
			.resultOrPartial(LOGGER::error)
			.ifPresent(tag -> compoundTag.put("LodestoneDimension", tag));
		compoundTag.putBoolean("LodestoneTracked", true);
	}

	@Override
	public String getDescriptionId(ItemStack itemStack) {
		return isLodestoneCompass(itemStack) ? "item.minecraft.lodestone_compass" : super.getDescriptionId(itemStack);
	}
}
