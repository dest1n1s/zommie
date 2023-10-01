package com.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.proto.ModelInferenceGrpc;
import com.example.proto.PredictRequest;
import com.example.proto.PredictResponse;
import com.google.protobuf.ByteString;

import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

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

	private static final String HOST = "localhost";
	private static final int PORT = 50051;
	private static ManagedChannel channel;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		FabricDefaultAttributeRegistry.register(EXAMPLE_ZOMBIE, ZommieZombieEntity.createZombieAttributes());

		channel = Grpc.newChannelBuilder(String.format("%s:%d", HOST, PORT), InsecureChannelCredentials.create())
				.build();

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

					var requestBuilder = PredictRequest.newBuilder()
							.setView(ByteString.copyFrom(imageBytes))
							.setPosition(toProtoVec3f(mob.getPos()))
							.setDirection(toProtoVec3f(mob.getRotationVector()))
							.setVelocity(toProtoVec3f(mob.getVelocity()))
							.setHealth(mob.getHealth())
							.setHand(toProtoItem(mob.getMainHandStack()))
							.setArmor(com.example.proto.Armor.newBuilder()
									.setHead(toProtoItem(mob.getEquippedStack(EquipmentSlot.HEAD)))
									.setChest(toProtoItem(mob.getEquippedStack(EquipmentSlot.CHEST)))
									.setLegs(toProtoItem(mob.getEquippedStack(EquipmentSlot.LEGS)))
									.setFeet(toProtoItem(mob.getEquippedStack(EquipmentSlot.FEET))).build());
					if (mob.getTarget() != null) {
						var target = mob.getTarget();
						requestBuilder.setTarget(com.example.proto.Entity.newBuilder()
								.setId(target.getId())
								.setPosition(toProtoVec3f(target.getPos()))
								.setDirection(toProtoVec3f(target.getRotationVector()))
								.setVelocity(toProtoVec3f(target.getVelocity()))
								.setHealth(target.getHealth())
								.setHand(toProtoItem(target.getMainHandStack()))
								.setArmor(com.example.proto.Armor.newBuilder()
										.setHead(toProtoItem(target.getEquippedStack(EquipmentSlot.HEAD)))
										.setChest(toProtoItem(target.getEquippedStack(EquipmentSlot.CHEST)))
										.setLegs(toProtoItem(target.getEquippedStack(EquipmentSlot.LEGS)))
										.setFeet(toProtoItem(target.getEquippedStack(EquipmentSlot.FEET))).build())
								.build());
					}
					var request = requestBuilder.build();


					var nonBlockingStub = ModelInferenceGrpc.newStub(channel);
					var responseObserver = new StreamObserver<PredictResponse>() {
						@Override
						public void onNext(PredictResponse value) {
							var mob = searchZommieZombieEntityById(server, entityId);
							if (mob == null) {
								return;
							}
							var actions = value.getActionsList();
							mob.executeActions(actions);
						}

						@Override
						public void onError(Throwable t) {
							Zommie.LOGGER.error("PredictResponse error: " + t);
						}

						@Override
						public void onCompleted() {
							Zommie.LOGGER.info("PredictResponse completed");
						}
					};
					nonBlockingStub.predict(request, responseObserver);
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

	private static com.example.proto.Vec3f toProtoVec3f(Vec3d vec3d) {
		return com.example.proto.Vec3f.newBuilder().setX((float) vec3d.getX()).setY((float) vec3d.getY())
				.setZ((float) vec3d.getZ()).build();
	}

	private static com.example.proto.Item toProtoItem(net.minecraft.item.ItemStack itemStack) {
		return com.example.proto.Item.newBuilder().setName(itemStack.getName().getString())
				.setCount(itemStack.getCount()).build();
	}
} 