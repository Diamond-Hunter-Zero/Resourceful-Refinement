---
title: Sports Ball
category: Entity
status: Implemented
introduced: v0.3
recipe_type: n/a
related:
  - "[[Fox Plushie]]"
  - "[[Codebase Overview]]"
  - "[[Primary Design Doc]]"
tags:
  - entity
  - decorative
---

The Sports Ball is a decorative, persistent entity that bounces around like a bouncy ball. It is not a projectile — once in the world it lives there until picked up, bouncing off floors, walls and ceilings while retaining momentum, and rolling to a stop. Hitting it with an attack kicks it forward; right-clicking it picks it back up as an item; and a dispenser loaded with the ball item fires a live, rolling ball into the world. It comes in three types and is the mod's playful "kickabout" novelty alongside the [[Fox Plushie]].

## Gameplay Role

Pure fun/decoration. The Sports Ball gives players something to kick around a base — a physics toy with a few colours. It carries no mechanical function, but interacts with a couple of vanilla systems (dispensers spawn it; hoppers collect it) so it can be looped through simple contraptions.

## Spawning

- **Throwing the item:** right-clicking with the `sports_ball` item spawns a `SportsBallEntity` along the player's look vector at `THROW_SPEED = 0.9` (a crouched throw is scaled to ~⅓), consuming one item and applying a short use cooldown. The thrown ball inherits the item's `ball_type`.
- **Dispenser:** `SportsBallDispenseBehavior` overrides the vanilla item dispense so a dispenser spawns a live ball rolling out of its face at `LAUNCH_SPEED = 0.4`, carrying the item's `ball_type` through. It keeps the vanilla click + smoke animation.

> [!note] Implementation
> The Primary Design Doc's intended creation route is a **"Sports Ball Stuffing Sleave"** placed on a pipe outlet and filled with the *stuffing* fluid, which would transform into the ball. That Stuffing Sleave system is not implemented yet; the ball currently enters the world by throwing the item, by dispenser, or from the creative tab. Keep the Stuffing Sleave route as design intent (see [[Primary Design Doc]]).

## Behaviour

The ball runs identical physics on client and server (an `ItemEntity`-style approach) so it animates smoothly without a bespoke motion packet, with vanilla position sync correcting drift:

- **Gravity:** `0.04` per tick.
- **Bouncing:** on collision it reflects the intended velocity on the collided axis — wall/ceiling restitution `0.65`, floor restitution `0.45`. Below a minimum downward speed (`MIN_BOUNCE_VY = 0.08`) it settles instead of bouncing again.
- **Friction:** `0.975` while rolling on the ground, `0.995` in the air; horizontal speed below `EPSILON` is zeroed so it comes cleanly to rest.
- **Spin:** visual tumble is integrated from horizontal velocity (rolling-without-slipping), and the resting orientation is synced once when the ball settles (one packet per stop, not per tick) so a reloaded or newly-visible ball faces the way it landed.
- **Hopper pickup:** when it comes to rest on a hopper (checked every 4 ticks) it converts back into a `sports_ball` item — preserving its `ball_type` — and inserts itself, simulating first so it keeps rolling if the hopper is full or filtered.
- **Intangibility:** it is deliberately non-collidable and non-pushable (an 11px solid that shoved players around would be worse than one you can walk through), takes no fall damage, and is fire-immune.

## Interactions

- **Kick (attack):** `hurt()` is repurposed as the kick. The ball has no health; being hit imparts a horizontal impulse along the attacker's look (`PUNCH_IMPULSE = 1.0`) plus a small lift (`PUNCH_LIFT = 0.3`) so kicks pop it off the ground. Crouching scales the kick to ¼ for a soft nudge. Looking straight up/down pops it upward instead. Plays a wool-hit sound.
- **Pick up (right-click):** within `INTERACT_DISTANCE = 2.5` blocks, right-clicking adds a `sports_ball` item (carrying the ball's `ball_type`) to the player's inventory and removes the entity; if the inventory is full the ball is left in place rather than duplicated.
- **Change type (shears):** right-clicking with `minecraft:shears` cycles the ball's type (`(ballType + 1) % 3`) with a shear sound.

## State & Data

- **`ball_type`:** an integer (0–2) selecting the ball's appearance. It lives as the `resourceful_refinement:ball_type` data component on the item (default `0`) and as a synced entity data accessor + `BallType` NBT tag on the entity, so the type survives throw → world → pickup → dispense round-trips.
- **Resting rotation:** a synced quaternion (`Spin` NBT) capturing the orientation the ball came to rest at.

## Rendering

- `SportsBallRenderer` draws the entity with `SportsBallModel`, interpolating the tumble each partial tick; `SportsBallDebugModel` is a development/diagnostic model.
- `SportsBallItemRenderer` is the item's custom BEWLR (shared code path with the plushie item renderer).

## Implementation

- **Package:** `content/sports_ball/`
- **Entity:** `SportsBallEntity extends Entity` — full bounce/roll/spin physics, kick/pickup/shears interactions, hopper pickup; constants `RADIUS = 11/32`, `GRAVITY = 0.04`, `WALL_RESTITUTION = 0.65`, `FLOOR_RESTITUTION = 0.45`, `ROLL_FRICTION = 0.975`, `AIR_FRICTION = 0.995`, `PUNCH_IMPULSE = 1.0`, `PUNCH_LIFT = 0.3`, `INTERACT_DISTANCE = 2.5`.
- **Item:** `SportsBallItem extends Item` — stacks to 16, carries the `ball_type` component (default 0), throws the entity on use.
- **Dispense:** `SportsBallDispenseBehavior extends DefaultDispenseItemBehavior`.
- **Renderer / model:** `SportsBallRenderer`, `SportsBallModel`, `SportsBallDebugModel`, `SportsBallItemRenderer`.
- **Registry IDs:** entity type `ModEntities.SPORTS_BALL` (`sports_ball`, `MobCategory.MISC`, size 11/16, tracking range 8, update interval 3), item `ModItems.SPORTS_BALL` (`sports_ball`), data component `ModDataComponents.BALL_TYPE` (`ball_type`).

## Related

- [[Fox Plushie]] — the other "stuffed" novelty, and the other planned Stuffing Sleave output.
- [[Codebase Overview]] — where this feature sits in the registry/content map.
- [[Primary Design Doc]] — Sports Ball and Stuffing Sleave design intent.
