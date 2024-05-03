package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public record SetBlockProperties(BlockItemStateProperties properties, Vec3i offset) implements EnchantmentEntityEffect {
	public static final MapCodec<SetBlockProperties> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					BlockItemStateProperties.CODEC.fieldOf("properties").forGetter(SetBlockProperties::properties),
					Vec3i.CODEC.optionalFieldOf("offset", Vec3i.ZERO).forGetter(SetBlockProperties::offset)
				)
				.apply(instance, SetBlockProperties::new)
	);

	public SetBlockProperties(BlockItemStateProperties blockItemStateProperties) {
		this(blockItemStateProperties, Vec3i.ZERO);
	}

	@Override
	public void apply(ServerLevel serverLevel, int i, EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3) {
		BlockPos blockPos = BlockPos.containing(vec3).offset(this.offset);
		BlockState blockState = entity.level().getBlockState(blockPos);
		BlockState blockState2 = this.properties.apply(blockState);
		if (!blockState.equals(blockState2)) {
			entity.level().setBlock(blockPos, blockState2, 3);
		}
	}

	@Override
	public MapCodec<SetBlockProperties> codec() {
		return CODEC;
	}
}
