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
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.io.*;
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
        TITLE, PLAYING, GAME_OVER, LEADERBOARD
    }

    GameState gameState = GameState.TITLE;

    OrthographicCamera camera;
    Viewport viewport;
    SpriteBatch batch;
    ShapeRenderer shapeRenderer;

    Player player;
    Array<Bullet> bullets;
    Array<Asteroid> asteroids;

    EnemyShip enemyShip;
    float ufoSpawnTimer = 0;

    BitmapFont font;

    ArrayList<HighScore> highScores = new ArrayList<>();
    String playerInitials = "AAA"; // default initials
    int initialsIndex = 0; // Tracking character position when entering initials
    boolean enteringInitials = false;

    float asteroidSpawnTimer;
    int level = 1;
    int score = 0;

    // 4:3 aspect ratio
    boolean isFullscreen = false;
    int windowedWidth = 800;
    int windowedHeight = 600;

    void addHighScore() {
        highScores.add(new HighScore(playerInitials, score));

        // Sort and keep only top 5
        highScores.sort((a, b) -> Integer.compare(b.score, a.score));
        while (highScores.size() > 5) {
            highScores.remove(highScores.size() - 1);
        }

        saveHighScores(); // Save to file
        loadHighScores(); // Reload from file

        enteringInitials = false;
        gameState = GameState.LEADERBOARD; // Transition to leaderboard
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

    @Override
    public void create() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(800, 600, camera);
        viewport.apply();

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        player = new Player();
        bullets = new Array<>();
        asteroids = new Array<>();

        font = new BitmapFont();
        font.setColor(Color.WHITE);

        loadHighScores(); // load high scores from the file
        levelUp();
    }

    void gameOver() {
        gameState = GameState.GAME_OVER;
        enteringInitials = true;
        playerInitials = "AAA";
        initialsIndex = 0;

        loadHighScores();
    }

    void handleInitialsInput() {
        char[] initials = playerInitials.toCharArray();

        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            initialsIndex = (initialsIndex + 1) % 3;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            initialsIndex = (initialsIndex + 2) % 3; // Move back
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            initials[initialsIndex] = (char) ((initials[initialsIndex] - 'A' + 1) % 26 + 'A');
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            initials[initialsIndex] = (char) ((initials[initialsIndex] - 'A' + 25) % 26 + 'A');
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            addHighScore();
        }

        playerInitials = new String(initials);
    }

    void handleInput() {
        if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) player.rotate(1.5f);
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) player.rotate(-1.5f);
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

        player.isInvincible = true;
        player.invincibilityTimer = 2.0f;
        player.invincibilityBlinkTimer = 0f;
    }

    void loadHighScores() {
        highScores.clear();
        File file = new File("highscores.txt");

        if (!file.exists()) return; // No high scores yet

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
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

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.2196f, 0.1686f, 0.1490f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        try {
            camera.update();
            batch.setProjectionMatrix(camera.combined);
            shapeRenderer.setProjectionMatrix(camera.combined);

            switch (gameState) {
                case TITLE:
                    renderTitleScreen();
                    break;
                case PLAYING:
                    handleInput();
                    update(Gdx.graphics.getDeltaTime());

                    shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                    player.draw(shapeRenderer);
                    if (enemyShip != null) enemyShip.draw(shapeRenderer);
                    for (Asteroid asteroid : asteroids) asteroid.draw(shapeRenderer);
                    shapeRenderer.end();

                    batch.begin();
                    for (Bullet bullet : bullets) bullet.draw(batch);
                    if (enemyShip != null) {
                        for (Bullet b : enemyShip.getBullets()) b.draw(batch);
                    }
                    font.draw(batch, "Score: " + score, 400, 580);
                    font.draw(batch, "Health: " + player.health, 20, 580);
                    font.draw(batch, "Level: " + (level - 1), 700, 580);
                    batch.end();
                    break;

                case GAME_OVER:
                    renderGameOverScreen();
                    break;

                case LEADERBOARD:
                    renderLeaderboardScreen();
                    break;
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Game crashed during render(): " + e.getMessage());
            e.printStackTrace();
        }
    }

    void renderGameOverScreen() {
        batch.begin();

        BitmapFont font = new BitmapFont();
        font.setColor(Color.WHITE);

        if (enteringInitials) {
            font.draw(batch, "ENTER YOUR INITIALS", 310, 400);
            font.draw(batch, playerInitials, 370, 350);
            font.draw(batch, "[LEFT/RIGHT] Move, [UP/DOWN] Change Letter, [ENTER] Confirm", 150, 300);

            handleInitialsInput();
        } else {
            font.draw(batch, "GAME OVER", 350, 400);
            font.draw(batch, "Press ENTER to View Leaderboard", 280, 350);
        }

        batch.end();

        if (!enteringInitials && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            gameState = GameState.LEADERBOARD; // Show leaderboard instead of restarting
        }
    }

    void renderLeaderboardScreen() {
        batch.begin();

        BitmapFont font = new BitmapFont();
        font.setColor(Color.WHITE);

        font.draw(batch, "LEADERBOARD", 330, 400);

        for (int i = 0; i < Math.min(5, highScores.size()); i++) {
            HighScore hs = highScores.get(i);
            font.draw(batch, (i + 1) + ". " + hs.initials + " " + hs.score, 330, 370 - i * 20);
        }

        font.draw(batch, "Press ENTER to Start a New Game", 250, 200);

        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            startGame(); // Restart the game when Enter is pressed
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

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    void saveHighScores() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("highscores.txt"))) {
            for (HighScore hs : highScores) {
                bw.write(hs.initials + "," + hs.score);
                bw.newLine();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    void splitAsteroid(Asteroid parent) {
        for (int i = 0; i < 2; i++) {
            asteroids.add(new Asteroid(parent.position.cpy(), parent.size - 1));
        }
    }

    void startGame() {
        gameState = GameState.PLAYING;
        level = 1;
        score = 0; // Reset score
        player = new Player();
        bullets.clear();
        asteroids.clear();
        levelUp(); // Start the first level
    }

    void update(float delta) {
        player.update(delta);
        updateBullets(delta);
        updateAsteroids(delta);
        checkCollisions();
        wrapAroundScreen();

        ufoSpawnTimer += delta;
        if (ufoSpawnTimer > 15 && enemyShip == null) {
            enemyShip = new EnemyShip();
            ufoSpawnTimer = 0;
        }

        if (enemyShip != null) {
            // Update the UFO
            enemyShip.update(delta, player.position);

            // 7a) Check if UFO collides directly with player
            if (enemyShip.collidesWithPlayer(player.position)) {
                player.takeDamage();
                enemyShip = null;  // stop referencing it
            }

            // 7b) If still alive, handle UFO bullet collisions
            if (enemyShip != null) {
                Array<Bullet> enemyBullets = enemyShip.getBullets();

                for (int i = enemyBullets.size - 1; i >= 0; i--) {
                    Bullet b = enemyBullets.get(i);

                    // Bullet hits player?
                    if (player.position.dst(b.position) < 10) {
                        player.takeDamage();
                        enemyBullets.removeIndex(i);
                        continue;
                    }

                    // Bullet hits asteroid?
                    for (int j = asteroids.size - 1; j >= 0; j--) {
                        Asteroid asteroid = asteroids.get(j);
                        if (asteroid.getBounds().contains(b.position)) {
                            if (asteroid.size > 1) {
                                splitAsteroid(asteroid);
                            }
                            asteroids.removeIndex(j);
                            enemyBullets.removeIndex(i);
                            break;
                        }
                    }
                }

                // 7c) If it goes off-screen, remove it
                if (enemyShip.isOffScreen()) {
                    enemyShip = null;
                }
            }
        }

        if (asteroids.size == 0) {
            levelUp();
        }
    }

    void updateAsteroids(float delta) {
        for(Asteroid asteroid : asteroids) {
            asteroid.update(delta);
        }
    }

    void updateBullets(float delta) {
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            bullet.update(delta);
            if (bullet.lifetime <= 0) {
                bullets.removeIndex(i); // Remove bullet if lifetime is over
            }
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
}

