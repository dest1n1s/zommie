package com.example.ai.control;

import com.example.ZommieZombieEntity;

public abstract class ZommieControl {
    protected final ZommieZombieEntity mob;

    public ZommieControl(ZommieZombieEntity mob) {
        this.mob = mob;
    }

    public abstract void tick();
}
