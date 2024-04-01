package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.component.XpComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class BigBrainBlockEntity extends BlockEntity {
	public static final String TAG_AMOUNT = "amount";
	private static final double ORB_TARGET_DISTANCE = 10.0;
	private static final int TICK_RATE = 5;
	private static final String TAG_TICK_DELAY = "delay";
	private int tickDelay = 5;
	private int xpAmount;

	public BigBrainBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityType.BIG_BRAIN, blockPos, blockState);
	}

	@Override
	public void load(CompoundTag compoundTag, HolderLookup.Provider provider) {
		this.xpAmount = compoundTag.getInt("amount");
		this.tickDelay = compoundTag.getInt("delay");
	}

	@Override
	protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
		compoundTag.putInt("amount", this.xpAmount);
		compoundTag.putInt("delay", this.tickDelay);
	}

	public int getXp() {
		return this.xpAmount;
	}

	public void setXp(int i) {
		this.xpAmount = i;
	}

	@Override
	public void collectComponents(DataComponentMap.Builder builder) {
		super.collectComponents(builder);
		builder.set(DataComponents.XP, new XpComponent(this.xpAmount));
	}

	@Override
	public void removeComponentsFromTag(CompoundTag compoundTag) {
		compoundTag.remove("amount");
	}

	public static void tick(Level level, BlockPos blockPos, BlockState blockState, BigBrainBlockEntity bigBrainBlockEntity) {
		if (--bigBrainBlockEntity.tickDelay <= 0) {
			bigBrainBlockEntity.tickDelay = 5;
			AABB aABB = AABB.ofSize(Vec3.atCenterOf(blockPos), 10.0, 10.0, 10.0);

			for (ExperienceOrb experienceOrb : level.getEntities(EntityTypeTest.forClass(ExperienceOrb.class), aABB, experienceOrbx -> true)) {
				experienceOrb.targetBlock(blockPos);
			}
		}
	}
}
