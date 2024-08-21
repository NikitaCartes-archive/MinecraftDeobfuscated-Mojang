package com.mojang.blaze3d.shaders;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

@Environment(EnvType.CLIENT)
public class CompiledShader implements AutoCloseable {
	private static final int NOT_ALLOCATED = -1;
	private final ResourceLocation id;
	private int shaderId;

	private CompiledShader(int i, ResourceLocation resourceLocation) {
		this.id = resourceLocation;
		this.shaderId = i;
	}

	public static CompiledShader compile(ResourceLocation resourceLocation, CompiledShader.Type type, String string) throws ShaderManager.CompilationException {
		RenderSystem.assertOnRenderThread();
		int i = GlStateManager.glCreateShader(type.glType());
		GlStateManager.glShaderSource(i, string);
		GlStateManager.glCompileShader(i);
		if (GlStateManager.glGetShaderi(i, 35713) == 0) {
			String string2 = StringUtils.trim(GlStateManager.glGetShaderInfoLog(i, 32768));
			throw new ShaderManager.CompilationException("Couldn't compile " + type.getName() + " shader (" + resourceLocation + ") : " + string2);
		} else {
			return new CompiledShader(i, resourceLocation);
		}
	}

	public void close() {
		if (this.shaderId == -1) {
			throw new IllegalStateException("Already closed");
		} else {
			RenderSystem.assertOnRenderThread();
			GlStateManager.glDeleteShader(this.shaderId);
			this.shaderId = -1;
		}
	}

	public ResourceLocation getId() {
		return this.id;
	}

	public int getShaderId() {
		return this.shaderId;
	}

	@Environment(EnvType.CLIENT)
	public static enum Type {
		VERTEX("vertex", ".vsh", 35633),
		FRAGMENT("fragment", ".fsh", 35632);

		private static final CompiledShader.Type[] TYPES = values();
		private final String name;
		private final String extension;
		private final int glType;

		private Type(final String string2, final String string3, final int j) {
			this.name = string2;
			this.extension = string3;
			this.glType = j;
		}

		@Nullable
		public static CompiledShader.Type byLocation(ResourceLocation resourceLocation) {
			for (CompiledShader.Type type : TYPES) {
				if (resourceLocation.getPath().endsWith(type.extension)) {
					return type;
				}
			}

			return null;
		}

		public String getName() {
			return this.name;
		}

		public int glType() {
			return this.glType;
		}

		public FileToIdConverter idConverter() {
			return new FileToIdConverter("shaders", this.extension);
		}
	}
}
