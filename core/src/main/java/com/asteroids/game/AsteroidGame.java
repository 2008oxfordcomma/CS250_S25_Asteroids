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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

class HighScore {
    String initials;
    int score;

    HighScore(String initials, int score) {
        this.initials = initials;
        this.score = score;
    }
}

public class AsteroidGame extends ApplicationAdapter {

    enum GameState {
        TITLE, PLAYING, GAME_OVER
    }

    GameState gameState = GameState.TITLE;

    OrthographicCamera camera;
    SpriteBatch batch;
    ShapeRenderer shapeRenderer;

    Player player;
    Array<Bullet> bullets;
    Array<Asteroid> asteroids;
    ArrayList<HighScore> highScores = new ArrayList<>();
    String playerInitials = "AAA"; // default initials
    int initialIndex = 0; // Tracking character position when entering initials
    boolean enteringInitials = false;
    float asteroidSpawnTimer;
    int level = 1;
    int score = 0;

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
                    // Remove bullet
                    bullets.removeIndex(i);

                    if (asteroid.size == 3) {
                        score += 20;
                    } else if (asteroid.size == 2) {
                        score += 50;
                    } else if (asteroid.size == 1) {
                        score += 100;
                    }

                    // Handle asteroid
                    if (asteroid.size > 1) {
                        splitAsteroid(asteroid); // Split into smaller asteroids
                    }
                    asteroids.removeIndex(j); // Remove the original asteroid
                    break; // Exit loop since the bullet is destroyed
                }
            }
        }
        for (Asteroid asteroid : asteroids) { // player and asteroid collision
            if (asteroid.getBounds().overlaps(new Circle(player.position.x, player.position.y, 10))) {
                player.takeDamage();
                break;
            }
        }

        if (player.health <= 0) {
            gameOver();
        }
    }

    void splitAsteroid(Asteroid parent) {
        for (int i = 0; i < 2; i++) {
            asteroids.add(new Asteroid(parent.position.cpy(), parent.size - 1));
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

        loadHighScores();
        levelUp();
    }

    void loadHighScores() {
        highScores.clear();
        File file = new File("/core/src/main/highscores.txt");

        if (!file.exists()) { // no high scores yet
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(", ");
                if (parts.length == 2) {
                    highScores.add(new HighScore(parts[0], Integer.parseInt(parts[1])));
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        // Sort scores (highest first)
        highScores.sort((a, b) -> Integer.compare(b.score, a.score));
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

        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        if (gameState == GameState.TITLE) {
            renderTitleScreen();
        } else if (gameState == GameState.PLAYING) {
            // Normal game rendering
            handleInput();
            update(Gdx.graphics.getDeltaTime());

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            player.draw(shapeRenderer);
            for (Asteroid asteroid : asteroids) asteroid.draw(shapeRenderer);
            shapeRenderer.end();

            batch.begin();
            for (Bullet bullet : bullets) bullet.draw(batch);

            // Draw UI
            BitmapFont font = new BitmapFont();
            font.setColor(Color.WHITE);
            font.draw(batch, "Score: " + score, Gdx.graphics.getWidth() / 2, 580);
            font.draw(batch, "Health: " + player.health, 20, 580);
            font.draw(batch, "Level: " + (level - 1), 700, 580);
            batch.end();
        } else if (gameState == GameState.GAME_OVER) {
            renderGameOverScreen();
        }
    }

    void renderTitleScreen() {
        batch.begin();

        BitmapFont font = new BitmapFont();
        font.setColor(Color.WHITE);

        font.draw(batch, "ASTEROIDS", 350, 400);
        font.draw(batch, "Press ENTER to Start", 310, 350);

        batch.end();

        // Start the game when ENTER is pressed
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            startGame();
        }
    }

    void handleInput() {
        if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) player.rotate(2);
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) player.rotate(-2);
        if(Gdx.input.isKeyPressed(Input.Keys.UP)) player.thrust();
        if(Gdx.input.isKeyPressed(Input.Keys.SPACE)) player.shoot(bullets);
    }

    void levelUp() {
        level++;
        int numAsteroids = 4 + level; // More asteroids per level

        for (int i = 0; i < numAsteroids; i++) {
            Vector2 randomPos = new Vector2(MathUtils.random(800), MathUtils.random(600)); // eventually make this scale with the screen
            int startSize = MathUtils.random(2,3); // starts with medium or large asteroids to give some variation

            Asteroid newAsteroid = new Asteroid(randomPos, startSize);
            newAsteroid.velocity.scl(1 + level * 0.1f); // slightly increase the speed
            asteroids.add(newAsteroid);
        }

        player.position.set(400, 300);
        player.velocity.set(0,0);
    }

    void startGame() {
        score = 0;
        level = 1;
        player = new Player();
        bullets.clear();
        asteroids.clear();
        gameState = GameState.PLAYING;
        levelUp(); // Starts the first level
    }

    void gameOver() {
        gameState = GameState.GAME_OVER;
    }

    void renderGameOverScreen() {
        batch.begin();

        BitmapFont font = new BitmapFont();
        font.setColor(Color.WHITE);

        font.draw(batch, "GAME OVER", 350, 400);
        font.draw(batch, "Press ENTER to Restart", 310, 350);

        batch.end();

        if (Gdx.input.isKeyPressed(Input.Keys.ENTER)) {
            startGame();
        }
    }

    void update(float delta) {
        player.update(delta);
        updateBullets(delta);
        updateAsteroids(delta);
        checkCollisions();
        wrapAroundScreen();
        if (asteroids.size == 0) {
            levelUp();
        }
    }
}

