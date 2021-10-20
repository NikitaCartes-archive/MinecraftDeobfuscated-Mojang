package com.mojang.blaze3d.shaders;

import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.io.InputStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class EffectProgram extends Program {
	private static final GlslPreprocessor PREPROCESSOR = new GlslPreprocessor() {
		@Override
		public String applyImport(boolean bl, String string) {
			return "#error Import statement not supported";
		}
	};
	private int references;

	private EffectProgram(Program.Type type, int i, String string) {
		super(type, i, string);
	}

	public void attachToEffect(Effect effect) {
		RenderSystem.assertOnRenderThread();
		this.references++;
		this.attachToShader(effect);
	}

	@Override
	public void close() {
		RenderSystem.assertOnRenderThread();
		this.references--;
		if (this.references <= 0) {
			super.close();
		}
	}

	public static EffectProgram compileShader(Program.Type type, String string, InputStream inputStream, String string2) throws IOException {
		RenderSystem.assertOnRenderThread();
		int i = compileShaderInternal(type, string, inputStream, string2, PREPROCESSOR);
		EffectProgram effectProgram = new EffectProgram(type, i, string);
		type.getPrograms().put(string, effectProgram);
		return effectProgram;
	}
}
