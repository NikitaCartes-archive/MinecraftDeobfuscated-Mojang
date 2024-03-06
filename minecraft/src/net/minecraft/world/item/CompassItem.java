package net.minecraft.world.item;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class CompassItem extends Item {
	public CompassItem(Item.Properties properties) {
		super(properties);
	}

	@Nullable
	public static GlobalPos getSpawnPosition(Level level) {
		return level.dimensionType().natural() ? GlobalPos.of(level.dimension(), level.getSharedSpawnPos()) : null;
	}

	@Override
	public boolean isFoil(ItemStack itemStack) {
		return itemStack.has(DataComponents.LODESTONE_TRACKER) || super.isFoil(itemStack);
	}

	@Override
	public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int i, boolean bl) {
		if (level instanceof ServerLevel serverLevel) {
			LodestoneTracker lodestoneTracker = itemStack.get(DataComponents.LODESTONE_TRACKER);
			if (lodestoneTracker != null) {
				LodestoneTracker lodestoneTracker2 = lodestoneTracker.tick(serverLevel);
				if (lodestoneTracker2 != lodestoneTracker) {
					itemStack.set(DataComponents.LODESTONE_TRACKER, lodestoneTracker2);
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
			LodestoneTracker lodestoneTracker = new LodestoneTracker(Optional.of(GlobalPos.of(level.dimension(), blockPos)), true);
			if (bl) {
				itemStack.set(DataComponents.LODESTONE_TRACKER, lodestoneTracker);
			} else {
				ItemStack itemStack2 = itemStack.transmuteCopy(Items.COMPASS, 1);
				itemStack.consume(1, player);
				itemStack2.set(DataComponents.LODESTONE_TRACKER, lodestoneTracker);
				if (!player.getInventory().add(itemStack2)) {
					player.drop(itemStack2, false);
				}
			}

			return InteractionResult.sidedSuccess(level.isClientSide);
		}
	}

	@Override
	public String getDescriptionId(ItemStack itemStack) {
		return itemStack.has(DataComponents.LODESTONE_TRACKER) ? "item.minecraft.lodestone_compass" : super.getDescriptionId(itemStack);
	}
}
