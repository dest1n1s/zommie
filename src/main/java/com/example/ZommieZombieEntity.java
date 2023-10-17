package com.example;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import com.example.ai.ActionControlledGoal;
import com.example.proto.Action;
import com.example.proto.ModelInferenceGrpc;
import com.example.proto.PredictRequest;
import com.example.proto.PredictResponse;
import com.example.util.proto.ProtoConverter;
import com.google.protobuf.ByteString;

import io.grpc.stub.StreamObserver;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class ZommieZombieEntity extends ZombieEntity {
    ActionControlledGoal controlledGoal;

    public ZommieZombieEntity(EntityType<? extends ZommieZombieEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initGoals() {
        controlledGoal = new ActionControlledGoal(this);
        this.goalSelector.add(0, controlledGoal);
    }

    @Override
    public void tick() {
        super.tick();
        // Get tick count
        if (this.age % 20 == 0) {
            // Return if not server
            var world = this.getWorld();
            if (world.isClient) {
                return;
            }

            PacketByteBuf buf = PacketByteBufs.create();
            // Write entity id
            buf.writeInt(this.getId());

            // Send packet to the first player
            var playerIterator = PlayerLookup.tracking((ServerWorld) world, this.getBlockPos()).iterator();
            if (!playerIterator.hasNext()) {
                return;
            }
            
            ServerPlayNetworking.send(playerIterator.next(), Zommie.RENDER_ZOMBIE_VIEW_PACKET_ID, buf);
        }
    }

    public void executeActions(Iterable<Action> actions) {
        controlledGoal.executeActions(actions);
    }

    public void onClientRendered(byte[] imageBytes) {
        var requestBuilder = PredictRequest.newBuilder()
							.setView(ByteString.copyFrom(imageBytes))
							.setPosition(ProtoConverter.toProtoVec3f(this.getPos()))
							.setDirection(ProtoConverter.toProtoVec3f(this.getRotationVector()))
							.setVelocity(ProtoConverter.toProtoVec3f(this.getVelocity()))
							.setHealth(this.getHealth())
							.setHand(ProtoConverter.toProtoItem(this.getMainHandStack()))
							.setArmor(com.example.proto.Armor.newBuilder()
									.setHead(ProtoConverter.toProtoItem(this.getEquippedStack(EquipmentSlot.HEAD)))
									.setChest(ProtoConverter.toProtoItem(this.getEquippedStack(EquipmentSlot.CHEST)))
									.setLegs(ProtoConverter.toProtoItem(this.getEquippedStack(EquipmentSlot.LEGS)))
									.setFeet(ProtoConverter.toProtoItem(this.getEquippedStack(EquipmentSlot.FEET))).build());
					if (this.getTarget() != null) {
						var target = this.getTarget();
						requestBuilder.setTarget(com.example.proto.Entity.newBuilder()
								.setId(target.getId())
								.setPosition(ProtoConverter.toProtoVec3f(target.getPos()))
								.setDirection(ProtoConverter.toProtoVec3f(target.getRotationVector()))
								.setVelocity(ProtoConverter.toProtoVec3f(target.getVelocity()))
								.setHealth(target.getHealth())
								.setHand(ProtoConverter.toProtoItem(target.getMainHandStack()))
								.setArmor(com.example.proto.Armor.newBuilder()
										.setHead(ProtoConverter.toProtoItem(target.getEquippedStack(EquipmentSlot.HEAD)))
										.setChest(ProtoConverter.toProtoItem(target.getEquippedStack(EquipmentSlot.CHEST)))
										.setLegs(ProtoConverter.toProtoItem(target.getEquippedStack(EquipmentSlot.LEGS)))
										.setFeet(ProtoConverter.toProtoItem(target.getEquippedStack(EquipmentSlot.FEET))).build())
								.build());
					}
					var request = requestBuilder.build();


					var nonBlockingStub = ModelInferenceGrpc.newStub(Zommie.channel);
					var responseObserver = new StreamObserver<PredictResponse>() {
						@Override
						public void onNext(PredictResponse value) {
							var actions = value.getActionsList();
							ZommieZombieEntity.this.executeActions(actions);
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
    }
}
