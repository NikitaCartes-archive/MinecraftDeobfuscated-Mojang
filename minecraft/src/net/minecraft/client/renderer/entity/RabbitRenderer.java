package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.RabbitModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.RabbitRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Rabbit;

@Environment(EnvType.CLIENT)
public class RabbitRenderer extends AgeableMobRenderer<Rabbit, RabbitRenderState, RabbitModel> {
	private static final ResourceLocation RABBIT_BROWN_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/rabbit/brown.png");
	private static final ResourceLocation RABBIT_WHITE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/rabbit/white.png");
	private static final ResourceLocation RABBIT_BLACK_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/rabbit/black.png");
	private static final ResourceLocation RABBIT_GOLD_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/rabbit/gold.png");
	private static final ResourceLocation RABBIT_SALT_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/rabbit/salt.png");
	private static final ResourceLocation RABBIT_WHITE_SPLOTCHED_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/rabbit/white_splotched.png");
	private static final ResourceLocation RABBIT_TOAST_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/rabbit/toast.png");
	private static final ResourceLocation RABBIT_EVIL_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/rabbit/caerbannog.png");

	public RabbitRenderer(EntityRendererProvider.Context context) {
		super(context, new RabbitModel(context.bakeLayer(ModelLayers.RABBIT)), new RabbitModel(context.bakeLayer(ModelLayers.RABBIT_BABY)), 0.3F);
	}

	public ResourceLocation getTextureLocation(RabbitRenderState rabbitRenderState) {
		if (rabbitRenderState.isToast) {
			return RABBIT_TOAST_LOCATION;
		} else {
			return switch (rabbitRenderState.variant) {
				case BROWN -> RABBIT_BROWN_LOCATION;
				case WHITE -> RABBIT_WHITE_LOCATION;
				case BLACK -> RABBIT_BLACK_LOCATION;
				case GOLD -> RABBIT_GOLD_LOCATION;
				case SALT -> RABBIT_SALT_LOCATION;
				case WHITE_SPLOTCHED -> RABBIT_WHITE_SPLOTCHED_LOCATION;
				case EVIL -> RABBIT_EVIL_LOCATION;
			};
		}
	}

	public RabbitRenderState createRenderState() {
		return new RabbitRenderState();
	}

	public void extractRenderState(Rabbit rabbit, RabbitRenderState rabbitRenderState, float f) {
		super.extractRenderState(rabbit, rabbitRenderState, f);
		rabbitRenderState.jumpCompletion = rabbit.getJumpCompletion(f);
		rabbitRenderState.isToast = "Toast".equals(ChatFormatting.stripFormatting(rabbit.getName().getString()));
		rabbitRenderState.variant = rabbit.getVariant();
	}
}
