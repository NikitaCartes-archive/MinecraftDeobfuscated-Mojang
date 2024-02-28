package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;

public record LodestoneTarget(GlobalPos pos, boolean tracked) {
	public static final Codec<LodestoneTarget> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					GlobalPos.MAP_CODEC.forGetter(LodestoneTarget::pos), ExtraCodecs.strictOptionalField(Codec.BOOL, "tracked", true).forGetter(LodestoneTarget::tracked)
				)
				.apply(instance, LodestoneTarget::new)
	);
	public static final StreamCodec<ByteBuf, LodestoneTarget> STREAM_CODEC = StreamCodec.composite(
		GlobalPos.STREAM_CODEC, LodestoneTarget::pos, ByteBufCodecs.BOOL, LodestoneTarget::tracked, LodestoneTarget::new
	);

	public boolean checkInvalid(ServerLevel serverLevel) {
		if (!this.tracked) {
			return false;
		} else if (this.pos.dimension() != serverLevel.dimension()) {
			return false;
		} else {
			BlockPos blockPos = this.pos.pos();
			return !serverLevel.isInWorldBounds(blockPos) || !serverLevel.getPoiManager().existsAtPosition(PoiTypes.LODESTONE, blockPos);
		}
	}
}
