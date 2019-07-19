package net.minecraft.world.phys;

import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class PosAndRot {
	private final Vec3 pos;
	private final float xRot;
	private final float yRot;

	public PosAndRot(Vec3 vec3, float f, float g) {
		this.pos = vec3;
		this.xRot = f;
		this.yRot = g;
	}

	public Vec3 pos() {
		return this.pos;
	}

	public float xRot() {
		return this.xRot;
	}

	public float yRot() {
		return this.yRot;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (object != null && this.getClass() == object.getClass()) {
			PosAndRot posAndRot = (PosAndRot)object;
			return Float.compare(posAndRot.xRot, this.xRot) == 0 && Float.compare(posAndRot.yRot, this.yRot) == 0 && Objects.equals(this.pos, posAndRot.pos);
		} else {
			return false;
		}
	}

	public int hashCode() {
		return Objects.hash(new Object[]{this.pos, this.xRot, this.yRot});
	}

	public String toString() {
		return "PosAndRot[" + this.pos + " (" + this.xRot + ", " + this.yRot + ")]";
	}
}
