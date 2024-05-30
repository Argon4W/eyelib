package io.github.tt432.eyelib.client.animation.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.tt432.eyelib.client.loader.BrModelLoader;
import io.github.tt432.eyelib.client.model.bedrock.BrModel;
import io.github.tt432.eyelib.client.render.bone.BoneRenderInfos;
import io.github.tt432.eyelib.client.render.visitor.ModelRenderVisitorRegistry;
import io.github.tt432.eyelib.client.render.visitor.builtin.ModelRenderVisitor;
import io.github.tt432.eyelib.util.client.RenderTypeSerializations;
import lombok.Getter;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * @author TT432
 */
@Getter
public class ModelComponent {
    public record SerializableInfo(
            ResourceLocation model,
            ResourceLocation texture,
            ResourceLocation renderType,
            ResourceLocation locator
    ) {
        public static final Codec<SerializableInfo> CODEC = RecordCodecBuilder.create(ins -> ins.group(
                ResourceLocation.CODEC.fieldOf("model").forGetter(o -> o.model),
                ResourceLocation.CODEC.fieldOf("texture").forGetter(o -> o.texture),
                ResourceLocation.CODEC.fieldOf("renderType").forGetter(o -> o.renderType),
                ResourceLocation.CODEC.fieldOf("locator").forGetter(o -> o.locator)
        ).apply(ins, SerializableInfo::new));
    }

    public record Info(
            BrModel model,
            ResourceLocation texture,
            Function<ResourceLocation, RenderType> renderTypeFactory,
            boolean isSolid,
            ModelRenderVisitor visitor
    ) {
    }

    @Nullable
    ModelComponent.Info info;
    SerializableInfo serializableInfo;

    public void setInfo(SerializableInfo serializableInfo) {
        this.serializableInfo = serializableInfo;

        RenderTypeSerializations.EntityRenderTypeData factory =
                RenderTypeSerializations.getFactory(serializableInfo.renderType);

        info = new Info(
                BrModelLoader.getModel(serializableInfo.model),
                serializableInfo.texture,
                factory.factory(),
                factory.isSolid(),
                ModelRenderVisitorRegistry.VISITOR_REGISTRY.get().getValue(serializableInfo.locator)
        );
    }

    final BoneRenderInfos boneInfos = new BoneRenderInfos();
}
