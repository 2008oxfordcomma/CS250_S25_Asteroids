/**
 * Author: Dre Harm
 * Date: 2/4/25
 */

package com.asteroids.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;

public class AsteroidGame extends ApplicationAdapter {
    OrthographicCamera camera;
    SpriteBatch batch;
    ShapeRenderer shapeRenderer;

    Player player;
    Array<Bullet> bullets;
    Array<Asteroid> asteroids;
    float asteroidSpawnTimer;

    void updateBullets(float delta) {
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            bullet.update(delta);
            if (bullet.lifetime <= 0) {
                bullets.removeIndex(i); // Remove bullet if lifetime is over
            }
        }
    }

    void updateAsteroids(float delta) {
        for(Asteroid asteroid : asteroids) {
            asteroid.update(delta);
        }
    }

    void checkCollisions() {
        // Loop through bullets and asteroids in reverse order to safely remove items
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            for (int j = asteroids.size - 1; j >= 0; j--) {
                Asteroid asteroid = asteroids.get(j);

                // Check for collision between bullet and asteroid
                if (asteroid.getBounds().contains(bullet.position)) {
                    // Remove the bullet
                    bullets.removeIndex(i);

                    // Handle asteroid (e.g., split or remove)
                    if (asteroid.size > 1) {
                        splitAsteroid(asteroid); // Split into smaller asteroids
                    }
                    asteroids.removeIndex(j); // Remove the original asteroid
                    break; // Exit the inner loop since the bullet is destroyed
                }
            }
        }
    }

    void splitAsteroid(Asteroid parent) {
        for(int i = 0; i < 2; i++) {
            asteroids.add(new Asteroid(
                parent.position.cpy(),
                parent.size - 1
            ));
        }
    }

    void wrapAroundScreen() {
        // Wrap player around the screen
        Vector2 pos = player.position;
        if (pos.x < 0) pos.x = 800;
        if (pos.x > 800) pos.x = 0;
        if (pos.y < 0) pos.y = 600;
        if (pos.y > 600) pos.y = 0;

        // Wrap asteroids around the screen
        for (Asteroid asteroid : asteroids) {
            Vector2 apos = asteroid.position;
            if (apos.x < 0) apos.x = 800;
            if (apos.x > 800) apos.x = 0;
            if (apos.y < 0) apos.y = 600;
            if (apos.y > 600) apos.y = 0;
        }

        // Wrap bullets around the screen
        for (Bullet bullet : bullets) {
            Vector2 bpos = bullet.position;
            if (bpos.x < 0) bpos.x = 800;
            if (bpos.x > 800) bpos.x = 0;
            if (bpos.y < 0) bpos.y = 600;
            if (bpos.y > 600) bpos.y = 0;
        }
    }

    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        player = new Player();
        bullets = new Array<>();
        asteroids = new Array<>();

        spawnInitialAsteroids();
    }

    void spawnInitialAsteroids() {
        for(int i = 0; i < 4; i++) {
            asteroids.add(new Asteroid(new Vector2(MathUtils.random(800), MathUtils.random(600)), 3));
        }
    }

    @Override
    public void render() {
        // Set the background color (#382b26)
        Gdx.gl.glClearColor(0.2196f, 0.1686f, 0.1490f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Clear the screen with the new color

        handleInput();
        update(Gdx.graphics.getDeltaTime());

        // Draw everything
        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        player.draw(shapeRenderer);
        for (Asteroid asteroid : asteroids) asteroid.draw(shapeRenderer);
        shapeRenderer.end();

        batch.begin();
        for (Bullet bullet : bullets) bullet.draw(batch);
        batch.end();
    }

    void handleInput() {
        if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) player.rotate(2);
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) player.rotate(-2);
        if(Gdx.input.isKeyPressed(Input.Keys.UP)) player.thrust();
        if(Gdx.input.isKeyPressed(Input.Keys.SPACE)) player.shoot(bullets);
    }

    void update(float delta) {
        player.update(delta);
        updateBullets(delta);
        updateAsteroids(delta);
        checkCollisions();
        wrapAroundScreen();
    }
}

