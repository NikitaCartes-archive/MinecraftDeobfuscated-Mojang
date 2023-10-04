package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class EquipableCarvedPumpkinBlock extends CarvedPumpkinBlock implements Equipable {
	public static final MapCodec<EquipableCarvedPumpkinBlock> CODEC = simpleCodec(EquipableCarvedPumpkinBlock::new);

	@Override
	public MapCodec<EquipableCarvedPumpkinBlock> codec() {
		return CODEC;
	}

	protected EquipableCarvedPumpkinBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public EquipmentSlot getEquipmentSlot() {
		return EquipmentSlot.HEAD;
	}
}
