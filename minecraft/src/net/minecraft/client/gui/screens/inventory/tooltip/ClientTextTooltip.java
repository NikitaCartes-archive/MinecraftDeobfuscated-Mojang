package net.minecraft.client.gui.screens.inventory.tooltip;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class ClientTextTooltip implements ClientTooltipComponent {
	private final FormattedCharSequence text;

	public ClientTextTooltip(FormattedCharSequence formattedCharSequence) {
		this.text = formattedCharSequence;
	}

	@Override
	public int getWidth(Font font) {
		return font.width(this.text);
	}

	@Override
	public int getHeight() {
		return 10;
	}

	@Override
	public void renderText(Font font, int i, int j, Matrix4f matrix4f, MultiBufferSource.BufferSource bufferSource) {
		font.drawInBatch(this.text, (float)i, (float)j, -1, true, matrix4f, bufferSource, false, 0, 15728880);
	}
}
