package com.example.mixin.client;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;

@Mixin(VertexConsumerProvider.Immediate.class)
public interface ImmediateAccessor {
    @Accessor
    BufferBuilder getFallbackBuffer();

    @Accessor
    Map<RenderLayer, BufferBuilder> getLayerBuffers();

    @Accessor
    Optional<RenderLayer> getCurrentLayer();

    @Accessor
    void setCurrentLayer(Optional<RenderLayer> layer);

    @Accessor
    Set<BufferBuilder> getActiveConsumers();
}
