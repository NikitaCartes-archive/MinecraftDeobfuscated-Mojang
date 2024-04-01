package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class EquipablePotatoZombieHeadBlock extends PotatoZombieHeadBlock implements Equipable {
	public static final MapCodec<EquipablePotatoZombieHeadBlock> CODEC = simpleCodec(EquipablePotatoZombieHeadBlock::new);

	@Override
	public MapCodec<EquipablePotatoZombieHeadBlock> codec() {
		return CODEC;
	}

	protected EquipablePotatoZombieHeadBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public EquipmentSlot getEquipmentSlot() {
		return EquipmentSlot.HEAD;
	}
}
