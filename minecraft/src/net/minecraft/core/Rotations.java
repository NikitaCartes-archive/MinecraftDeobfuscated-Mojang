package net.minecraft.core;

import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;

public class Rotations {
	protected final float x;
	protected final float y;
	protected final float z;

	public Rotations(float f, float g, float h) {
		this.x = !Float.isInfinite(f) && !Float.isNaN(f) ? f % 360.0F : 0.0F;
		this.y = !Float.isInfinite(g) && !Float.isNaN(g) ? g % 360.0F : 0.0F;
		this.z = !Float.isInfinite(h) && !Float.isNaN(h) ? h % 360.0F : 0.0F;
	}

	public Rotations(ListTag listTag) {
		this(listTag.getFloat(0), listTag.getFloat(1), listTag.getFloat(2));
	}

	public ListTag save() {
		ListTag listTag = new ListTag();
		listTag.add(FloatTag.valueOf(this.x));
		listTag.add(FloatTag.valueOf(this.y));
		listTag.add(FloatTag.valueOf(this.z));
		return listTag;
	}

	public boolean equals(Object object) {
		if (!(object instanceof Rotations)) {
			return false;
		} else {
			Rotations rotations = (Rotations)object;
			return this.x == rotations.x && this.y == rotations.y && this.z == rotations.z;
		}
	}

	public float getX() {
		return this.x;
	}

	public float getY() {
		return this.y;
	}

	public float getZ() {
		return this.z;
	}
}
