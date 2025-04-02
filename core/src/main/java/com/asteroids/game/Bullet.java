/**
 * Author: Dre Harm
 * Date: 2/4/25
 */

package com.asteroids.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public class Bullet {
    Vector2 position;
    Vector2 velocity;
    float lifetime = 1.5f;

    public Bullet(Vector2 pos, Vector2 vel) {
        position = pos;
        velocity = vel;
    }

    public void update(float delta) {
        position.add(velocity.x * delta, velocity.y * delta);
        lifetime -= delta;
    }

    public void drawWhitePixel(SpriteBatch batch) {
        batch.draw(createWhitePixel(), position.x, position.y, 2, 2);
    }

    public void drawRedPixel(SpriteBatch batch) {
        batch.draw(createRedPixel(), position.x, position.y, 2, 2);
    }
    private TextureRegion createWhitePixel() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(231/255f, 255/255f, 238/255f, 1.0f)); // #e7ffee
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegion(texture);
    }

    private TextureRegion createRedPixel() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(237/255f, 160/255f, 49/255f, 1.0f)); // #eda031
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegion(texture);
    }
}
