package net.minecraft.world.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;

public record LodestoneTracker(Optional<GlobalPos> target, boolean tracked) {
	public static final Codec<LodestoneTracker> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ExtraCodecs.strictOptionalField(GlobalPos.CODEC, "target").forGetter(LodestoneTracker::target),
					ExtraCodecs.strictOptionalField(Codec.BOOL, "tracked", true).forGetter(LodestoneTracker::tracked)
				)
				.apply(instance, LodestoneTracker::new)
	);
	public static final StreamCodec<ByteBuf, LodestoneTracker> STREAM_CODEC = StreamCodec.composite(
		GlobalPos.STREAM_CODEC.apply(ByteBufCodecs::optional), LodestoneTracker::target, ByteBufCodecs.BOOL, LodestoneTracker::tracked, LodestoneTracker::new
	);

	public LodestoneTracker tick(ServerLevel serverLevel) {
		if (this.tracked && !this.target.isEmpty()) {
			if (((GlobalPos)this.target.get()).dimension() != serverLevel.dimension()) {
				return this;
			} else {
				BlockPos blockPos = ((GlobalPos)this.target.get()).pos();
				return serverLevel.isInWorldBounds(blockPos) && serverLevel.getPoiManager().existsAtPosition(PoiTypes.LODESTONE, blockPos)
					? this
					: new LodestoneTracker(Optional.empty(), true);
			}
		} else {
			return this;
		}
	}
}
