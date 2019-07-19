/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.tutorial;

import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.tutorial.CompletedTutorialStepInstance;
import net.minecraft.client.tutorial.CraftPlanksTutorialStep;
import net.minecraft.client.tutorial.FindTreeTutorialStepInstance;
import net.minecraft.client.tutorial.MovementTutorialStepInstance;
import net.minecraft.client.tutorial.OpenInventoryTutorialStep;
import net.minecraft.client.tutorial.PunchTreeTutorialStepInstance;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.client.tutorial.TutorialStepInstance;

@Environment(value=EnvType.CLIENT)
public enum TutorialSteps {
    MOVEMENT("movement", MovementTutorialStepInstance::new),
    FIND_TREE("find_tree", FindTreeTutorialStepInstance::new),
    PUNCH_TREE("punch_tree", PunchTreeTutorialStepInstance::new),
    OPEN_INVENTORY("open_inventory", OpenInventoryTutorialStep::new),
    CRAFT_PLANKS("craft_planks", CraftPlanksTutorialStep::new),
    NONE("none", CompletedTutorialStepInstance::new);

    private final String name;
    private final Function<Tutorial, ? extends TutorialStepInstance> constructor;

    private <T extends TutorialStepInstance> TutorialSteps(String string2, Function<Tutorial, T> function) {
        this.name = string2;
        this.constructor = function;
    }

    public TutorialStepInstance create(Tutorial tutorial) {
        return this.constructor.apply(tutorial);
    }

    public String getName() {
        return this.name;
    }

    public static TutorialSteps getByName(String string) {
        for (TutorialSteps tutorialSteps : TutorialSteps.values()) {
            if (!tutorialSteps.name.equals(string)) continue;
            return tutorialSteps;
        }
        return NONE;
    }
}

