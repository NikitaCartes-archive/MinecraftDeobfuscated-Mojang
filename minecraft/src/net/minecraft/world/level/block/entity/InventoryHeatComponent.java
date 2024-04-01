package net.minecraft.world.level.block.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record InventoryHeatComponent(@Nullable UUID owner, int slot, int heat) {
	public static final Codec<InventoryHeatComponent> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					UUIDUtil.CODEC.fieldOf("owner").forGetter(inventoryHeatComponent -> inventoryHeatComponent.owner),
					Codec.INT.fieldOf("slot").forGetter(inventoryHeatComponent -> inventoryHeatComponent.slot),
					Codec.INT.fieldOf("heat").forGetter(inventoryHeatComponent -> inventoryHeatComponent.heat)
				)
				.apply(instance, InventoryHeatComponent::new)
	);
	public static final StreamCodec<ByteBuf, InventoryHeatComponent> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
}
