/**
 * Author: Dre Harm
 * Date: 3/25/25
 */
package com.asteroids.game;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class EnemyShip {
    Vector2 position;
    Vector2 velocity;
    float fireCooldown;
    Circle bounds;
    Array<Bullet> bulletsFired;

    public EnemyShip() {
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
        bounds = new Circle(position.x, position.y, 10);
        bulletsFired = new Array<>();
    }

    public void update(float delta, Vector2 playerPos) {
        position.add(velocity.x * delta, velocity.y * delta);
        bounds.setPosition(position);

        fireCooldown -= delta;
        if (fireCooldown <= 0) {
            fireCooldown = 2.5f;
            shootAtRandom(playerPos);
        }

        for (Bullet bullet : bulletsFired) {
            bullet.update(delta);
        }
    }

    private void shootAtRandom(Vector2 playerPos) {
        Vector2 direction = new Vector2(playerPos).sub(position).nor();
        direction.rotateDeg(MathUtils.random(-30f, 30f));
        bulletsFired.add(new Bullet(position.cpy(), direction.scl(200)));
    }

    public void draw(ShapeRenderer renderer) {
        renderer.identity();
        renderer.translate(position.x, position.y, 0);
        renderer.setColor(1, 1, 1, 1);
        renderer.rect(-10, -5, 20, 10); // Simple rectangle for the ship
    }

    public boolean isOffScreen() {
        return position.x < -30 || position.x > 830;
    }

    public boolean collidesWithPlayer(Vector2 playerPos) {
        return bounds.overlaps(new Circle(playerPos.x, playerPos.y, 10));
    }

    public Array<Bullet> getBullets() {
        return bulletsFired;
    }
}
