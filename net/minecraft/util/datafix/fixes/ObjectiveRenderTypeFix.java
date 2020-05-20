/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class ObjectiveRenderTypeFix
extends DataFix {
    public ObjectiveRenderTypeFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    private static ObjectiveCriteria.RenderType getRenderType(String string) {
        return string.equals("health") ? ObjectiveCriteria.RenderType.HEARTS : ObjectiveCriteria.RenderType.INTEGER;
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<Pair<String, Dynamic<?>>> type = DSL.named(References.OBJECTIVE.typeName(), DSL.remainderType());
        if (!Objects.equals(type, this.getInputSchema().getType(References.OBJECTIVE))) {
            throw new IllegalStateException("Objective type is not what was expected.");
        }
        return this.fixTypeEverywhere("ObjectiveRenderTypeFix", type, dynamicOps -> pair -> pair.mapSecond(dynamic -> {
            Optional<String> optional = dynamic.get("RenderType").asString().result();
            if (!optional.isPresent()) {
                String string = dynamic.get("CriteriaName").asString("");
                ObjectiveCriteria.RenderType renderType = ObjectiveRenderTypeFix.getRenderType(string);
                return dynamic.set("RenderType", dynamic.createString(renderType.getId()));
            }
            return dynamic;
        }));
    }
}

