/**
 * Author: Dre Harm
 * Date: 2/4/25
 */

package com.asteroids.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Player {
    Vector2 position;
    Vector2 velocity;
    float angle;
    float speed = 500;
    float fireCooldown;
    int health = 3;
    boolean isInvincible = false;
    float invincibilityTimer = 0f;
    float rotationSpeed = 180f;
    float invincibilityBlinkTimer = 0f; // tracks how long we've been blinking

    public Player() {
        position = new Vector2(400, 300);
        velocity = new Vector2();
        angle = 90; // starts pointing straight up
    }

    public void rotate(float direction) {
        angle += direction * rotationSpeed * Gdx.graphics.getDeltaTime();
    }

    public void thrust() {
        velocity.x += MathUtils.cosDeg(angle) * speed * Gdx.graphics.getDeltaTime();
        velocity.y += MathUtils.sinDeg(angle) * speed * Gdx.graphics.getDeltaTime();
    }

    public void shoot(Array<Bullet> bullets) {
        if(fireCooldown <= 0) {
            bullets.add(new Bullet(
                position.cpy(),
                new Vector2(MathUtils.cosDeg(angle) * 500, MathUtils.sinDeg(angle) * 500)
            ));
            fireCooldown = 0.2f;
        }
    }

    public void update(float delta) {
        position.add(velocity.x * delta, velocity.y * delta);
        velocity.scl(0.98f);
        fireCooldown -= delta;

        // If invincible, decrement the timer & track blink
        if (isInvincible) {
            invincibilityTimer -= delta;
            invincibilityBlinkTimer += delta; // used for blinking

            if (invincibilityTimer <= 0) {
                isInvincible = false;
                invincibilityBlinkTimer = 0; // reset once done blinking
            }
        }
    }

    public void takeDamage() {
        if (!isInvincible) {
            health--;
            isInvincible = true;
            invincibilityTimer = 2.0f;

            // respawn player at center if hit
            if (health > 0) {
                position.set(400, 300);
                velocity.set(0, 0);
            }
        }
    }

    public void draw(ShapeRenderer renderer) {
        boolean shouldDraw = true;
        if (isInvincible) {
            float blinkFrequency = 5f;
            // if the floor of blinkFrequency * total time is even, draw; else skip
            int blinkState = (int)(invincibilityBlinkTimer * blinkFrequency);
            shouldDraw = (blinkState % 2 == 0);
        }

        if (shouldDraw) {
            renderer.identity();
            renderer.translate(position.x, position.y, 0);
            renderer.setColor(231/255f, 255/255f, 238/255f, 1.0f); // #e7ffee
            renderer.rotate(0, 0, 1, angle + 90);
            renderer.triangle(-10, 10, 0, -15, 10, 10);
        }
    }
}
