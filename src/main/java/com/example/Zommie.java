package com.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;

public class Zommie implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("modid");

	public static final Identifier RENDER_ZOMBIE_VIEW_PACKET_ID = new Identifier("zommie", "render_zombie_view");

	public static final EntityType<ZommieZombieEntity> EXAMPLE_ZOMBIE = Registry.register(
			Registries.ENTITY_TYPE,
			new Identifier("zommie", "zombie"),
			FabricEntityTypeBuilder.create(SpawnGroup.MONSTER, ZommieZombieEntity::new)
					.dimensions(EntityDimensions.fixed(0.6f, 1.95f)).build());

	public static final String HOST = "localhost";
	public static final int PORT = 50051;
	public static final ManagedChannel channel = Grpc.newChannelBuilder(String.format("%s:%d", HOST, PORT), InsecureChannelCredentials.create())
				.build();

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		FabricDefaultAttributeRegistry.register(EXAMPLE_ZOMBIE, ZommieZombieEntity.createZombieAttributes());

		ServerPlayNetworking.registerGlobalReceiver(RENDER_ZOMBIE_VIEW_PACKET_ID,
				(server, player, handler, buf, responseSender) -> {
					int entityId = buf.readInt();
					byte[] imageBytes = buf.readByteArray();
					// server.execute(() -> {
					// 	try {
					// 		// Write image to file
					// 		Path zommieView = server.getRunDirectory().toPath().resolve("zommieView.png");
					// 		Files.write(zommieView, imageBytes);
					// 	} catch (IOException e) {
					// 		e.printStackTrace();
					// 	}
					// });

					ZommieZombieEntity mob = searchZommieZombieEntityById(server, entityId);

					if (mob == null) {
						return;
					}

					mob.onClientRendered(imageBytes);
				});
	}

	private static @Nullable ZommieZombieEntity searchZommieZombieEntityById(MinecraftServer server, int id) {
		for (ServerWorld world : server.getWorlds()) {
			Entity entity = world.getEntityById(id);
			if (entity instanceof ZommieZombieEntity) {
				return (ZommieZombieEntity) entity;
			}
		}
		return null;
	}

	
} 