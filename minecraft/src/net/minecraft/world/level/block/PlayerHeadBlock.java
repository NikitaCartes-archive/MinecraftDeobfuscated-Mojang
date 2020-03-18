package net.minecraft.world.level.block;

import com.mojang.authlib.GameProfile;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.StringUtils;

public class PlayerHeadBlock extends SkullBlock {
	protected PlayerHeadBlock(BlockBehaviour.Properties properties) {
		super(SkullBlock.Types.PLAYER, properties);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
		super.setPlacedBy(level, blockPos, blockState, livingEntity, itemStack);
		BlockEntity blockEntity = level.getBlockEntity(blockPos);
		if (blockEntity instanceof SkullBlockEntity) {
			SkullBlockEntity skullBlockEntity = (SkullBlockEntity)blockEntity;
			GameProfile gameProfile = null;
			if (itemStack.hasTag()) {
				CompoundTag compoundTag = itemStack.getTag();
				if (compoundTag.contains("SkullOwner", 10)) {
					gameProfile = NbtUtils.readGameProfile(compoundTag.getCompound("SkullOwner"));
				} else if (compoundTag.contains("SkullOwner", 8) && !StringUtils.isBlank(compoundTag.getString("SkullOwner"))) {
					gameProfile = new GameProfile(null, compoundTag.getString("SkullOwner"));
				}
			}

			skullBlockEntity.setOwner(gameProfile);
		}
	}
}
