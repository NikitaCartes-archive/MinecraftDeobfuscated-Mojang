package net.minecraft.client.animation.definitions;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

@Environment(EnvType.CLIENT)
public class ArmadilloAnimation {
	public static final AnimationDefinition ARMADILLO_ROLL_UP = AnimationDefinition.Builder.withLength(0.5F)
		.addAnimation(
			"body",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.1667F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"body",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.1667F, KeyframeAnimations.posVec(0.0F, 5.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.2083F, KeyframeAnimations.posVec(0.0F, 6.0F, -1.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, 6.0F, -1.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.375F, KeyframeAnimations.posVec(0.0F, -1.0F, -1.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"tail",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.1667F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"tail",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.1667F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.2083F, KeyframeAnimations.posVec(0.0F, 0.0F, -2.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, 0.0F, -2.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"head",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.1667F, KeyframeAnimations.degreeVec(17.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.25F, KeyframeAnimations.degreeVec(-72.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"head",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.1667F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.2083F, KeyframeAnimations.posVec(0.0F, 2.0F, 1.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, 2.0F, 1.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.2917F, KeyframeAnimations.posVec(0.0F, 2.0F, 6.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.375F, KeyframeAnimations.posVec(0.0F, 2.0F, 7.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"right_hind_leg",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-35.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"right_hind_leg",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.1667F, KeyframeAnimations.posVec(0.0F, 5.0F, -2.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.1875F, KeyframeAnimations.posVec(0.0F, 8.0F, -3.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.2083F, KeyframeAnimations.posVec(0.0F, 8.0F, -2.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, 8.0F, -2.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.375F, KeyframeAnimations.posVec(1.0F, 3.0F, -6.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"left_hind_leg",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-45.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"left_hind_leg",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.1667F, KeyframeAnimations.posVec(0.0F, 5.0F, -2.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.1875F, KeyframeAnimations.posVec(0.0F, 8.0F, -3.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.2083F, KeyframeAnimations.posVec(0.0F, 8.0F, -2.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, 8.0F, -2.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.375F, KeyframeAnimations.posVec(-1.0F, 3.0F, -6.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"right_front_leg",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-27.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.2083F, KeyframeAnimations.degreeVec(-32.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.25F, KeyframeAnimations.degreeVec(-85.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"right_front_leg",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.1667F, KeyframeAnimations.posVec(0.0F, 5.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.1875F, KeyframeAnimations.posVec(-0.5F, 11.5F, 0.5F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.2083F, KeyframeAnimations.posVec(-1.0F, 9.0F, -1.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.25F, KeyframeAnimations.posVec(-1.0F, 9.0F, -1.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.375F, KeyframeAnimations.posVec(-1.0F, 2.0F, 3.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"left_front_leg",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.1667F, KeyframeAnimations.degreeVec(-12.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.2083F, KeyframeAnimations.degreeVec(-35.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.25F, KeyframeAnimations.degreeVec(-85.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"left_front_leg",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.1667F, KeyframeAnimations.posVec(0.0F, 5.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.1875F, KeyframeAnimations.posVec(0.5F, 11.5F, 0.5F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.2083F, KeyframeAnimations.posVec(1.0F, 9.0F, -1.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.25F, KeyframeAnimations.posVec(1.0F, 9.0F, -1.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.375F, KeyframeAnimations.posVec(1.0F, 2.0F, 3.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"cube",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.1667F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.4167F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.4583F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"cube",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 3.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.1667F, KeyframeAnimations.posVec(0.0F, 8.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.2083F, KeyframeAnimations.posVec(0.0F, 7.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, 7.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.375F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.4167F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.4583F, KeyframeAnimations.posVec(0.0F, 0.6F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.build();
	public static final AnimationDefinition ARMADILLO_WALK = AnimationDefinition.Builder.withLength(1.4583F)
		.looping()
		.addAnimation(
			"body",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 4.6F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.2917F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 6.81F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.7083F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.9583F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -4.6F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -6.89F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.4583F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"body",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, -0.2F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.7083F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.9583F, KeyframeAnimations.posVec(0.0F, -0.2F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.25F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.4583F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"tail",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.5F, KeyframeAnimations.degreeVec(-9.17F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.75F, KeyframeAnimations.degreeVec(5.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.2083F, KeyframeAnimations.degreeVec(-8.24F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.4583F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"right_hind_leg",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(-50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.75F, KeyframeAnimations.degreeVec(50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.0F, KeyframeAnimations.degreeVec(50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.2917F, KeyframeAnimations.degreeVec(-20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.4583F, KeyframeAnimations.degreeVec(-50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"right_hind_leg",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, 0.0F, -0.5F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.0F, -0.5F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.2917F, KeyframeAnimations.posVec(0.0F, 1.0F, -0.18F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.4583F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"left_hind_leg",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.25F, KeyframeAnimations.degreeVec(50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.5417F, KeyframeAnimations.degreeVec(-20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.7083F, KeyframeAnimations.degreeVec(-50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.9583F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.2083F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.4583F, KeyframeAnimations.degreeVec(50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"left_hind_leg",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, -0.25F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, 0.0F, -0.5F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.5417F, KeyframeAnimations.posVec(0.0F, 1.0F, -0.18F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.7083F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.9583F, KeyframeAnimations.posVec(0.0F, 0.0F, -0.5F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.2083F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.4583F, KeyframeAnimations.posVec(0.0F, 0.0F, -0.25F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"right_front_leg",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.2917F, KeyframeAnimations.degreeVec(50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.5417F, KeyframeAnimations.degreeVec(-20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.7083F, KeyframeAnimations.degreeVec(-50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.9583F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.2083F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.4583F, KeyframeAnimations.degreeVec(50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"right_front_leg",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, -0.25F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, 0.0F, -0.5F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.5417F, KeyframeAnimations.posVec(0.0F, 1.0F, -0.18F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.7083F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.9583F, KeyframeAnimations.posVec(0.0F, 0.0F, -0.5F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.2083F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.4583F, KeyframeAnimations.posVec(0.0F, 0.0F, -0.25F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"left_front_leg",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(-50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.75F, KeyframeAnimations.degreeVec(50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.0F, KeyframeAnimations.degreeVec(50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.2917F, KeyframeAnimations.degreeVec(-20.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.4583F, KeyframeAnimations.degreeVec(-50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"left_front_leg",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, 0.0F, -0.5F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.0F, KeyframeAnimations.posVec(0.0F, 0.0F, -0.5F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.2917F, KeyframeAnimations.posVec(0.0F, 1.0F, -0.18F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.4583F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"head",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -2.5F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.7083F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 2.5F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.4583F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.build();
	public static final AnimationDefinition ARMADILLO_PEEK = AnimationDefinition.Builder.withLength(2.5F)
		.addAnimation(
			"head",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(-70.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.15F, KeyframeAnimations.degreeVec(-65.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.4F, KeyframeAnimations.degreeVec(-50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.7F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.9F, KeyframeAnimations.degreeVec(-7.5F, 0.0F, 45.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.15F, KeyframeAnimations.degreeVec(-7.5F, 0.0F, 45.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.3F, KeyframeAnimations.degreeVec(-0.8639F, -1.4959F, -39.1287F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.6F, KeyframeAnimations.degreeVec(-0.8639F, -1.4959F, -39.1287F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.75F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.8F, KeyframeAnimations.degreeVec(-25.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.85F, KeyframeAnimations.degreeVec(-70.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"head",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 1.0F, 7.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.05F, KeyframeAnimations.posVec(0.0F, 1.0F, 4.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.15F, KeyframeAnimations.posVec(0.0F, 1.0F, 4.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, 1.0F, 5.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.35F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.2F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.4F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.2F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 2.1F, 1.2F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.6F, KeyframeAnimations.posVec(0.0F, 1.1F, 0.2F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.7F, KeyframeAnimations.posVec(0.0F, 1.1F, 0.2F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.75F, KeyframeAnimations.posVec(0.0F, 1.1F, 0.2F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.8F, KeyframeAnimations.posVec(0.0F, 0.1F, 0.2F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.95F, KeyframeAnimations.posVec(0.0F, 0.1F, 5.2F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 0.1F, 7.2F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(2.15F, KeyframeAnimations.posVec(0.0F, 0.1F, 8.2F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(2.3F, KeyframeAnimations.posVec(0.0F, 0.1F, 5.2F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"right_hind_leg",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION, new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 3.0F, -2.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"left_hind_leg",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION, new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 3.0F, -2.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"right_front_leg",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.5833F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.8333F, KeyframeAnimations.degreeVec(-45.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.0F, KeyframeAnimations.degreeVec(-45.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.75F, KeyframeAnimations.degreeVec(-45.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.8F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.95F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"right_front_leg",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(-1.0F, 2.0F, 2.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.5833F, KeyframeAnimations.posVec(-1.0F, 2.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.6667F, KeyframeAnimations.posVec(-1.0F, 2.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.8333F, KeyframeAnimations.posVec(-1.0F, 2.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.0F, KeyframeAnimations.posVec(-1.0F, 2.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.75F, KeyframeAnimations.posVec(-1.0F, 2.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.95F, KeyframeAnimations.posVec(-1.0F, 2.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.0F, KeyframeAnimations.posVec(-1.0F, 2.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.15F, KeyframeAnimations.posVec(-1.0F, 3.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"left_front_leg",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.6667F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.8333F, KeyframeAnimations.degreeVec(-45.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.0F, KeyframeAnimations.degreeVec(-45.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.75F, KeyframeAnimations.degreeVec(-45.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.8F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.95F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"left_front_leg",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(1.0F, 2.0F, 2.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.6667F, KeyframeAnimations.posVec(1.0F, 2.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.75F, KeyframeAnimations.posVec(1.0F, 2.0F, -2.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.8333F, KeyframeAnimations.posVec(1.0F, 2.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.0F, KeyframeAnimations.posVec(1.0F, 2.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.75F, KeyframeAnimations.posVec(1.0F, 2.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.95F, KeyframeAnimations.posVec(1.0F, 2.0F, -1.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.0F, KeyframeAnimations.posVec(1.0F, 2.0F, 3.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(2.15F, KeyframeAnimations.posVec(1.0F, 3.0F, 4.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"cube",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.35F, KeyframeAnimations.degreeVec(15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.5F, KeyframeAnimations.degreeVec(-7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.6F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(2.05F, KeyframeAnimations.degreeVec(-17.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(2.15F, KeyframeAnimations.degreeVec(-25.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(2.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(2.3F, KeyframeAnimations.degreeVec(12.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(2.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"cube",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.35F, KeyframeAnimations.posVec(0.0F, 1.6F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.5F, KeyframeAnimations.posVec(0.0F, 0.5F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.6F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(2.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(2.05F, KeyframeAnimations.posVec(0.0F, 1.2F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(2.15F, KeyframeAnimations.posVec(0.0F, 1.7F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(2.25F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(2.3F, KeyframeAnimations.posVec(0.0F, 1.3F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(2.5F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.build();
	public static final AnimationDefinition ARMADILLO_ROLL_OUT = AnimationDefinition.Builder.withLength(1.5F)
		.addAnimation(
			"head",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.1F, KeyframeAnimations.degreeVec(-50.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.15F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.4F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.65F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.7F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.85F, KeyframeAnimations.degreeVec(-2.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.9F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.95F, KeyframeAnimations.degreeVec(-7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.05F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.1F, KeyframeAnimations.degreeVec(7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"head",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 1.0F, 5.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.05F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.2F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.1F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.2F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.15F, KeyframeAnimations.posVec(0.0F, 2.1F, 1.2F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, 1.03F, 0.13F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.4F, KeyframeAnimations.posVec(0.0F, 1.03F, 0.13F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.65F, KeyframeAnimations.posVec(0.0F, 1.03F, 0.13F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.7F, KeyframeAnimations.posVec(0.0F, 1.1F, 0.2F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.75F, KeyframeAnimations.posVec(0.0F, 4.1F, 2.2F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.85F, KeyframeAnimations.posVec(0.0F, 5.1F, 3.2F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.9F, KeyframeAnimations.posVec(0.0F, 0.1F, 0.2F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.95F, KeyframeAnimations.posVec(0.0F, 0.9F, -0.8F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.05F, KeyframeAnimations.posVec(0.0F, 0.9F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.1F, KeyframeAnimations.posVec(0.0F, 2.6F, 0.2F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.15F, KeyframeAnimations.posVec(0.0F, 2.4F, 0.2F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.2F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.2F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.25F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.2F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.3F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.2F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"right_hind_leg",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(1.1F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.3F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 30.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.4F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.45F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"right_hind_leg",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(1.1F, KeyframeAnimations.posVec(0.0F, 3.0F, -2.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.2F, KeyframeAnimations.posVec(0.0F, 8.0F, -2.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.3F, KeyframeAnimations.posVec(-1.0F, 3.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.4F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.45F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.5F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"left_hind_leg",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(1.1F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.3F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -30.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.4F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.45F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"left_hind_leg",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(1.1F, KeyframeAnimations.posVec(0.0F, 3.0F, -2.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.2F, KeyframeAnimations.posVec(0.0F, 8.0F, -2.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.3F, KeyframeAnimations.posVec(1.0F, 3.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.35F, KeyframeAnimations.posVec(1.0F, 3.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.4F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.45F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.5F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"right_front_leg",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.05F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.25F, KeyframeAnimations.degreeVec(-45.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.55F, KeyframeAnimations.degreeVec(-45.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.6F, KeyframeAnimations.degreeVec(-92.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.1F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.3F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 30.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.4F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.45F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"right_front_leg",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(-1.0F, 2.0F, 2.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.05F, KeyframeAnimations.posVec(-1.0F, 2.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.25F, KeyframeAnimations.posVec(-1.0F, 2.0F, -1.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.55F, KeyframeAnimations.posVec(-1.0F, 2.0F, -1.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.7F, KeyframeAnimations.posVec(-1.0F, 2.0F, 2.63F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.1F, KeyframeAnimations.posVec(-1.0F, 2.0F, 2.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.2F, KeyframeAnimations.posVec(-1.0F, 7.0F, 2.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.3F, KeyframeAnimations.posVec(-1.0F, 3.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.4F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.45F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.5F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"left_front_leg",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.05F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.25F, KeyframeAnimations.degreeVec(-45.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.55F, KeyframeAnimations.degreeVec(-45.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.6F, KeyframeAnimations.degreeVec(-87.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.1F, KeyframeAnimations.degreeVec(-90.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.3F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -30.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.4F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.45F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"left_front_leg",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(1.0F, 2.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.05F, KeyframeAnimations.posVec(1.0F, 2.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.15F, KeyframeAnimations.posVec(1.0F, 2.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.25F, KeyframeAnimations.posVec(1.0F, 2.0F, -1.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.55F, KeyframeAnimations.posVec(1.0F, 2.0F, -1.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.7F, KeyframeAnimations.posVec(1.0F, 2.0F, 1.88F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(0.75F, KeyframeAnimations.posVec(1.0F, 2.0F, 2.67F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.1F, KeyframeAnimations.posVec(1.0F, 2.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.2F, KeyframeAnimations.posVec(1.0F, 8.0F, 2.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.25F, KeyframeAnimations.posVec(1.06F, 5.06F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.3F, KeyframeAnimations.posVec(1.0F, 3.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.4F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
				new Keyframe(1.45F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.5F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
			)
		)
		.addAnimation(
			"cube",
			new AnimationChannel(
				AnimationChannel.Targets.ROTATION,
				new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.05F, KeyframeAnimations.degreeVec(15.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.15F, KeyframeAnimations.degreeVec(-7.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.25F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.7F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.75F, KeyframeAnimations.degreeVec(-17.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.85F, KeyframeAnimations.degreeVec(-25.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.9F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.95F, KeyframeAnimations.degreeVec(12.5F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.05F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.1F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"cube",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.05F, KeyframeAnimations.posVec(0.0F, 1.6F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.15F, KeyframeAnimations.posVec(0.0F, 0.5F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.25F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.7F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.75F, KeyframeAnimations.posVec(0.0F, 1.2F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.85F, KeyframeAnimations.posVec(0.0F, 1.7F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.9F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(0.95F, KeyframeAnimations.posVec(0.0F, 1.3F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.05F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.2F, KeyframeAnimations.posVec(0.0F, 5.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.25F, KeyframeAnimations.posVec(0.0F, 8.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.5F, KeyframeAnimations.posVec(0.0F, 1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.addAnimation(
			"body",
			new AnimationChannel(
				AnimationChannel.Targets.POSITION,
				new Keyframe(1.1F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.2F, KeyframeAnimations.posVec(0.0F, 4.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.25F, KeyframeAnimations.posVec(0.0F, 5.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.3F, KeyframeAnimations.posVec(0.0F, 4.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.4F, KeyframeAnimations.posVec(0.0F, -1.0F, 0.0F), AnimationChannel.Interpolations.LINEAR),
				new Keyframe(1.5F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.LINEAR)
			)
		)
		.build();
}
