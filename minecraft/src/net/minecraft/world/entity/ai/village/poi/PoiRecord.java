package net.minecraft.world.entity.ai.village.poi;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.util.VisibleForDebug;

public class PoiRecord {
	private final BlockPos pos;
	private final Holder<PoiType> poiType;
	private int freeTickets;
	private final Runnable setDirty;

	public static Codec<PoiRecord> codec(Runnable runnable) {
		return RecordCodecBuilder.create(
			instance -> instance.group(
						BlockPos.CODEC.fieldOf("pos").forGetter(poiRecord -> poiRecord.pos),
						RegistryFixedCodec.create(Registry.POINT_OF_INTEREST_TYPE_REGISTRY).fieldOf("type").forGetter(poiRecord -> poiRecord.poiType),
						Codec.INT.fieldOf("free_tickets").orElse(0).forGetter(poiRecord -> poiRecord.freeTickets),
						RecordCodecBuilder.point(runnable)
					)
					.apply(instance, PoiRecord::new)
		);
	}

	private PoiRecord(BlockPos blockPos, Holder<PoiType> holder, int i, Runnable runnable) {
		this.pos = blockPos.immutable();
		this.poiType = holder;
		this.freeTickets = i;
		this.setDirty = runnable;
	}

	public PoiRecord(BlockPos blockPos, Holder<PoiType> holder, Runnable runnable) {
		this(blockPos, holder, holder.value().maxTickets(), runnable);
	}

	@Deprecated
	@VisibleForDebug
	public int getFreeTickets() {
		return this.freeTickets;
	}

	protected boolean acquireTicket() {
		if (this.freeTickets <= 0) {
			return false;
		} else {
			this.freeTickets--;
			this.setDirty.run();
			return true;
		}
	}

	protected boolean releaseTicket() {
		if (this.freeTickets >= this.poiType.value().maxTickets()) {
			return false;
		} else {
			this.freeTickets++;
			this.setDirty.run();
			return true;
		}
	}

	public boolean hasSpace() {
		return this.freeTickets > 0;
	}

	public boolean isOccupied() {
		return this.freeTickets != this.poiType.value().maxTickets();
	}

	public BlockPos getPos() {
		return this.pos;
	}

	public Holder<PoiType> getPoiType() {
		return this.poiType;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			return object != null && this.getClass() == object.getClass() ? Objects.equals(this.pos, ((PoiRecord)object).pos) : false;
		}
	}

	public int hashCode() {
		return this.pos.hashCode();
	}
}
