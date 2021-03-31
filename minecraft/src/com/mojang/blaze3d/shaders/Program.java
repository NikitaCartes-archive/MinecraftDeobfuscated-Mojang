package com.mojang.blaze3d.shaders;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.commons.lang3.StringUtils;

@Environment(EnvType.CLIENT)
public class Program {
	private static final int MAX_LOG_LENGTH = 32768;
	private final Program.Type type;
	private final String name;
	private final int id;

	protected Program(Program.Type type, int i, String string) {
		this.type = type;
		this.id = i;
		this.name = string;
	}

	public void attachToShader(Shader shader) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GlStateManager.glAttachShader(shader.getId(), this.getId());
	}

	public void close() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GlStateManager.glDeleteShader(this.id);
		this.type.getPrograms().remove(this.name);
	}

	public String getName() {
		return this.name;
	}

	public static Program compileShader(Program.Type type, String string, InputStream inputStream, String string2, GlslPreprocessor glslPreprocessor) throws IOException {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		int i = compileShaderInternal(type, string, inputStream, string2, glslPreprocessor);
		Program program = new Program(type, i, string);
		type.getPrograms().put(string, program);
		return program;
	}

	protected static int compileShaderInternal(Program.Type type, String string, InputStream inputStream, String string2, GlslPreprocessor glslPreprocessor) throws IOException {
		String string3 = TextureUtil.readResourceAsString(inputStream);
		if (string3 == null) {
			throw new IOException("Could not load program " + type.getName());
		} else {
			int i = GlStateManager.glCreateShader(type.getGlType());
			GlStateManager.glShaderSource(i, glslPreprocessor.process(string3));
			GlStateManager.glCompileShader(i);
			if (GlStateManager.glGetShaderi(i, 35713) == 0) {
				String string4 = StringUtils.trim(GlStateManager.glGetShaderInfoLog(i, 32768));
				throw new IOException("Couldn't compile " + type.getName() + " program (" + string2 + ", " + string + ") : " + string4);
			} else {
				return i;
			}
		}
	}

	private static Program createProgram(Program.Type type, String string, int i) {
		return new Program(type, i, string);
	}

	protected int getId() {
		return this.id;
	}

	@Environment(EnvType.CLIENT)
	public static enum Type {
		VERTEX("vertex", ".vsh", 35633),
		FRAGMENT("fragment", ".fsh", 35632);

		private final String name;
		private final String extension;
		private final int glType;
		private final Map<String, Program> programs = Maps.<String, Program>newHashMap();

		private Type(String string2, String string3, int j) {
			this.name = string2;
			this.extension = string3;
			this.glType = j;
		}

		public String getName() {
			return this.name;
		}

		public String getExtension() {
			return this.extension;
		}

		private int getGlType() {
			return this.glType;
		}

		public Map<String, Program> getPrograms() {
			return this.programs;
		}
	}
}
