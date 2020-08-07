package net.minecraft.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum CameraType {
	FIRST_PERSON(true, false),
	THIRD_PERSON_BACK(false, false),
	THIRD_PERSON_FRONT(false, true);

	private static final CameraType[] VALUES = values();
	private boolean firstPerson;
	private boolean mirrored;

	private CameraType(boolean bl, boolean bl2) {
		this.firstPerson = bl;
		this.mirrored = bl2;
	}

	public boolean isFirstPerson() {
		return this.firstPerson;
	}

	public boolean isMirrored() {
		return this.mirrored;
	}

	public CameraType cycle() {
		return VALUES[(this.ordinal() + 1) % VALUES.length];
	}
}
