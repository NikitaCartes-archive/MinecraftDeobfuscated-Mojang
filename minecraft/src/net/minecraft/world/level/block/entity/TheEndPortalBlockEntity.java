package net.minecraft.world.level.block.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;

public class TheEndPortalBlockEntity extends BlockEntity {
	public TheEndPortalBlockEntity(BlockEntityType<?> blockEntityType) {
		super(blockEntityType);
	}

	public TheEndPortalBlockEntity() {
		this(BlockEntityType.END_PORTAL);
	}

	@Environment(EnvType.CLIENT)
	public boolean shouldRenderFace(Direction direction) {
		return direction == Direction.UP;
	}
}
