import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JComponent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

//Jarod Esareesingh 
//816026811
public class GamePanel extends JComponent {

    private List<Missile> missiles;
    private List<Alien> aliens;
    private List<ExplosionEffect> explosionEffects;
    private Graphics2D graphics;
    private BufferedImage buffImage;
    private Image backgroundImage;
    private int w;
    private int h;
    private Thread gameThread;
    private boolean startGameBool = true;
    private boolean isPaused = false;
    private KeyManager key;
    private int missileMS;
    private final int frames = 60;
    private final int target = 1000000000 / frames;
    private SoundManger gameSounds;
    private Ship ship;
    private int remainingTime = 30;
    private boolean levelComplete = false;
    private boolean playerDead= false;
    private float opacity = 0.9f;
    private float opacityChange = 0.01f;
    private int background = 1;
    private boolean isTransparent;
    private boolean isChanging = true;
    private boolean firstLevelBeaten = false;
    private int score = 0;
    private int imageNum;
    private int level = 1;
    private Timer countdownTimer;

    public void pauseGame() {
        if (startGameBool) {
            if (isPaused)
                isPaused = false;
            else
                isPaused = true;
        }
    }

    public void start() {
        // Get width and height of the window
        w = getWidth();
        h = getHeight();

        // Load background image and create image buffer
        backgroundImage = ImageManager.loadImage("images/Space Background1.png");
        buffImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        graphics = buffImage.createGraphics();

        // Start game loop thread
        gameThread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (startGameBool) {
                    // System.out.print(isPaused);
                    if (!isPaused) {

                        long startTime = System.nanoTime();

                        // Draw game elements
                        drawBackground();
                        drawGame();
                        // Process the Enter key
                        handleEnterKey();

                        // Limit frame rate
                        long time = System.nanoTime() - startTime;
                        if (time < target) {
                            long sleepTime = (target - time) / 1000000;
                            sleep(sleepTime);
                        }
                        if (score % 10 == 9) {
                            isChanging = true;
                        }

                        if (score % 10 == 0 && isChanging && score != 0) {
                            background++;
                            if (background > 3) {
                                background = 1;
                            }
                            makeTransparnet(background);
                            isChanging = false;

                        }
                    }
                    // Render to screen
                    renderGame();
                }
            }
        });

        // Initialize game objects
        initializeGameObjects();

        // Initialize keyboard input
        initializeKeys();

        // Initialize missiles
        initializeMissiles();

        // Start background music
        // gameSounds.playBackground();

        // Start game loop thread
        countdownTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (remainingTime > 0 && !levelComplete) {
                    if (!isPaused)
                        remainingTime--;
                } else {
                    ((Timer) e.getSource()).stop();
                    if (checkWinCondition()) {
                        levelComplete = true;
                        // remainingTime = 30;
                        // startGameBool = false;
                        aliens.clear();
                    } else if (!levelComplete) {
                        // Check if the score is less than 20
                        if (score < 10) {
                            // Trigger ship explosion and game over screen
                            ship.setAlive(false);
                            gameSounds.stopBackground();
                            gameSounds.soundPlayerDeath();
                            level = 1;
                            double x = ship.getX() + Ship.shipSize / 2;
                            double y = ship.getY() + Ship.shipSize / 2;
                            explosionEffects.add(new ExplosionEffect(x, y, 5, 5, 75, 0.05f, new Color(255, 165, 0))); // orange
                            explosionEffects.add(new ExplosionEffect(x, y, 5, 5, 75, 0.1f, new Color(255, 255, 0))); // yellow
                            explosionEffects.add(new ExplosionEffect(x, y, 10, 10, 100, 0.3f, new Color(255, 69, 0))); // deep
                                                                                                                       // orange
                            explosionEffects.add(new ExplosionEffect(x, y, 10, 5, 100, 0.5f, new Color(255, 0, 0))); // red
                            explosionEffects.add(new ExplosionEffect(x, y, 10, 5, 150, 0.2f, new Color(255, 255, 255))); // white

                        }
                    }

                }
                checkEndGameCondition();
            }
        });

        countdownTimer.start();
        gameThread.start();
        // handleEnterKey();
    }

    private void addMissile() {
        // Create 3 aliens with random Y positions
        Random ran = new Random();
        int locY = ran.nextInt(h - 50) + 25;
        int alienType;
        alienType = level;
        if(level > 6){
            alienType = (alienType % 6) + 1 ;
        }
        Alien alien = new Alien(alienType);
        alien.changeShipLoc(0, locY);
        alien.modifyShipAngle(0);
        aliens.add(alien);

        int locY2 = ran.nextInt(h - 50) + 25;
        Alien alien1 = new Alien(alienType);
        alien1.changeShipLoc(w, locY2);
        alien1.modifyShipAngle(180);
        aliens.add(alien1);

        int locY3 = ran.nextInt(h - 50) + 25;
        Alien alien3 = new Alien(alienType);
        alien3.changeShipLoc(w, locY3);
        alien3.modifyShipAngle(180);
        aliens.add(alien3);
        // watch out

        if (level >= 2) {
            int locY4 = ran.nextInt(w - 50) + 25;
            Alien alien4 = new Alien(alienType);
            alien4.changeShipLoc(locY4, 0);
            alien4.modifyShipAngle(90f);
            aliens.add(alien4);
            int locY5 = ran.nextInt(w - 50) + 25;
            Alien alien5 = new Alien(alienType);
            alien5.changeShipLoc(locY5, h);
            alien5.modifyShipAngle(270f);
            aliens.add(alien5);
        }
        if (level >= 3) {
            Alien topLeftAlien = new Alien(alienType);
            topLeftAlien.changeShipLoc(0, 0);
            topLeftAlien.modifyShipAngle(45); // Angle to move diagonally
            aliens.add(topLeftAlien);

            Alien topRightAlien = new Alien(alienType);
            topRightAlien.changeShipLoc(w, 0);
            topRightAlien.modifyShipAngle(135); // Angle to move diagonally
            aliens.add(topRightAlien);

            Alien bottomLeftAlien = new Alien(alienType);
            bottomLeftAlien.changeShipLoc(0, h);
            bottomLeftAlien.modifyShipAngle(315); // Angle to move diagonally
            aliens.add(bottomLeftAlien);

            Alien bottomRightAlien = new Alien(alienType);
            bottomRightAlien.changeShipLoc(w, h);
            bottomRightAlien.modifyShipAngle(225); // Angle to move diagonally
            aliens.add(bottomRightAlien);
        }
    }

    private void startNextLevel() {
        levelComplete = false;
        score = 0;
        aliens.clear();
        missiles.clear();
        remainingTime = 30;
        level++;

        countdownTimer.restart();
    }

    private void drawLevel() {
        String levelStr = "Level: " + level;
        graphics.setColor(Color.WHITE);
        graphics.setFont(getFont().deriveFont(Font.BOLD, 20f));
        FontMetrics metrics = graphics.getFontMetrics();
        int x = 10;
        int y = 40;
        graphics.drawString(levelStr, x, y);
    }

    private boolean checkWinCondition() {
        return score >= 10 && remainingTime > 0;
    }

    private void checkEndGameCondition() {
        if (score >= 10 && remainingTime > 0) {
            levelComplete = true;
            if (!key.enterCheck()) {
                drawNextLevelMessage();
            } else {
                startNextLevel();
               // levelComplete = false; // this line resets the levelComplete flag
            }
        }
    }

    private void drawTimer(Graphics2D graphics) {
        graphics.setColor(Color.WHITE);
        graphics.setFont(getFont().deriveFont(Font.BOLD, 18f));
        graphics.drawString("Time Remaining: " + remainingTime, w - 180, 20);
    }

    private void addExplosionEffects(double x, double y) {
        explosionEffects.add(new ExplosionEffect(x, y, 5, 5, 75, 0.05f, new Color(255, 165, 0))); // orange
        explosionEffects.add(new ExplosionEffect(x, y, 5, 5, 75, 0.1f, new Color(255, 255, 0))); // yellow
        explosionEffects.add(new ExplosionEffect(x, y, 10, 10, 100, 0.3f, new Color(255, 69, 0))); // deep orange
        explosionEffects.add(new ExplosionEffect(x, y, 10, 5, 100, 0.5f, new Color(255, 0, 0))); // red
        explosionEffects.add(new ExplosionEffect(x, y, 10, 5, 150, 0.2f, new Color(255, 255, 255))); // white
    }

    private void drawNextLevelMessage() {
        if (level == 3) {
            String message1 = "You defeated all the invading aliens,";
            String message2 = "press enter for endless mode!";
            graphics.setColor(Color.WHITE);
            graphics.setFont(getFont().deriveFont(Font.BOLD, 35f));
            FontMetrics metrics = graphics.getFontMetrics();

            int x1 = (w - metrics.stringWidth(message1)) / 2;
            int y1 = (h / 2) - 15;
            int x2 = (w - metrics.stringWidth(message2)) / 2;
            int y2 = (h / 2) + 15;

            // Add explosion effects
            for (int i = 0; i < 1; i++) {
                int randomX = new Random().nextInt(w);
                int randomY = new Random().nextInt(h);
                addExplosionEffects(randomX, randomY);
            }

            graphics.drawString(message1, x1, y1);
            graphics.drawString(message2, x2, y2);
            reset();
        } else {
            String message = "You beat this level, press enter to continue to next level!";
            graphics.setColor(Color.GREEN);
            graphics.setFont(getFont().deriveFont(Font.BOLD, 25f));
            FontMetrics metrics = graphics.getFontMetrics();
            int x = (w - metrics.stringWidth(message)) / 2;
            int y = h / 2;
            graphics.drawString(message, x, y);
            reset();
            // startGameBool = false;
        }

    }

    private void initializeGameObjects() {
        // Initialize game objects
        gameSounds = new SoundManger();
        ship = new Ship();
        ship.changeShipLoc(150, 150);
        aliens = new ArrayList<>();
        explosionEffects = new ArrayList<>();

        // Start thread to spawn aliens periodically
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (startGameBool) {
                    addMissile();
                    sleep(2000);
                }
            }
        }).start();
    }

    public void reset() {
        score = 0;
        remainingTime = 30;

        aliens.clear();
        missiles.clear();
        ship.changeShipLoc(150, 150);
        ship.resetGame();
        countdownTimer.restart();
        if(playerDead){
            level = 1;
            playerDead = false;
        }
        
    }

    public void initializeKeys() {
        key = new KeyManager();
        requestFocus();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                handleKeyRelease(e.getKeyCode());
            }
        });

        new Thread(() -> {
            float ang = 0.5f;
            while (startGameBool && !isPaused) {
                if (ship.isIntact()) {
                    float angle = ship.getAngle();
                    handleLeftRightKeys(ang, angle);
                    shootGunAndMissiles();
                    handleSpaceKey();
                    ship.updateShipLoc();
                } else {
                    handleEnterKey();
                }

                updateMissiles();
                sleep(5);
            }
        }).start();
    }

    private void handleKeyPress(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_A:
                key.setLeft(true);
                break;
            case KeyEvent.VK_D:
                key.setRight(true);
                break;
            case KeyEvent.VK_SPACE:
                key.setSpace(true);
                break;
            case KeyEvent.VK_J:
                key.setJ(true);
                break;
            case KeyEvent.VK_K:
                key.setK(true);
                break;
            case KeyEvent.VK_ENTER:
                key.setEnter(true);
                break;
            case KeyEvent.VK_BACK_SPACE:
                key.setBackspace(true);
                break;
            default:
                break;
        }
    }

    private void handleKeyRelease(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_A:
                key.setLeft(false);
                break;
            case KeyEvent.VK_D:
                key.setRight(false);
                break;
            case KeyEvent.VK_SPACE:
                key.setSpace(false);
                break;
            case KeyEvent.VK_J:
                key.setJ(false);
                break;
            case KeyEvent.VK_K:
                key.setK(false);
                break;
            case KeyEvent.VK_ENTER:
                key.setEnter(false);
                break;
            case KeyEvent.VK_BACK_SPACE:
                key.setBackspace(false);
                break;
            default:
                break;
        }
    }

    private void handleLeftRightKeys(float ang, float angle) {
        if (key.leftCheck()) {
            angle -= ang;
        }
        if (key.rightCheck()) {
            angle += ang;
        }
        ship.modifyShipAngle(angle);
    }

    private void shootGunAndMissiles() {
        if (key.jCheck() || key.kCheck()) {
            handleBulletShot();
        } else {
            missileMS = 0;
        }
    }

    private void handleBulletShot() {
        if (missileMS == 0) {
            int bulletSpeed = key.jCheck() ? 5 : 20;
            missiles.add(0, new Missile(ship.getX(), ship.getY(), ship.getAngle(), bulletSpeed, 3f));
            gameSounds.soundShoot();
        }
        missileMS++;
        if (missileMS == 15) {
            missileMS = 0;
        }
    }

    private void handleSpaceKey() {
        if (key.spaceCheck()) {
            ship.boost();
        } else {
            ship.speedDown();
        }
    }

    private void handleEnterKey() {
        if (key.enterCheck()) {
            if (levelComplete) {
                // startGameBool = true;
                firstLevelBeaten = true;
                score = 0;
                remainingTime = 30;
                // levelComplete = false;
                startNextLevel();
                //levelComplete = false;
            } else {
                reset();
            }
        }
    }

    private void updateMissiles() {
        for (int i = 0; i < aliens.size(); i++) {
            Alien alien = aliens.get(i);
            if (alien != null) {
                alien.updateAlien();
                if (!alien.checkAlien(w, h)) {
                    aliens.remove(alien);
                } else {
                    if (ship.isIntact()) {
                        checkShip(alien);
                    }
                }
            }
        }
    }

    private void initializeMissiles() {
        missiles = new ArrayList<>();

        new Thread(() -> {
            while (startGameBool) {
                for (int i = 0; i < missiles.size(); i++) {
                    Missile missile = missiles.get(i);

                    if (missile != null) {
                        missile.updateMissile();
                        checkMissiles(missile);

                        if (!missile.checkMissile(w, h)) {
                            missiles.remove(missile);
                        }
                    } else {
                        missiles.remove(missile);
                    }
                }

                for (int i = 0; i < explosionEffects.size(); i++) {
                    ExplosionEffect explosionEffect1 = explosionEffects.get(i);

                    if (explosionEffect1 != null) {
                        explosionEffect1.updateExplosion();

                        if (!explosionEffect1.checkExplosion()) {
                            explosionEffects.remove(explosionEffect1);
                        }
                    } else {
                        explosionEffects.remove(explosionEffect1);
                    }
                }

                sleep(1);
            }
        }).start();
    }

    private void checkMissiles(Missile missile) {
        for (int i = 0; i < aliens.size(); i++) {
            Alien alien = aliens.get(i);
            if (alien != null) {
                Area area = new Area(missile.getMissileShape());
                area.intersect(alien.getShipShape());
                if (!area.isEmpty()) {
                    // create explosion effect at missile position
                    explosionEffects
                            .add(new ExplosionEffect(missile.returnCenterX(), missile.returnCenterY(), 5, 10, 100, 0.2f,
                                    new Color(255, 70, 70)));

                    // update alien health
                    if (!alien.updateHealth(Math.abs(missile.returnSize()))) {
                        // alien destroyed
                        score++;
                        aliens.remove(alien);
                        gameSounds.soundDestroy();

                        // create bigger explosion effect at alien position
                        double x = alien.returnX() + Alien.alienRocketSize / 2;
                        double y = alien.returnY() + Alien.alienRocketSize / 2;
                        Color dominantColor = alien.getDominantColor();
                        explosionEffects.add(new ExplosionEffect(x, y, 7, 20, 120, 0.6f, dominantColor));
                        explosionEffects.add(new ExplosionEffect(x, y, 10, 20, 200, 0.5f, new Color(255, 200, 0)));
                        explosionEffects.add(new ExplosionEffect(x, y, 5, 40, 150, 0.4f, new Color(255, 70, 70)));
                        explosionEffects.add(new ExplosionEffect(x, y, 3, 60, 100, 0.3f, new Color(255, 255, 255)));
                    } else {
                        // alien hit
                        gameSounds.soundHit();

                        // create smaller explosion effect at alien position
                        double x = alien.returnX() + Alien.alienRocketSize / 2;
                        double y = alien.returnY() + Alien.alienRocketSize / 2;
                        explosionEffects.add(new ExplosionEffect(x, y, 5, 5, 50, 0.1f, new Color(255, 200, 0)));
                    }

                    // remove missile from list
                    missiles.remove(missile);
                }
            }
        }
    }

    private void checkShip(Alien alien) {
        if (alien == null) {
            return;
        }

        Area shipArea = new Area(ship.getShipShape());
        Area alienArea = new Area(alien.getShipShape());
        shipArea.intersect(alienArea);

        if (shipArea.isEmpty()) {
            return;
        }

        double alienHealth = alien.getHealth();
        if (!alien.updateHealth(ship.getHealth())) {
            // alien destroyed
            aliens.remove(alien);
            gameSounds.soundDestroy();
            double x = alien.returnX() + Alien.alienRocketSize / 2;
            double y = alien.returnY() + Alien.alienRocketSize / 2;
            Color dominantColor = alien.getDominantColor();
            explosionEffects.add(new ExplosionEffect(x, y, 5, 5, 75, 0.05f, new Color(255, 165, 0))); // orange
            explosionEffects.add(new ExplosionEffect(x, y, 5, 5, 75, 0.1f, new Color(255, 255, 0))); // yellow
            explosionEffects.add(new ExplosionEffect(x, y, 5, 10, 75, 0.05f, dominantColor));
            explosionEffects.add(new ExplosionEffect(x, y, 10, 3, 100, 0.5f, new Color(255, 0, 0))); // red
            explosionEffects.add(new ExplosionEffect(x, y, 10, 5, 150, 0.2f, new Color(255, 255, 255))); // white
        } else {
            gameSounds.soundHit();
        }

        if (!ship.updateHealth(Math.abs(alienHealth))) {

            // ship destroyed
            ship.setAlive(false);
            gameSounds.stopBackground();
            gameSounds.soundPlayerDeath();
            double x = ship.getX() + Ship.shipSize / 2;
            double y = ship.getY() + Ship.shipSize / 2;
            explosionEffects.add(new ExplosionEffect(x, y, 5, 5, 75, 0.05f, new Color(255, 165, 0))); // orange
            explosionEffects.add(new ExplosionEffect(x, y, 5, 5, 75, 0.1f, new Color(255, 255, 0))); // yellow
            explosionEffects.add(new ExplosionEffect(x, y, 10, 10, 100, 0.3f, new Color(255, 69, 0))); // deep orange
            explosionEffects.add(new ExplosionEffect(x, y, 10, 5, 100, 0.5f, new Color(255, 0, 0))); // red
            explosionEffects.add(new ExplosionEffect(x, y, 10, 5, 150, 0.2f, new Color(255, 255, 255))); // white
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
        g2d.setComposite(alpha);
        g2d.drawImage(backgroundImage, 0, 0, null);
    }

    public void makeTransparnet(int image) {
        isTransparent = true;
        this.imageNum = image;
        opacityChange = 0.01f;
        opacity = 0.9f;
    }

    private void drawBackground() {
        Graphics2D imageContext = (Graphics2D) buffImage.getGraphics();
        if (isTransparent && opacity <= 0.9) {
            paintComponent(imageContext);
            if (opacity < 0.1f) {
                opacityChange = opacityChange * -1;
                backgroundImage = ImageManager.loadBufferedImage("images/Space Background" + imageNum + ".png");
            }
            if (opacity > 0.9f) {
                opacity = 0.9f;
                isTransparent = false;

            } else {
                opacity = opacity - opacityChange;
            }
        } else if (opacityChange > 0) {
            imageContext.drawImage(backgroundImage, 0, 0, null);
            imageContext.dispose();
        } else {
            opacityChange = 0.1f;
        }
    }

    private void sleep(long speed) {

        try {
            Thread.sleep(speed);

        } catch (InterruptedException ex) {

            System.err.println(ex);
        }
    }

    private void drawGame() {
        // Draw ship if alive
        if (ship.isIntact()) {
            ship.draw(graphics);
        }

        // Draw missiles
        for (int i = 0; i < missiles.size(); i++) {
            Missile missile = missiles.get(i);
            if (missile != null) {
                missile.draw(graphics);
            }
        }

        // Draw aliens
        for (int i = 0; i < aliens.size(); i++) {
            Alien alien = aliens.get(i);
            if (alien != null) {
                alien.draw(graphics);
            }
        }

        // Draw explosion effects
        for (int i = 0; i < explosionEffects.size(); i++) {
            ExplosionEffect explosionEffect1 = explosionEffects.get(i);
            if (explosionEffect1 != null) {
                explosionEffect1.draw(graphics);
            }
        }
        if (levelComplete) {
            drawNextLevelMessage();
            // startGameBool = false;
        }

        // Draw score
        graphics.setColor(Color.RED);
        graphics.setFont(getFont().deriveFont(Font.BOLD, 18f));
        graphics.drawString("Ships Destroyed : " + score, 10, 20);
        drawTimer(graphics);
        drawLevel();
        // Draw game over screen if ship is dead
        if (!ship.isIntact()) {
            playerDead = true;
            // Draw game over text
            String text = "YOU HAVE DIED AT LEVEL " + level;
            levelComplete = false;
            graphics.setFont(getFont().deriveFont(Font.BOLD, 25f));
            FontMetrics font = graphics.getFontMetrics();
            Rectangle2D rect = font.getStringBounds(text, graphics);
            double textWidth = rect.getWidth();
            double textHeight = rect.getHeight();
            double x = (w - textWidth) / 2;
            double y = (h - textHeight) / 2;
            graphics.drawString(text, (int) x, (int) y + font.getAscent());

            // Draw key prompt text
            String textKey = "Press the enter key if you wish to Continue ...";
            graphics.setFont(getFont().deriveFont(Font.BOLD, 19f));
            
            font = graphics.getFontMetrics();
            rect = font.getStringBounds(textKey, graphics);
            textWidth = rect.getWidth();
            textHeight = rect.getHeight();
            x = (w - textWidth) / 2;
            y = (h - textHeight) / 2;
            graphics.drawString(textKey, (int) x, (int) y + font.getAscent() + 50);
        }
    }

    private void renderGame() {
        Graphics graphics1 = getGraphics();
        graphics1.drawImage(buffImage, 0, 0, null);
        graphics1.dispose();
    }

}
