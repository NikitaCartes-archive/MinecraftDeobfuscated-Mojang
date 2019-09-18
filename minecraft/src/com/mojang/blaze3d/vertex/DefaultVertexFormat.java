package com.mojang.blaze3d.vertex;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class DefaultVertexFormat {
	public static final VertexFormatElement ELEMENT_POSITION = new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.POSITION, 3);
	public static final VertexFormatElement ELEMENT_COLOR = new VertexFormatElement(0, VertexFormatElement.Type.UBYTE, VertexFormatElement.Usage.COLOR, 4);
	public static final VertexFormatElement ELEMENT_UV0 = new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.UV, 2);
	public static final VertexFormatElement ELEMENT_UV1 = new VertexFormatElement(1, VertexFormatElement.Type.SHORT, VertexFormatElement.Usage.UV, 2);
	public static final VertexFormatElement ELEMENT_NORMAL = new VertexFormatElement(0, VertexFormatElement.Type.BYTE, VertexFormatElement.Usage.NORMAL, 3);
	public static final VertexFormatElement ELEMENT_PADDING = new VertexFormatElement(0, VertexFormatElement.Type.BYTE, VertexFormatElement.Usage.PADDING, 1);
	public static final VertexFormat BLOCK = new VertexFormat()
		.addElement(ELEMENT_POSITION)
		.addElement(ELEMENT_COLOR)
		.addElement(ELEMENT_UV0)
		.addElement(ELEMENT_UV1)
		.addElement(ELEMENT_NORMAL)
		.addElement(ELEMENT_PADDING);
	public static final VertexFormat ENTITY = new VertexFormat()
		.addElement(ELEMENT_POSITION)
		.addElement(ELEMENT_UV0)
		.addElement(ELEMENT_NORMAL)
		.addElement(ELEMENT_PADDING);
	public static final VertexFormat PARTICLE = new VertexFormat()
		.addElement(ELEMENT_POSITION)
		.addElement(ELEMENT_UV0)
		.addElement(ELEMENT_COLOR)
		.addElement(ELEMENT_UV1);
	public static final VertexFormat POSITION = new VertexFormat().addElement(ELEMENT_POSITION);
	public static final VertexFormat POSITION_COLOR = new VertexFormat().addElement(ELEMENT_POSITION).addElement(ELEMENT_COLOR);
	public static final VertexFormat POSITION_TEX = new VertexFormat().addElement(ELEMENT_POSITION).addElement(ELEMENT_UV0);
	public static final VertexFormat POSITION_NORMAL = new VertexFormat().addElement(ELEMENT_POSITION).addElement(ELEMENT_NORMAL).addElement(ELEMENT_PADDING);
	public static final VertexFormat POSITION_TEX_COLOR = new VertexFormat().addElement(ELEMENT_POSITION).addElement(ELEMENT_UV0).addElement(ELEMENT_COLOR);
	public static final VertexFormat POSITION_TEX_NORMAL = new VertexFormat()
		.addElement(ELEMENT_POSITION)
		.addElement(ELEMENT_UV0)
		.addElement(ELEMENT_NORMAL)
		.addElement(ELEMENT_PADDING);
	public static final VertexFormat POSITION_TEX2_COLOR = new VertexFormat()
		.addElement(ELEMENT_POSITION)
		.addElement(ELEMENT_UV0)
		.addElement(ELEMENT_UV1)
		.addElement(ELEMENT_COLOR);
	public static final VertexFormat POSITION_TEX_COLOR_NORMAL = new VertexFormat()
		.addElement(ELEMENT_POSITION)
		.addElement(ELEMENT_UV0)
		.addElement(ELEMENT_COLOR)
		.addElement(ELEMENT_NORMAL)
		.addElement(ELEMENT_PADDING);
}
