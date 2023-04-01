package net.minecraft.world.item;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.BlockHitResult;

public interface DispensibleContainerItem {
	default void checkExtraContent(@Nullable Player player, Level level, ItemStack itemStack, BlockPos blockPos) {
	}

	boolean emptyContents(@Nullable Player player, Level level, BlockPos blockPos, @Nullable BlockHitResult blockHitResult);

	static void tryPlacePortal(Level level, BlockPos blockPos) {
		Optional<PortalShape> optional = PortalShape.findEmptyPortalShape(level, blockPos, Direction.Axis.X, true);
		optional.ifPresent(PortalShape::createPortalBlocks);
	}
}
