/**
 * Author: Dre Harm
 * Date: 3/25/25
 */
package com.asteroids.game;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class EnemyShip {
    Vector2 position;
    Vector2 velocity;
    float fireCooldown;

    public EnemyShip() {
        // Spawn randomly from left or right
        float y = MathUtils.random(50, 550);
        boolean fromLeft = MathUtils.randomBoolean();

        if (fromLeft) {
            position = new Vector2(-20, y);
            velocity = new Vector2(60, 0);
        } else {
            position = new Vector2(820, y);
            velocity = new Vector2(-60, 0);
        }
        fireCooldown = 2.0f;
    }

    public void update(float delta, Vector2 playerPos, Array<Bullet> bullets) {
        position.add(velocity.x * delta, velocity.y * delta);

        fireCooldown -= delta;
        if (fireCooldown <= 0) {
            fireCooldown = 2.5f;
            shootAtRandom(playerPos, bullets);
        }
    }

    private void shootAtRandom(Vector2 playerPos, Array<Bullet> bullets) {
        // Fire loosely in player's direction with random error
        Vector2 direction = new Vector2(playerPos).sub(position).nor();
        direction.rotateDeg(MathUtils.random(-30f, 30f));
        bullets.add(new Bullet(
            position.cpy(),
            direction.scl(200)
        ));
    }

    public void draw(ShapeRenderer renderer) {
        renderer.identity();
        renderer.translate(position.x, position.y, 0);
        renderer.setColor(1, 1, 1, 1);
        renderer.rect(-10, -5, 20, 10); // Simple rectangle ship
    }

    public boolean isOffScreen() {
        return position.x < -30 || position.x > 830;
    }
}
