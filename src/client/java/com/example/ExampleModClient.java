package com.example;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.WindowFramebuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ZombieEntityRenderer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Util;
import com.example.mixin.client.BlockBufferBuilderStorageAccessor;
import com.example.mixin.client.BufferBuilderAccessor;
import com.example.mixin.client.BufferBuilderStorageAccessor;
import com.example.mixin.client.ImmediateAccessor;
import com.example.mixin.client.OutlineVertexConsumerProviderAccessor;

public class ExampleModClient implements ClientModInitializer {
	Map<Entity, Context> contexts = new HashMap<>();

	private static File getScreenshotFilename(File directory) {
		String string = Util.getFormattedCurrentTime();
		int i = 1;
		File file;
		while ((file = new File(directory, string + (String) (i == 1 ? "" : "_" + i) + ".png")).exists()) {
			++i;
		}
		return file;
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
		toAccessor.setFormat(fromAccessor.getFormat());
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

	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as
		// rendering.
		EntityRendererRegistry.register(ExampleMod.EXAMPLE_ZOMBIE, (context) -> {
			return new ZombieEntityRenderer(context);
		});

		ClientPlayNetworking.registerGlobalReceiver(ExampleMod.RENDER_ZOMBIE_VIEW_PACKET_ID,
				(client, handler, buf, responseSender) -> {
					int entityId = buf.readInt();

					client.execute(() -> {
						// Get entity
						var entity = client.world.getEntityById(entityId);
						if (entity == null) {
							return;
						}

						Window window = client.getWindow();

						// Get context
						if (!contexts.containsKey(entity)) {
							contexts.put(entity, new Context(new WindowFramebuffer(window.getFramebufferWidth(),
									window.getFramebufferHeight()), new BufferBuilderStorage()));
						}
						Context context = contexts.get(entity);

						// Save old
						var oldBufferBuilders = new BufferBuilderStorage();
						cloneBufferBuilderStorage(client.getBufferBuilders(), oldBufferBuilders);

						// Override
						OverrideFramebuffer.framebuffer = context.getFramebuffer();
						cloneBufferBuilderStorage(context.getBufferBuilders(), client.getBufferBuilders());
						client.setCameraEntity(entity);
						client.gameRenderer.setRenderHand(false);

						// RenderSystem.clear(GlConst.GL_DEPTH_BUFFER_BIT | GlConst.GL_COLOR_BUFFER_BIT,
						// false);
						context.getFramebuffer().beginWrite(true);
						// BackgroundRenderer.clearFog();
						// RenderSystem.enableCull();

						var matrices = new MatrixStack();
						var tickDelta = 1;
						client.gameRenderer.renderWorld(tickDelta, Util.getMeasuringTimeNano(), matrices);
						context.getFramebuffer().endWrite();

						// Reset
						client.gameRenderer.setRenderHand(true);
						client.setCameraEntity(client.player);
						cloneBufferBuilderStorage(client.getBufferBuilders(), context.getBufferBuilders());
						cloneBufferBuilderStorage(oldBufferBuilders, client.getBufferBuilders());
						OverrideFramebuffer.framebuffer = null;
						NativeImage image = ScreenshotRecorder.takeScreenshot(context.getFramebuffer());

						// Create a new thread to write to file
						var thread = new Thread(() -> {
							// Write to file
							File viewDirectory = new File(client.runDirectory, "zommieView");
							if (!viewDirectory.exists()) {
								viewDirectory.mkdir();
							}
							File file = getScreenshotFilename(viewDirectory);
							try {
								image.writeTo(file);
							} catch (IOException e) {
								e.printStackTrace();
							}
						});
						thread.start();

					});
				});
	}

	public class Context {
		private final Framebuffer framebuffer;
		private final BufferBuilderStorage bufferBuilders;

		public Context(Framebuffer framebuffer, BufferBuilderStorage bufferBuilders) {
			this.framebuffer = framebuffer;
			this.bufferBuilders = bufferBuilders;
		}

		public Framebuffer getFramebuffer() {
			return framebuffer;
		}

		public BufferBuilderStorage getBufferBuilders() {
			return bufferBuilders;
		}
	}
}