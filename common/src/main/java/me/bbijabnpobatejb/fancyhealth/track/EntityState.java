package me.bbijabnpobatejb.fancyhealth.track;

import net.minecraft.world.entity.LivingEntity;

public class EntityState {

    public LivingEntity entity;
    public boolean init;
    public float health;
    public float absorption;
    public float max;
    public int seenTick;
    public int changeTick = -100000;
    public int lookTick = -100000;
    public int kindTick = -100000;
    public DamageKind kind = DamageKind.PHYSICAL;
    public int critTick = -100000;
    public int mineTick = -100000;
    public boolean fromScore;
    public boolean visible = true;
    public float occl = -1f;
    public float alpha;
    public final BarAnim anim = new BarAnim();
}
