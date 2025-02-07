/**
 * Author: Dre Harm
 * Date: 2/4/25
 */

package com.asteroids.game;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;


public class Asteroid {
    Vector2 position;
    Vector2 velocity;
    int size; // 3 = large, 2 = medium, 1 = small
    float rotation;
    float rotationSpeed;
    float noiseTime;

    public Asteroid(Vector2 pos, int size) {
        position = pos;
        this.size = size;
        rotationSpeed = MathUtils.random(-100, 100);
        noiseTime = MathUtils.random(1000); // To avoid synchronization, using random start.

        float angle = MathUtils.random(0f, 360f);
        float speed = MathUtils.random(30, 60);
        velocity = new Vector2(MathUtils.cosDeg(angle) * speed, MathUtils.sinDeg(angle) * speed);
    }

    public void update(float delta) {
        noiseTime += delta * 0.5f; // Increment time slowly for smoother change
        float noiseX = PerlinNoise.noise(noiseTime) * 10;
        float noiseY = PerlinNoise.noise(noiseTime + 100) * 10; // offset gives variation

        velocity.x += noiseX * delta;
        velocity.y += noiseY * delta;

        position.add(velocity.x * delta, velocity.y * delta);
        rotation += rotationSpeed * delta;
    }

    public void draw(ShapeRenderer renderer) {
        renderer.identity();
        renderer.translate(position.x, position.y, 0);
        renderer.rotate(0, 0, 1, rotation);

        renderer.setColor(0.7216f, 0.7608f, 0.7255f, 1.0f);
        renderer.circle(0, 0, size * 10, 12);
    }

    public Circle getBounds() {

        return new Circle(position.x, position.y, size * 10);
    }
}
