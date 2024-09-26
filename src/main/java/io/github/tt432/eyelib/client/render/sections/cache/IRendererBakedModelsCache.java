package io.github.tt432.eyelib.client.render.sections.cache;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import net.minecraft.client.resources.model.BakedModel;

public interface IRendererBakedModelsCache {
    BakedModel getTransformedModel(BakedModel model, PoseStack poseStack);
    BakedModel getTransformedModel(BakedModel model, Transformation transformation);
    int getSize();
}
