package com.example.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.OutlineVertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider;

@Mixin(OutlineVertexConsumerProvider.class)
public interface OutlineVertexConsumerProviderAccessor {
    @Accessor
    VertexConsumerProvider.Immediate getParent();

    @Accessor
    VertexConsumerProvider.Immediate getPlainDrawer();
}
