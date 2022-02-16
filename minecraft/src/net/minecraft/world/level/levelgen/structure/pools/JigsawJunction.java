package net.minecraft.world.level.levelgen.structure.pools;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;

public class JigsawJunction {
	private final int sourceX;
	private final int sourceGroundY;
	private final int sourceZ;
	private final int deltaY;
	private final StructureTemplatePool.Projection destProjection;

	public JigsawJunction(int i, int j, int k, int l, StructureTemplatePool.Projection projection) {
		this.sourceX = i;
		this.sourceGroundY = j;
		this.sourceZ = k;
		this.deltaY = l;
		this.destProjection = projection;
	}

	public int getSourceX() {
		return this.sourceX;
	}

	public int getSourceGroundY() {
		return this.sourceGroundY;
	}

	public int getSourceZ() {
		return this.sourceZ;
	}

	public int getDeltaY() {
		return this.deltaY;
	}

	public StructureTemplatePool.Projection getDestProjection() {
		return this.destProjection;
	}

	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		Builder<T, T> builder = ImmutableMap.builder();
		builder.put(dynamicOps.createString("source_x"), dynamicOps.createInt(this.sourceX))
			.put(dynamicOps.createString("source_ground_y"), dynamicOps.createInt(this.sourceGroundY))
			.put(dynamicOps.createString("source_z"), dynamicOps.createInt(this.sourceZ))
			.put(dynamicOps.createString("delta_y"), dynamicOps.createInt(this.deltaY))
			.put(dynamicOps.createString("dest_proj"), dynamicOps.createString(this.destProjection.getName()));
		return new Dynamic<>(dynamicOps, dynamicOps.createMap(builder.build()));
	}

	public static <T> JigsawJunction deserialize(Dynamic<T> dynamic) {
		return new JigsawJunction(
			dynamic.get("source_x").asInt(0),
			dynamic.get("source_ground_y").asInt(0),
			dynamic.get("source_z").asInt(0),
			dynamic.get("delta_y").asInt(0),
			StructureTemplatePool.Projection.byName(dynamic.get("dest_proj").asString(""))
		);
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			JigsawJunction jigsawJunction = (JigsawJunction)object;
			if (this.sourceX != jigsawJunction.sourceX) {
				return false;
			} else if (this.sourceZ != jigsawJunction.sourceZ) {
				return false;
			} else {
				return this.deltaY != jigsawJunction.deltaY ? false : this.destProjection == jigsawJunction.destProjection;
			}
		} else {
			return false;
		}
	}

	public int hashCode() {
		int i = this.sourceX;
		i = 31 * i + this.sourceGroundY;
		i = 31 * i + this.sourceZ;
		i = 31 * i + this.deltaY;
		return 31 * i + this.destProjection.hashCode();
	}

	public String toString() {
		return "JigsawJunction{sourceX="
			+ this.sourceX
			+ ", sourceGroundY="
			+ this.sourceGroundY
			+ ", sourceZ="
			+ this.sourceZ
			+ ", deltaY="
			+ this.deltaY
			+ ", destProjection="
			+ this.destProjection
			+ "}";
	}
}
