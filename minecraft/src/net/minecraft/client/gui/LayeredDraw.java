package net.minecraft.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.DeltaTracker;

@Environment(EnvType.CLIENT)
public class LayeredDraw {
	public static final float Z_SEPARATION = 200.0F;
	private final List<LayeredDraw.Layer> layers = new ArrayList();

	public LayeredDraw add(LayeredDraw.Layer layer) {
		this.layers.add(layer);
		return this;
	}

	public LayeredDraw add(LayeredDraw layeredDraw, BooleanSupplier booleanSupplier) {
		return this.add((guiGraphics, deltaTracker) -> {
			if (booleanSupplier.getAsBoolean()) {
				layeredDraw.renderInner(guiGraphics, deltaTracker);
			}
		});
	}

	public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		guiGraphics.pose().pushPose();
		this.renderInner(guiGraphics, deltaTracker);
		guiGraphics.pose().popPose();
	}

	private void renderInner(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		for (LayeredDraw.Layer layer : this.layers) {
			layer.render(guiGraphics, deltaTracker);
			guiGraphics.pose().translate(0.0F, 0.0F, 200.0F);
		}
	}

	@Environment(EnvType.CLIENT)
	public interface Layer {
		void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker);
	}
}
