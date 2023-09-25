package com.example;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.Util;
import net.minecraft.world.World;

import java.io.File;
import java.io.IOException;

import com.example.mixin.ExampleMixin;
import com.example.mixin.MinecraftClientAccessor;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.WindowFramebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.client.util.math.MatrixStack;

public class ExampleZombie extends ZombieEntity {

    public ExampleZombie(EntityType<? extends ExampleZombie> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new PersistantJumpGoal(this));
    }

    private static File getScreenshotFilename(File directory) {
        String string = Util.getFormattedCurrentTime();
        int i = 1;
        File file;
        while ((file = new File(directory, string + (String) (i == 1 ? "" : "_" + i) + ".png")).exists()) {
            ++i;
        }
        return file;
    }

    @Override
    public void tick() {
        super.tick();
        // Get tick count
        if (this.age % 20 == 0) {
            System.out.println("Tick");
            // If not render thread, return
            if (!MinecraftClient.getInstance().isOnThread()) {
                return;
            }
            MinecraftClient client = MinecraftClient.getInstance();
            Framebuffer framebuffer = new WindowFramebuffer(client.getWindow().getFramebufferWidth(),
                    client.getWindow().getFramebufferHeight());

            // Override
            OverrideFramebuffer.framebuffer = framebuffer;
            client.setCameraEntity(this);
            client.gameRenderer.setRenderHand(false);

            RenderSystem.clear(GlConst.GL_DEPTH_BUFFER_BIT | GlConst.GL_COLOR_BUFFER_BIT, false);
            framebuffer.beginWrite(true);
            BackgroundRenderer.clearFog();
            RenderSystem.enableCull();

            var matrices = new MatrixStack();
            var tickDelta = client.getTickDelta();
            client.gameRenderer.renderWorld(tickDelta, Util.getMeasuringTimeNano(), matrices);

            // // Render hand
            // client.gameRenderer.firstPersonRenderer.renderItem(tickDelta, matrices,
            // client.getBufferBuilders().getEntityVertexConsumers(), this,
            // client.getEntityRenderDispatcher().getLight(this, tickDelta));

            DrawContext drawContext = new DrawContext(client, client.getBufferBuilders().getEntityVertexConsumers());
            drawContext.draw();

            framebuffer.endWrite();

            // Reset
            client.gameRenderer.setRenderHand(true);
            client.setCameraEntity(client.player);
            OverrideFramebuffer.framebuffer = null;

            // Write to file
            File viewDirectory = new File(client.runDirectory, "zommieView");
            if (!viewDirectory.exists()) {
                viewDirectory.mkdir();
            }
            File file = getScreenshotFilename(viewDirectory);
            NativeImage image = ScreenshotRecorder.takeScreenshot(framebuffer);
            try {
                image.writeTo(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
