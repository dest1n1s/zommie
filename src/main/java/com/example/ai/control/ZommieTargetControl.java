package com.example.ai.control;

import java.util.List;

import javax.annotation.Nullable;

import com.example.Zommie;
import com.example.ZommieZombieEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

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
            entityList = mob.getWorld().getEntitiesByClass(LivingEntity.class, mob.getBoundingBox().expand(10), this::checkTarget);
        } else {
            var entityTypeOption = EntityType.get(type);
            if (entityTypeOption.isEmpty()) {
                return false;
            }
            EntityType<?> entityType = entityTypeOption.get();

            List<? extends Entity> allEntityList = mob.getWorld().getEntitiesByType(entityType, mob.getBoundingBox().expand(10), this::checkTarget);
            entityList = allEntityList.stream().filter(entity -> entity instanceof LivingEntity).map(entity -> (LivingEntity) entity).toList();
        }
        entityList = entityList.stream().sorted((a, b) -> {
            var aDistance = mob.squaredDistanceTo(a);
            var bDistance = mob.squaredDistanceTo(b);
            return Double.compare(aDistance, bDistance);
        }).toList();
        Zommie.LOGGER.info("entityList: " + entityList);
        if (entityList.size() < k) {
            return false;
        }
        mob.setTarget(entityList.get(k - 1));
        return true;
    }

    private boolean checkTarget(Entity entity) {
        if (!(entity instanceof LivingEntity)) {
            return false;
        }
        var livingEntity = (LivingEntity) entity;
        // Don't target players in creative or spectator mode.
        if (livingEntity instanceof PlayerEntity) {
            var player = (PlayerEntity) livingEntity;
            if (player.isCreative() || player.isSpectator()) {
                return false;
            }
        }
        // Don't target dead entities.
        if (!livingEntity.isAlive()) {
            return false;
        }
        // Don't target entities that are invisible.
        if (livingEntity.isInvisible()) {
            return false;
        }
        // Don't target self.
        if (livingEntity == mob) {
            return false;
        }
        return true;
    }

    /**
     * Stops targeting a living entity.
     */
    public void stopTargeting() {
        mob.setTarget(null);
    }

    @Override
    public void stop() {
        stopTargeting();
    }
}
