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

    public Asteroid(Vector2 pos, int size) {
        position = pos;
        velocity = new Vector2(MathUtils.random(-50, 50), MathUtils.random(-50, 50));
        this.size = size;
        rotationSpeed = MathUtils.random(-100, 100);
    }

    public void update(float delta) {
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
