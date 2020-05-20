package net.minecraft.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.DimensionType;

public final class GlobalPos {
	public static final Codec<GlobalPos> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					DimensionType.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(GlobalPos::dimension), BlockPos.CODEC.fieldOf("pos").forGetter(GlobalPos::pos)
				)
				.apply(instance, GlobalPos::of)
	);
	private final ResourceKey<DimensionType> dimension;
	private final BlockPos pos;

	private GlobalPos(ResourceKey<DimensionType> resourceKey, BlockPos blockPos) {
		this.dimension = resourceKey;
		this.pos = blockPos;
	}

	public static GlobalPos of(ResourceKey<DimensionType> resourceKey, BlockPos blockPos) {
		return new GlobalPos(resourceKey, blockPos);
	}

	public ResourceKey<DimensionType> dimension() {
		return this.dimension;
	}

	public BlockPos pos() {
		return this.pos;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			GlobalPos globalPos = (GlobalPos)object;
			return Objects.equals(this.dimension, globalPos.dimension) && Objects.equals(this.pos, globalPos.pos);
		} else {
			return false;
		}
	}

	public int hashCode() {
		return Objects.hash(new Object[]{this.dimension, this.pos});
	}

	public String toString() {
		return this.dimension.toString() + " " + this.pos;
	}
}
