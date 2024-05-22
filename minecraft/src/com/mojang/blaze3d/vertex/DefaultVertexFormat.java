package com.mojang.blaze3d.vertex;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class DefaultVertexFormat {
	public static final VertexFormat BLIT_SCREEN = VertexFormat.builder().add("Position", VertexFormatElement.POSITION).build();
	public static final VertexFormat BLOCK = VertexFormat.builder()
		.add("Position", VertexFormatElement.POSITION)
		.add("Color", VertexFormatElement.COLOR)
		.add("UV0", VertexFormatElement.UV0)
		.add("UV2", VertexFormatElement.UV2)
		.add("Normal", VertexFormatElement.NORMAL)
		.padding(1)
		.build();
	public static final VertexFormat NEW_ENTITY = VertexFormat.builder()
		.add("Position", VertexFormatElement.POSITION)
		.add("Color", VertexFormatElement.COLOR)
		.add("UV0", VertexFormatElement.UV0)
		.add("UV1", VertexFormatElement.UV1)
		.add("UV2", VertexFormatElement.UV2)
		.add("Normal", VertexFormatElement.NORMAL)
		.padding(1)
		.build();
	public static final VertexFormat PARTICLE = VertexFormat.builder()
		.add("Position", VertexFormatElement.POSITION)
		.add("UV0", VertexFormatElement.UV0)
		.add("Color", VertexFormatElement.COLOR)
		.add("UV2", VertexFormatElement.UV2)
		.build();
	public static final VertexFormat POSITION = VertexFormat.builder().add("Position", VertexFormatElement.POSITION).build();
	public static final VertexFormat POSITION_COLOR = VertexFormat.builder()
		.add("Position", VertexFormatElement.POSITION)
		.add("Color", VertexFormatElement.COLOR)
		.build();
	public static final VertexFormat POSITION_COLOR_NORMAL = VertexFormat.builder()
		.add("Position", VertexFormatElement.POSITION)
		.add("Color", VertexFormatElement.COLOR)
		.add("Normal", VertexFormatElement.NORMAL)
		.padding(1)
		.build();
	public static final VertexFormat POSITION_COLOR_LIGHTMAP = VertexFormat.builder()
		.add("Position", VertexFormatElement.POSITION)
		.add("Color", VertexFormatElement.COLOR)
		.add("UV2", VertexFormatElement.UV2)
		.build();
	public static final VertexFormat POSITION_TEX = VertexFormat.builder()
		.add("Position", VertexFormatElement.POSITION)
		.add("UV0", VertexFormatElement.UV0)
		.build();
	public static final VertexFormat POSITION_TEX_COLOR = VertexFormat.builder()
		.add("Position", VertexFormatElement.POSITION)
		.add("UV0", VertexFormatElement.UV0)
		.add("Color", VertexFormatElement.COLOR)
		.build();
	public static final VertexFormat POSITION_COLOR_TEX_LIGHTMAP = VertexFormat.builder()
		.add("Position", VertexFormatElement.POSITION)
		.add("Color", VertexFormatElement.COLOR)
		.add("UV0", VertexFormatElement.UV0)
		.add("UV2", VertexFormatElement.UV2)
		.build();
	public static final VertexFormat POSITION_TEX_LIGHTMAP_COLOR = VertexFormat.builder()
		.add("Position", VertexFormatElement.POSITION)
		.add("UV0", VertexFormatElement.UV0)
		.add("UV2", VertexFormatElement.UV2)
		.add("Color", VertexFormatElement.COLOR)
		.build();
	public static final VertexFormat POSITION_TEX_COLOR_NORMAL = VertexFormat.builder()
		.add("Position", VertexFormatElement.POSITION)
		.add("UV0", VertexFormatElement.UV0)
		.add("Color", VertexFormatElement.COLOR)
		.add("Normal", VertexFormatElement.NORMAL)
		.padding(1)
		.build();
}
