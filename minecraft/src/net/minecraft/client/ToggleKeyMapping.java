package net.minecraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.BooleanSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class ToggleKeyMapping extends KeyMapping {
	private final BooleanSupplier needsToggle;

	public ToggleKeyMapping(String string, int i, String string2, BooleanSupplier booleanSupplier) {
		super(string, InputConstants.Type.KEYSYM, i, string2);
		this.needsToggle = booleanSupplier;
	}

	@Override
	public void setDown(boolean bl) {
		if (this.needsToggle.getAsBoolean()) {
			if (bl) {
				super.setDown(!this.isDown());
			}
		} else {
			super.setDown(bl);
		}
	}

	protected void reset() {
		super.setDown(false);
	}
}
