package com.bobmowzie.mowziesmobs.server.ability;

import com.bobmowzie.mowziesmobs.server.capability.AbilityCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public enum AbilityClientEventHandler {
    INSTANCE;

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            AbilityCapability.IAbilityCapability abilityCapability = AbilityHandler.INSTANCE.getAbilityCapability(player);
            if (abilityCapability != null) {
                for (Ability ability : abilityCapability.getAbilities()) {
                    ability.onRenderTick(event);
                }
            }
        }
    }
}