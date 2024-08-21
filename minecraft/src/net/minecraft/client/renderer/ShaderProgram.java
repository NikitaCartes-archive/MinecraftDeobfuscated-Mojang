package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public record ShaderProgram(ResourceLocation configId, VertexFormat vertexFormat, ShaderDefines defines) {
	public String toString() {
		String string = this.configId + " (" + this.vertexFormat + ")";
		return !this.defines.isEmpty() ? string + " with " + this.defines : string;
	}
}
