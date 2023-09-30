package com.example.ai.control;

import com.example.ZommieZombieEntity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;

public class ZommieAttackControl extends ZommieControl {
    private int cooldown = 0;

    public ZommieAttackControl(ZommieZombieEntity mob) {
        super(mob);
    }

    @Override
    public void tick() {
        cooldown = Math.max(0, cooldown - 1);

        var target = mob.getTarget();
        if (target == null || !target.isAlive()) {
            stopAttacking();
            return;
        }

        if (cooldown == 0 && isAttackable(target)) {
            attack(target);
            cooldown = 20;
        }
    }

    public boolean startAttacking() {
        var target = mob.getTarget();
        if (target == null || !target.isAttackable()) {
            return false;
        }
        mob.setAttacking(true);
        return true;
    }

    public void stopAttacking() {
        mob.setAttacking(false);
    }

    private boolean isAttackable(LivingEntity target) {
        var squaredDistance = mob.getSquaredDistanceToAttackPosOf(target);
        var squaredMaxAttackDistance = getSquaredMaxAttackDistance(target);
        return squaredDistance <= squaredMaxAttackDistance;
    }

    private double getSquaredMaxAttackDistance(LivingEntity entity) {
        return this.mob.getWidth() * 2.0f * (this.mob.getWidth() * 2.0f) + entity.getWidth();
    }

    private void attack(LivingEntity target) {
        mob.swingHand(Hand.MAIN_HAND);
        mob.tryAttack(target);
    }
}
