package io.github.tt432.eyelib.client.animation.component;

import io.github.tt432.eyelib.client.model.flat.FlatBrModel;
import io.github.tt432.eyelib.client.render.BrModelRenderVisitor;
import lombok.Data;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

/**
 * @author TT432
 */
@Data
public class ModelComponent {
    @Nullable
    FlatBrModel model;
    @Nullable
    ResourceLocation texture;
    @Nullable
    BrModelRenderVisitor visitor;
}