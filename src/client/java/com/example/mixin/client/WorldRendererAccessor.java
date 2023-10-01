package com.example.mixin.client;

import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.WorldRenderer;

@Mixin(WorldRenderer.class)
public interface WorldRendererAccessor {
    @Accessor
    Frustum getCapturedFrustum();

    @Accessor
    void setCapturedFrustum(Frustum frustum);

    @Accessor
    Vector3d getCapturedFrustumPosition();
}
