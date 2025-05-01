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
    static Texture whitePixel;
    static Texture redPixel;
    static boolean initialized = false;
    Color darkRedColor = new Color(237/255f, 160/255f, 49/255f, 1.0f);
    Color darkerRedColor = new Color (0.6f, 0.1f, 0.1f, 1f);
    Color lightColor = new Color(231/255f, 255/255f, 238/255f, 1.0f);

    public Bullet(Vector2 pos, Vector2 vel) {
        position = pos;
        velocity = vel;

        if (!initialized) {
            createPixels();
            initialized = true;
        }
    }

    private void createPixels() {
        Pixmap pixmapWhite = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmapWhite.setColor(lightColor);
        pixmapWhite.fill();
        whitePixel = new Texture(pixmapWhite);
        pixmapWhite.dispose();

        Pixmap pixmapRed = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmapRed.setColor(darkRedColor);
        pixmapRed.fill();
        redPixel = new Texture(pixmapRed);
        pixmapRed.dispose();
    }

    public void update(float delta) {
        position.add(velocity.x * delta, velocity.y * delta);
        lifetime -= delta;
    }

    public void drawWhitePixel(SpriteBatch batch, boolean darkMode) {
        batch.setColor(darkMode ? lightColor : darkRedColor);
        batch.draw(whitePixel, position.x, position.y, 2, 2);
        batch.setColor(lightColor);
    }

    public void drawRedPixel(SpriteBatch batch, boolean darkMode) {
        batch.setColor(darkMode ? darkRedColor : darkerRedColor); // darker red for light mode
        batch.draw(redPixel, position.x, position.y, 2, 2);
        batch.setColor(Color.WHITE); // Reset color
    }

}
