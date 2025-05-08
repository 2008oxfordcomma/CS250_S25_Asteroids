package com.asteroids.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Bullet {
    Vector2 position;
    Vector2 velocity;
    float lifetime = 1.5f;

    static Texture whitePixel;
    static Texture redPixel;
    static boolean initialized = false;

    static final Color colorDark = new Color(231 / 255f, 255 / 255f, 238 / 255f, 1.0f); // light color for dark mode
    static final Color colorLight = new Color(0.1f, 0.1f, 0.1f, 1.0f);                  // dark color for light mode
    static final Color darkRedColor = new Color(237 / 255f, 160 / 255f, 49 / 255f, 1.0f);
    static final Color darkerRedColor = new Color(0.6f, 0.1f, 0.1f, 1f);

    public Bullet(Vector2 pos, Vector2 vel) {
        position = pos;
        velocity = vel;

        if (!initialized) {
            createRedPixel();
            recreatePixels(true); // default to dark mode
            initialized = true;
        }
    }

    private void createRedPixel() {
        Pixmap pixmapRed = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmapRed.setColor(darkRedColor);
        pixmapRed.fill();
        redPixel = new Texture(pixmapRed);
        pixmapRed.dispose();
    }

    public static void recreatePixels(boolean darkMode) {
        if (whitePixel != null) whitePixel.dispose();

        Pixmap pixmapWhite = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmapWhite.setColor(darkMode ? colorDark : colorLight);
        pixmapWhite.fill();
        whitePixel = new Texture(pixmapWhite);
        pixmapWhite.dispose();
    }

    public void update(float delta) {
        position.add(velocity.x * delta, velocity.y * delta);
        lifetime -= delta;
    }

    public void drawWhitePixel(SpriteBatch batch, boolean darkMode) {
        batch.setColor(Color.WHITE);
        batch.draw(whitePixel, position.x, position.y, 2, 2);
    }

    public void drawRedPixel(SpriteBatch batch, boolean darkMode) {
        batch.setColor(darkMode ? darkRedColor : darkerRedColor);
        batch.draw(redPixel, position.x, position.y, 2, 2);
        batch.setColor(Color.WHITE);
    }
}
