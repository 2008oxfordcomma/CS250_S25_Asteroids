package com.asteroids.game;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Explosion {
        Vector2 position;
        Array<Vector2> directions = new Array<>();
        float time = 0f;
        float duration = 0.4f;

        Explosion(Vector2 position) {
            this.position = position;
            for (int i = 0; i < 16; i++) {
                float angle = i * 360f / 16;
                directions.add(new Vector2(MathUtils.cosDeg(angle), MathUtils.sinDeg(angle)));
            }
        }

        boolean isFinished() {
            return time >= duration;
        }

        void update(float delta) {
            time += delta;
        }

        void draw(ShapeRenderer renderer) {
            float alpha = 1f - (time / duration);
            renderer.setColor(1f, 1f, 1f, alpha);
            for (Vector2 dir : directions) {
                float len = time * 60f;
                renderer.line(position.x, position.y, position.x + dir.x * len, position.y + dir.y * len);
            }
        }
}
