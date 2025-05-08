/**
 * Author: Dre Harm
 * Date: 2/4/25
 */

package com.asteroids.game;

import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.*;

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
        TITLE, PLAYING, GAME_OVER, LEADERBOARD, SETTINGS
    }

    GameState gameState = GameState.TITLE;
    GameState previousGameState;

    OrthographicCamera camera;
    OrthographicCamera screenCamera;

    SpriteBatch batch;
    ShapeRenderer shapeRenderer;

    Viewport gameViewport;
    Viewport barViewport;

    Player player;
    Array<Bullet> bullets;
    Array<Asteroid> asteroids;

    EnemyShip enemyShip;
    float ufoSpawnTimer = 0;

    BitmapFont font;
    GlyphLayout layout = new GlyphLayout();

    boolean darkMode = true;
    Color currentBackground = new Color(39/255f, 41/255f, 70/255f, 1f);
    Color targetBackground = new Color(39/255f, 41/255f, 70/255f, 1f);
    Color darkColor = new Color(39/255f, 41/255f, 70/255f, 1f);
    Color lightColor = new Color(254/255f, 245/255f, 235/255f, 1f);
    Color fontColorDark = new Color(231 / 255f, 255 / 255f, 238 / 255f, 1.0f);
    Color fontColorLight = new Color(0.1f, 0.1f, 0.1f, 1.0f);
    Color barColorLightMode = new Color(212/255f, 204/255f, 195/255f, 1f);
    Color barColorDarkMode = new Color(39/255f, 34/255f, 59/255f, 1f);
    Preferences prefs;

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

    int nextExtraLifeScore = 10000;

    Stage settingsStage;
    Skin uiSkin;

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

                //Check for collision between bullet and asteroid
                if (asteroid.getBounds().contains(bullet.position)) {

                    bullets.removeIndex(i);

                    if (asteroid.size == 3) {
                        score += 20;
                    } else if (asteroid.size == 2) {
                        score += 50;
                    } else if (asteroid.size == 1) {
                        score += 100;
                    }

                    checkExtraLife();

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

    void checkExtraLife() {
        if (score >= nextExtraLifeScore) {
            player.health++;
            nextExtraLifeScore += 10000;
        }
    }

    @Override
    public void create() {
        screenCamera = new OrthographicCamera();
        screenCamera.setToOrtho(false, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());

        barViewport = new ScreenViewport(screenCamera);

        camera = new OrthographicCamera();
        gameViewport = new FitViewport(800, 600, camera);
        gameViewport.apply();

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        player = new Player();
        bullets = new Array<>();
        asteroids = new Array<>();

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("asteroids-display.ttf")); // from assets folder
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 20;
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;

        font = generator.generateFont(parameter);
        generator.dispose();
        font.setColor(darkMode ? fontColorLight : fontColorDark);

        loadHighScores(); // load high scores from the file
        levelUp();

        prefs = Gdx.app.getPreferences("AsteoidSettings");

        darkMode = prefs.getBoolean("darkMode", true);
        currentBackground.set(darkMode ? darkColor : lightColor);
        targetBackground.set(darkMode ? darkColor : lightColor);
    }

    void gameOver() {
        gameState = GameState.GAME_OVER;
        enteringInitials = true;
        playerInitials = "AAA";
        initialsIndex = 0;
        loadHighScores();
    }

    void initSettingsUI() {
        settingsStage = new Stage(gameViewport); // Use the same viewport as the main game to keep sizing consistent

        TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("uiskin.atlas"));

        uiSkin = new Skin(atlas);
        uiSkin.load(Gdx.files.internal("uiskin.json"));

        // Create a table for layout
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        settingsStage.addActor(rootTable);

        TextButton modeButton = new TextButton("Toggle Light/Dark Mode", uiSkin);
        TextButton fullscreenButton = new TextButton("Toggle Fullscreen", uiSkin);
        TextButton backButton = new TextButton("Resume", uiSkin); // Changed label
        TextButton exitToTitleButton = new TextButton("Exit to Title", uiSkin);
        TextButton quitButton = new TextButton("Quit Game", uiSkin);

        Label pauseLabel = new Label("PAUSED", uiSkin);

        fullscreenButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (isFullscreen) { // switch to windowed mode
                    Gdx.graphics.setWindowedMode(windowedWidth, windowedHeight);
                    gameViewport.update(windowedWidth, windowedHeight, true);
                    barViewport.update(windowedWidth, windowedHeight, true);
                    isFullscreen = false;
                } else { // switch to fullscreen mode
                    Graphics.DisplayMode displayMode = Gdx.graphics.getDisplayMode();
                    Gdx.graphics.setFullscreenMode(displayMode);
                    gameViewport.update(displayMode.width, displayMode.height, true);
                    barViewport.update(displayMode.width, displayMode.height, true);
                    isFullscreen = true;
                }
            }
        });

        modeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                toggleDarkMode();
            }
        });

        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                gameState = previousGameState != null ? previousGameState : GameState.TITLE;
                Gdx.input.setInputProcessor(null);
            }
        });

        exitToTitleButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                gameState = GameState.TITLE;
                Gdx.input.setInputProcessor(null);
            }
        });

        quitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });
        rootTable.add(pauseLabel).pad(20f).row();
        rootTable.add(backButton).pad(10f).row();
        rootTable.add(modeButton).pad(10f).row();
        rootTable.add(fullscreenButton).pad(10f).row();
        rootTable.add(exitToTitleButton).pad(10f).row();
        rootTable.add(quitButton).pad(10f).row();
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


        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            previousGameState = gameState;
            gameState = GameState.SETTINGS;
            initSettingsUI();               // set up the stage if not done yet
            Gdx.input.setInputProcessor(settingsStage);
        }
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
        currentBackground.lerp(targetBackground, Gdx.graphics.getDeltaTime() * 5f);
        Gdx.gl.glClearColor(currentBackground.r, currentBackground.g, currentBackground.b, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);



        try {
            // Handle fullscreen toggle
            if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
                if (isFullscreen) {
                    Gdx.graphics.setWindowedMode(windowedWidth, windowedHeight);
                    isFullscreen = false;
                } else {
                    Graphics.DisplayMode displayMode = Gdx.graphics.getDisplayMode();
                    Gdx.graphics.setFullscreenMode(displayMode);
                    isFullscreen = true;
                }
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                if (gameState != GameState.SETTINGS) {
                    previousGameState = gameState;
                    gameState = GameState.SETTINGS;
                    initSettingsUI();
                    Gdx.input.setInputProcessor(settingsStage);
                } else {
                    // ESCAPE exits the pause/settings menu
                    gameState = previousGameState != null ? previousGameState : GameState.TITLE;
                    Gdx.input.setInputProcessor(null);
                }
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
                toggleDarkMode();
            }

            barViewport.apply();
            screenCamera.update();
            shapeRenderer.setProjectionMatrix(screenCamera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.identity();
            shapeRenderer.setColor(darkMode ? barColorDarkMode : barColorLightMode);
            int screenWidth = Gdx.graphics.getWidth();
            int screenHeight = Gdx.graphics.getHeight();
            int gameX = gameViewport.getScreenX();
            int gameY = gameViewport.getScreenY();
            int gameWidth = gameViewport.getScreenWidth();
            int gameHeight = gameViewport.getScreenHeight();

            // Left bar
            shapeRenderer.rect(0, 0, gameX, screenHeight);
            // Right bar
            shapeRenderer.rect(gameX + gameWidth, 0, screenWidth - (gameX + gameWidth), screenHeight);
            // Bottom bar
            shapeRenderer.rect(gameX, 0, gameWidth, gameY);
            // Top bar
            shapeRenderer.rect(gameX, gameY + gameHeight, gameWidth, screenHeight - (gameY + gameHeight));

            shapeRenderer.end();

            gameViewport.apply();
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
                    player.draw(shapeRenderer, darkMode);
                    if (enemyShip != null) enemyShip.draw(shapeRenderer);
                    for (Asteroid asteroid : asteroids) asteroid.draw(shapeRenderer, darkMode);
                    // for (Explosion explosion : explosions) explosion.draw(shapeRenderer);
                    shapeRenderer.end();

                    font.setColor(darkMode ? fontColorDark : fontColorLight);

                    batch.begin();
                    for (Bullet bullet : bullets) bullet.drawWhitePixel(batch, darkMode);
                    if (enemyShip != null) {
                        for (Bullet b : enemyShip.getBullets()) b.drawRedPixel(batch, darkMode);
                    }

                    float x = (gameViewport.getWorldWidth() - layout.width) / 2f;
                    float y = gameViewport.getWorldHeight() - 20;

                    font.draw(batch, layout, x, y);

                    String scoreText = "Score: " + score;
                    layout.setText(font, scoreText);
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

                case SETTINGS:
                    settingsStage.act(Gdx.graphics.getDeltaTime());
                    settingsStage.draw();
                    break;
            }

        } catch (Exception e) {
            System.err.println("[ERROR] Game crashed during render(): " + e.getMessage());
            e.printStackTrace();
        }
    }


    void renderGameOverScreen() {
        batch.begin();

        font.setColor(darkMode ? fontColorDark : fontColorLight);

        if (enteringInitials) {
            layout.setText(font, "ENTER YOUR INITIALS");
            font.draw(batch, layout, (gameViewport.getWorldWidth() - layout.width) / 2f, 400);

            layout.setText(font, playerInitials);
            font.draw(batch, layout, (gameViewport.getWorldWidth() - layout.width) / 2f, 350);

            layout.setText(font, "[LEFT/RIGHT] Move, [UP/DOWN] Change Letter, [ENTER] Confirm");
            font.draw(batch, layout, (gameViewport.getWorldWidth() - layout.width) / 2f, 300);

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

        font.setColor(darkMode ? fontColorDark : fontColorLight);

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

        font.setColor(darkMode ? fontColorDark : fontColorLight);
        layout.setText(font, "ASTEROIDS");
        font.draw(batch, layout, (gameViewport.getWorldWidth() - layout.width) / 2f, 400);

        layout.setText(font, "Press [ENTER] to Start");
        font.draw(batch, layout, (gameViewport.getWorldWidth() - layout.width) / 2f, 350);

        batch.end();

        // Start the game when ENTER is pressed
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            startGame();
        }
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height, true);
        barViewport.update(width, height, true);
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
            enemyShip.update(delta, player.position);

            // Check if UFO collides directly with player
            if (enemyShip.collidesWithPlayer(player.position)) {
                player.takeDamage();
                enemyShip = null;  // stop referencing it
            }

            // If still alive, handle UFO bullet collisions
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

                // If it goes off-screen, remove it
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

    void toggleDarkMode() {
        darkMode = !darkMode;
        targetBackground.set(darkMode ? darkColor : lightColor);
        font.setColor(darkMode ? fontColorDark : fontColorLight);

        layout.setText(font, layout.toString());

        prefs.putBoolean("darkMode", darkMode);
        prefs.flush();
    }
}

