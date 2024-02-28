package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FlowerBlock extends BushBlock implements SuspiciousEffectHolder {
	protected static final MapCodec<SuspiciousStewEffects> EFFECTS_FIELD = SuspiciousStewEffects.CODEC.fieldOf("suspicious_stew_effects");
	public static final MapCodec<FlowerBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(EFFECTS_FIELD.forGetter(FlowerBlock::getSuspiciousEffects), propertiesCodec()).apply(instance, FlowerBlock::new)
	);
	protected static final float AABB_OFFSET = 3.0F;
	protected static final VoxelShape SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 10.0, 11.0);
	private final SuspiciousStewEffects suspiciousStewEffects;

	@Override
	public MapCodec<? extends FlowerBlock> codec() {
		return CODEC;
	}

	public FlowerBlock(Holder<MobEffect> holder, int i, BlockBehaviour.Properties properties) {
		this(makeEffectList(holder, i), properties);
	}

	public FlowerBlock(SuspiciousStewEffects suspiciousStewEffects, BlockBehaviour.Properties properties) {
		super(properties);
		this.suspiciousStewEffects = suspiciousStewEffects;
	}

	protected static SuspiciousStewEffects makeEffectList(Holder<MobEffect> holder, int i) {
		return new SuspiciousStewEffects(List.of(new SuspiciousStewEffects.Entry(holder, i * 20)));
	}

	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		Vec3 vec3 = blockState.getOffset(blockGetter, blockPos);
		return SHAPE.move(vec3.x, vec3.y, vec3.z);
	}

	@Override
	public SuspiciousStewEffects getSuspiciousEffects() {
		return this.suspiciousStewEffects;
	}
}
