package com.example;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gl.WindowFramebuffer;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.entity.ZombieEntityRenderer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.Window;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;

public class ZommieClient implements ClientModInitializer {
	Map<Entity, EntityViewRenderer> entityViewRendererMap = new HashMap<>();
	FileSystem inMemoryFileSystem = Jimfs.newFileSystem(Configuration.unix());
	Path viewDirectory = inMemoryFileSystem.getPath("/views");

	@Override
	public void onInitializeClient() {
		try {
			Files.createDirectories(viewDirectory);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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

						try {
							// Write image to in-memory file
							Path viewFile = inMemoryFileSystem.getPath("/").resolve("view.png");
							if (Files.exists(viewFile)) {
								Files.delete(viewFile);
							}

							image.writeTo(viewFile);
							byte[] imageBytes = Files.readAllBytes(viewFile);

							PacketByteBuf imageBuf = PacketByteBufs.create();
							imageBuf.writeInt(entityId);

							// Add image
							imageBuf.writeByteArray(imageBytes);
							ClientPlayNetworking.send(Zommie.RENDER_ZOMBIE_VIEW_PACKET_ID, imageBuf);
						} catch (IOException e) {
							e.printStackTrace();
						}
					});
				});
	}
}