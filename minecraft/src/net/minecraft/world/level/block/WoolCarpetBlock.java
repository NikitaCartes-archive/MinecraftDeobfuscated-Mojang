package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class WoolCarpetBlock extends CarpetBlock implements Equipable {
	public static final MapCodec<WoolCarpetBlock> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(DyeColor.CODEC.fieldOf("color").forGetter(WoolCarpetBlock::getColor), propertiesCodec()).apply(instance, WoolCarpetBlock::new)
	);
	private final DyeColor color;

	@Override
	public MapCodec<WoolCarpetBlock> codec() {
		return CODEC;
	}

	protected WoolCarpetBlock(DyeColor dyeColor, BlockBehaviour.Properties properties) {
		super(properties);
		this.color = dyeColor;
	}

	public DyeColor getColor() {
		return this.color;
	}

	@Override
	public EquipmentSlot getEquipmentSlot() {
		return EquipmentSlot.BODY;
	}

	@Override
	public Holder<SoundEvent> getEquipSound() {
		return SoundEvents.LLAMA_SWAG;
	}
}
