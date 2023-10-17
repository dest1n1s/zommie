package com.example.util.proto;

import net.minecraft.util.math.Vec3d;

public final class ProtoConverter {
    public static com.example.proto.Vec3f toProtoVec3f(Vec3d vec3d) {
		return com.example.proto.Vec3f.newBuilder().setX((float) vec3d.getX()).setY((float) vec3d.getY())
				.setZ((float) vec3d.getZ()).build();
	}

	public static com.example.proto.Item toProtoItem(net.minecraft.item.ItemStack itemStack) {
		return com.example.proto.Item.newBuilder().setName(itemStack.getName().getString())
				.setCount(itemStack.getCount()).build();
	}
}
