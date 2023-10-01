package com.example;

import java.util.Map;

import org.joml.Vector3d;

import com.example.mixin.client.BlockBufferBuilderStorageAccessor;
import com.example.mixin.client.BufferBuilderAccessor;
import com.example.mixin.client.BufferBuilderStorageAccessor;
import com.example.mixin.client.ImmediateAccessor;
import com.example.mixin.client.OutlineVertexConsumerProviderAccessor;
import com.example.mixin.client.WorldRendererAccessor;
import com.example.mixinInterfaces.FramebufferOverrider;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Util;

public class EntityViewRenderer {
    private final Framebuffer framebuffer;
    private final BufferBuilderStorage bufferBuilders;
    private final Entity entity;

    public EntityViewRenderer(Entity entity, Framebuffer framebuffer, BufferBuilderStorage bufferBuilders) {
        this.framebuffer = framebuffer;
        this.bufferBuilders = bufferBuilders;
        this.entity = entity;
    }

    private static void cloneBufferBuilder(BufferBuilder from, BufferBuilder to) {
        BufferBuilderAccessor fromAccessor = (BufferBuilderAccessor) from;
        BufferBuilderAccessor toAccessor = (BufferBuilderAccessor) to;

        // Replace each field in 'to' with the corresponding one in 'from'
        toAccessor.setBuiltBufferCount(fromAccessor.getBuiltBufferCount());
        toAccessor.setBatchOffset(fromAccessor.getBatchOffset());
        toAccessor.setElementOffset(fromAccessor.getElementOffset());
        toAccessor.setVertexCount(fromAccessor.getVertexCount());
        toAccessor.setCurrentElement(fromAccessor.getCurrentElement());
        toAccessor.setCurrentElementId(fromAccessor.getCurrentElementId());
        toAccessor.invokeSetFormat(fromAccessor.getFormat());
        toAccessor.setDrawMode(fromAccessor.getDrawMode());
        toAccessor.setCanSkipElementChecks(fromAccessor.isCanSkipElementChecks());
        toAccessor.setHasOverlay(fromAccessor.hasOverlay());
        toAccessor.setBuilding(fromAccessor.isBuilding());
        toAccessor.setSortingPrimitiveCenters(fromAccessor.getSortingPrimitiveCenters());
        toAccessor.setSorter(fromAccessor.getSorter());
        toAccessor.setHasNoVertexBuffer(fromAccessor.hasNoVertexBuffer());
    }

    private static <Key> void cloneBufferBuilderMap(Map<Key, BufferBuilder> from,
            Map<Key, BufferBuilder> to) {
        for (Key layer : from.keySet()) {
            BufferBuilder fromBuilder = from.get(layer);
            if (to.containsKey(layer)) {
                BufferBuilder toBuilder = to.get(layer);
                cloneBufferBuilder(fromBuilder, toBuilder);
                to.put(layer, toBuilder);
            } else
                to.put(layer, fromBuilder);
        }
        // Remove any extra layers
        for (Key layer : to.keySet()) {
            if (!from.containsKey(layer)) {
                to.remove(layer);
            }
        }
    }

    private static void cloneImmediate(VertexConsumerProvider.Immediate from,
            VertexConsumerProvider.Immediate to) {
        ImmediateAccessor fromAccessor = (ImmediateAccessor) from;
        ImmediateAccessor toAccessor = (ImmediateAccessor) to;

        cloneBufferBuilder(fromAccessor.getFallbackBuffer(), toAccessor.getFallbackBuffer());
        cloneBufferBuilderMap(fromAccessor.getLayerBuffers(), toAccessor.getLayerBuffers());
        toAccessor.setCurrentLayer(fromAccessor.getCurrentLayer());
        toAccessor.getActiveConsumers().clear();
        toAccessor.getActiveConsumers().addAll(fromAccessor.getActiveConsumers());
    }

    private static void cloneBufferBuilderStorage(BufferBuilderStorage from, BufferBuilderStorage to) {
        Map<RenderLayer, BufferBuilder> fromBlockBuilders = ((BlockBufferBuilderStorageAccessor) to
                .getBlockBufferBuilders())
                .getBuilders();
        Map<RenderLayer, BufferBuilder> toBlockBuilders = ((BlockBufferBuilderStorageAccessor) from
                .getBlockBufferBuilders())
                .getBuilders();
        cloneBufferBuilderMap(fromBlockBuilders, toBlockBuilders);
        cloneBufferBuilderMap(((BufferBuilderStorageAccessor) from).getEntityBuilders(),
                ((BufferBuilderStorageAccessor) to).getEntityBuilders());
        cloneImmediate(from.getEntityVertexConsumers(), to.getEntityVertexConsumers());
        cloneImmediate(from.getEffectVertexConsumers(), to.getEffectVertexConsumers());
        var fromOutlineConsumers = ((OutlineVertexConsumerProviderAccessor) from.getOutlineVertexConsumers());
        var toOutlineConsumers = ((OutlineVertexConsumerProviderAccessor) to.getOutlineVertexConsumers());
        cloneImmediate(fromOutlineConsumers.getParent(), toOutlineConsumers.getParent());
        cloneImmediate(fromOutlineConsumers.getPlainDrawer(), toOutlineConsumers.getPlainDrawer());
    }

    public NativeImage renderView() {
        var client = MinecraftClient.getInstance();

        // Save old
        var oldBufferBuilders = new BufferBuilderStorage();
        cloneBufferBuilderStorage(client.getBufferBuilders(), oldBufferBuilders);

        var oldViewBobbing = client.options.getBobView().getValue();
        var oldCapturedFrustum = ((WorldRendererAccessor) client.worldRenderer).getCapturedFrustum();
        var oldCapturedFrustumPosition = new Vector3d(0, 0, 0);
        oldCapturedFrustumPosition
                .set(((WorldRendererAccessor) client.worldRenderer).getCapturedFrustumPosition());

        // Override
        ((FramebufferOverrider) client).setOverrideFramebuffer(this.framebuffer);
        cloneBufferBuilderStorage(this.bufferBuilders, client.getBufferBuilders());
        client.setCameraEntity(entity);
        client.gameRenderer.setRenderHand(false);
        client.options.getBobView().setValue(true);

        this.framebuffer.beginWrite(true);
        var matrices = new MatrixStack();
        var tickDelta = 0;
        client.gameRenderer.renderWorld(tickDelta, Util.getMeasuringTimeNano(), matrices);
        this.framebuffer.endWrite();

        // Reset
        client.options.getBobView().setValue(oldViewBobbing);
        client.gameRenderer.setRenderHand(true);
        client.setCameraEntity(client.player);
        ((WorldRendererAccessor) client.worldRenderer).setCapturedFrustum(oldCapturedFrustum);
        ((WorldRendererAccessor) client.worldRenderer)
                .getCapturedFrustumPosition().set(oldCapturedFrustumPosition);
        cloneBufferBuilderStorage(client.getBufferBuilders(), this.bufferBuilders);
        cloneBufferBuilderStorage(oldBufferBuilders, client.getBufferBuilders());
        ((FramebufferOverrider) client).setOverrideFramebuffer(null);
        return ScreenshotRecorder.takeScreenshot(this.framebuffer);
    }
}
