package com.bobmowzie.mowziesmobs.client.render.item;

import com.bobmowzie.mowziesmobs.client.model.item.ModelEarthrendGauntlet;
import com.bobmowzie.mowziesmobs.server.item.ItemEarthrendGauntlet;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class RenderEarthrendGauntlet extends GeoItemRenderer<ItemEarthrendGauntlet> {

    public RenderEarthrendGauntlet() {
        super(new ModelEarthrendGauntlet());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!getGeoModel().getAnimationProcessor().getRegisteredBones().isEmpty()) {
            if (transformType == ItemDisplayContext.THIRD_PERSON_LEFT_HAND ||
                    transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
                getGeoModel().getBone("root").get().setHidden(true);
                getGeoModel().getBone("rootFlipped").get().setHidden(false);
            }
            else {
                getGeoModel().getBone("root").get().setHidden(false);
                getGeoModel().getBone("rootFlipped").get().setHidden(true);
            }
        }
        super.renderByItem(stack, transformType, poseStack, bufferSource, packedLight, packedOverlay);
    }
}
