package com.mojang.blaze3d.vertex;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class DefaultVertexFormat {
	public static final VertexFormatElement ELEMENT_POSITION = new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.POSITION, 3);
	public static final VertexFormatElement ELEMENT_COLOR = new VertexFormatElement(0, VertexFormatElement.Type.UBYTE, VertexFormatElement.Usage.COLOR, 4);
	public static final VertexFormatElement ELEMENT_UV0 = new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.UV, 2);
	public static final VertexFormatElement ELEMENT_UV1 = new VertexFormatElement(1, VertexFormatElement.Type.SHORT, VertexFormatElement.Usage.UV, 2);
	public static final VertexFormatElement ELEMENT_UV2 = new VertexFormatElement(2, VertexFormatElement.Type.SHORT, VertexFormatElement.Usage.UV, 2);
	public static final VertexFormatElement ELEMENT_NORMAL = new VertexFormatElement(0, VertexFormatElement.Type.BYTE, VertexFormatElement.Usage.NORMAL, 3);
	public static final VertexFormatElement ELEMENT_PADDING = new VertexFormatElement(0, VertexFormatElement.Type.BYTE, VertexFormatElement.Usage.PADDING, 1);
	public static final VertexFormat BLOCK = new VertexFormat(
		ImmutableList.<VertexFormatElement>builder()
			.add(ELEMENT_POSITION)
			.add(ELEMENT_COLOR)
			.add(ELEMENT_UV0)
			.add(ELEMENT_UV2)
			.add(ELEMENT_NORMAL)
			.add(ELEMENT_PADDING)
			.build()
	);
	public static final VertexFormat NEW_ENTITY = new VertexFormat(
		ImmutableList.<VertexFormatElement>builder()
			.add(ELEMENT_POSITION)
			.add(ELEMENT_COLOR)
			.add(ELEMENT_UV0)
			.add(ELEMENT_UV1)
			.add(ELEMENT_UV2)
			.add(ELEMENT_NORMAL)
			.add(ELEMENT_PADDING)
			.build()
	);
	@Deprecated
	public static final VertexFormat PARTICLE = new VertexFormat(
		ImmutableList.<VertexFormatElement>builder().add(ELEMENT_POSITION).add(ELEMENT_UV0).add(ELEMENT_COLOR).add(ELEMENT_UV2).build()
	);
	public static final VertexFormat POSITION = new VertexFormat(ImmutableList.<VertexFormatElement>builder().add(ELEMENT_POSITION).build());
	public static final VertexFormat POSITION_COLOR = new VertexFormat(
		ImmutableList.<VertexFormatElement>builder().add(ELEMENT_POSITION).add(ELEMENT_COLOR).build()
	);
	public static final VertexFormat POSITION_TEX = new VertexFormat(ImmutableList.<VertexFormatElement>builder().add(ELEMENT_POSITION).add(ELEMENT_UV0).build());
	public static final VertexFormat POSITION_COLOR_TEX = new VertexFormat(
		ImmutableList.<VertexFormatElement>builder().add(ELEMENT_POSITION).add(ELEMENT_COLOR).add(ELEMENT_UV0).build()
	);
	@Deprecated
	public static final VertexFormat POSITION_TEX_COLOR = new VertexFormat(
		ImmutableList.<VertexFormatElement>builder().add(ELEMENT_POSITION).add(ELEMENT_UV0).add(ELEMENT_COLOR).build()
	);
	public static final VertexFormat POSITION_COLOR_TEX_LIGHTMAP = new VertexFormat(
		ImmutableList.<VertexFormatElement>builder().add(ELEMENT_POSITION).add(ELEMENT_COLOR).add(ELEMENT_UV0).add(ELEMENT_UV2).build()
	);
	@Deprecated
	public static final VertexFormat POSITION_TEX_LIGHTMAP_COLOR = new VertexFormat(
		ImmutableList.<VertexFormatElement>builder().add(ELEMENT_POSITION).add(ELEMENT_UV0).add(ELEMENT_UV2).add(ELEMENT_COLOR).build()
	);
	@Deprecated
	public static final VertexFormat POSITION_TEX_COLOR_NORMAL = new VertexFormat(
		ImmutableList.<VertexFormatElement>builder().add(ELEMENT_POSITION).add(ELEMENT_UV0).add(ELEMENT_COLOR).add(ELEMENT_NORMAL).add(ELEMENT_PADDING).build()
	);
}
