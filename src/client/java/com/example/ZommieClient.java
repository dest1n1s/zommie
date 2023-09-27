package com.example;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.gl.WindowFramebuffer;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.entity.ZombieEntityRenderer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.Window;
import net.minecraft.entity.Entity;
import net.minecraft.util.Util;

public class ZommieClient implements ClientModInitializer {
	Map<Entity, EntityViewRenderer> entityViewRendererMap = new HashMap<>();

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
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as
		// rendering.
		EntityRendererRegistry.register(Zommie.EXAMPLE_ZOMBIE, (context) -> {
			return new ZombieEntityRenderer(context);
		});

		ClientPlayNetworking.registerGlobalReceiver(Zommie.RENDER_ZOMBIE_VIEW_PACKET_ID,
				(client, handler, buf, responseSender) -> {
					int entityId = buf.readInt();

					client.execute(() -> {
						// Get entity
						var entity = client.world.getEntityById(entityId);
						if (entity == null) {
							return;
						}

						Window window = client.getWindow();

						// Get renderer
						if (!entityViewRendererMap.containsKey(entity)) {
							entityViewRendererMap
									.put(entity,
											new EntityViewRenderer(entity,
													new WindowFramebuffer(window.getFramebufferWidth(),
															window.getFramebufferHeight()),
													new BufferBuilderStorage()));
						}

						EntityViewRenderer renderer = entityViewRendererMap.get(entity);

						NativeImage image = renderer.renderView();

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
}