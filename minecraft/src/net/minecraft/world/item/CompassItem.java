package net.minecraft.world.item;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CompassItem extends Item implements Vanishable {
	private static final Logger LOGGER = LogManager.getLogger();
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

	@Override
	public boolean isFoil(ItemStack itemStack) {
		return isLodestoneCompass(itemStack) || super.isFoil(itemStack);
	}

	public static Optional<ResourceKey<Level>> getLodestoneDimension(CompoundTag compoundTag) {
		return Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, compoundTag.get("LodestoneDimension")).result();
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
					BlockPos blockPos = NbtUtils.readBlockPos(compoundTag.getCompound("LodestonePos"));
					if (!level.isInWorldBounds(blockPos) || !((ServerLevel)level).getPoiManager().existsAtPosition(PoiType.LODESTONE, blockPos)) {
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
			boolean bl = !player.getAbilities().instabuild && itemStack.getCount() == 1;
			if (bl) {
				this.addLodestoneTags(level.dimension(), blockPos, itemStack.getOrCreateTag());
			} else {
				ItemStack itemStack2 = new ItemStack(Items.COMPASS, 1);
				CompoundTag compoundTag = itemStack.hasTag() ? itemStack.getTag().copy() : new CompoundTag();
				itemStack2.setTag(compoundTag);
				if (!player.getAbilities().instabuild) {
					itemStack.shrink(1);
				}

				this.addLodestoneTags(level.dimension(), blockPos, compoundTag);
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
