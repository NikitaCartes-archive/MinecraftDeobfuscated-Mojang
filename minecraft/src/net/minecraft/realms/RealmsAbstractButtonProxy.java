package net.minecraft.realms;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface RealmsAbstractButtonProxy<T extends AbstractRealmsButton<?>> {
	T getButton();

	boolean active();

	void active(boolean bl);

	boolean isVisible();

	void setVisible(boolean bl);
}
