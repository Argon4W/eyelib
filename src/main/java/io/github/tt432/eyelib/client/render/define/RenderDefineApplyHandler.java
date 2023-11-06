package io.github.tt432.eyelib.client.render.define;

import io.github.tt432.eyelib.capability.AnimatableCapability;
import io.github.tt432.eyelib.client.animation.component.ModelComponent;
import io.github.tt432.eyelib.client.loader.BrModelLoader;
import io.github.tt432.eyelib.client.loader.ModelReplacerLoader;
import io.github.tt432.eyelib.client.model.bedrock.BrModel;
import io.github.tt432.eyelib.client.render.visitor.BlankEntityModelRenderVisit;
import io.github.tt432.eyelib.event.InitComponentEvent;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Random;

/**
 * @author TT432
 */
@Mod.EventBusSubscriber(Dist.CLIENT)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RenderDefineApplyHandler {

    private static final Random rnd = new Random();

    @SubscribeEvent
    public static void onEvent(InitComponentEvent event) {
        if (event.entity instanceof Entity entity && event.componentObject instanceof AnimatableCapability<?> capability) {
            ModelComponent modelComponent = capability.getModelComponent();

            RenderDefine renderDefine = ModelReplacerLoader.byTarget(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()));

            if (renderDefine == null) {
                return;
            }

            BrModel model = BrModelLoader.getModel(renderDefine.getModel());

            if (model == null) {
                return;
            }

            ResourceLocation[] textures = renderDefine.getTextures();

            if (textures.length == 0) {
                return;
            }

            modelComponent.setModel(model.copy());
            ResourceLocation texture = textures[rnd.nextInt(textures.length)];
            modelComponent.setTexture(new ResourceLocation(texture.getNamespace(),
                    "textures/" + texture.getPath() + ".png"));
            modelComponent.setVisitor(new BlankEntityModelRenderVisit());
        }
    }
}
