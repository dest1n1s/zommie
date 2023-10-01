package com.example.ai.control;

import java.util.List;

import javax.annotation.Nullable;

import com.example.ZommieZombieEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;

public class ZommieTargetControl extends ZommieControl {

    public ZommieTargetControl(ZommieZombieEntity mob) {
        super(mob);
    }

    @Override
    public void tick() {
    }
    
    /**
     * Starts targeting a living entity, filtering by k-th closest and type.
     * @param k The k-th closest entity to target.
     * @param type The type of entity to target.
     */
    public boolean startTargeting(int k, @Nullable String type) {
        List<LivingEntity> entityList;
        if(type == null) {
            entityList = mob.getWorld().getEntitiesByClass(LivingEntity.class, mob.getBoundingBox().expand(10), null);
        } else {
            var entityTypeOption = EntityType.get(type);
            if (entityTypeOption.isEmpty()) {
                return false;
            }
            EntityType<?> entityType = entityTypeOption.get();

            List<? extends Entity> allEntityList = mob.getWorld().getEntitiesByType(entityType, mob.getBoundingBox().expand(10), null);
            entityList = allEntityList.stream().filter(entity -> entity instanceof LivingEntity).map(entity -> (LivingEntity) entity).toList();
        }
        entityList = entityList.stream().filter(entity -> entity.isAlive()).sorted((a, b) -> {
            var aDistance = mob.squaredDistanceTo(a);
            var bDistance = mob.squaredDistanceTo(b);
            return Double.compare(aDistance, bDistance);
        }).toList();
        if (entityList.size() <= k) {
            return false;
        }
        mob.setTarget(entityList.get(k));
        return true;
    }

    /**
     * Stops targeting a living entity.
     */
    public void stopTargeting() {
        mob.setTarget(null);
    }
}
