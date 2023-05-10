import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

//Jarod Esareesingh 
//816026811
public class Missile {

    public float missileAngle;
    public double missileSize;
    public float missileSpeed = 1f;
    public double x;
    public double y;
    public Shape shape;
    public BufferedImage shotImage;
    public Color color = new Color(255, 0, 0);
    public BufferedImage[] shotImages;
    public int currentFrame = 0;
    public long lastFrameTime = System.currentTimeMillis();
    public long frameDuration = 80; // Duration for each frame in milliseconds

    // Constructor for missile class
    public Missile(double x, double y, float missileAngle, double missileSize, float missileSpeed) {
        // Calculate the spawn point for the missile based on ship's position, angle and
        // size
        double shipCenterX = x + Ship.shipSize / 2;
        double shipCenterY = y + Ship.shipSize / 2;
        double spawnDistance = Ship.shipSize / 2 + missileSize;
        double spawnX = shipCenterX + Math.cos(Math.toRadians(missileAngle)) * spawnDistance;
        double spawnY = shipCenterY + Math.sin(Math.toRadians(missileAngle)) * spawnDistance;

        // Set missile properties
        this.x = spawnX - missileSize / 2 ;
        this.y = spawnY - missileSize / 2 - 20;
        this.missileAngle = missileAngle;
        this.missileSize = missileSize;
        this.missileSpeed = missileSpeed;
        shape = new Ellipse2D.Double(0, 0, missileSize, missileSize);
        shotImages = new BufferedImage[4];
        for (int i = 0; i < 4; i++) {
            shotImages[i] = ImageManager.loadBufferedImage("Shot1/shot1_" + (i + 1) + ".png");
        }
    }

    public Shape getMissileShape() {
        return new Area(new Ellipse2D.Double(x, y, missileSize, missileSize));
    }

    public double returnX() {
        return x;
    }

    public double returnY() {
        return y;
    }

    public double returnSize() {
        return missileSize;
    }

    public double returnCenterX() {
        return x + missileSize / 2;
    }

    public double returnCenterY() {
        return y + missileSize / 2;
    }

    // Update the position of the missile
    public void updateMissile() {
        x += Math.cos(Math.toRadians(missileAngle)) * missileSpeed;
        y += Math.sin(Math.toRadians(missileAngle)) * missileSpeed;
        // Update frame index based on elapsed time
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameTime >= frameDuration) {
            currentFrame = (currentFrame + 1) % shotImages.length;
            lastFrameTime = currentTime;
        }
    }

    // Check if missile is out of bounds of the game window
    public boolean checkMissile(int width, int height) {
        boolean inBounds = (x > -missileSize && y > -missileSize && x < width && y < height);
        return inBounds;
    }

    // Draw the missile shape onto the graphics context
    public void draw(Graphics2D g2) {
        // Save the current graphics transform
        AffineTransform oldTransform = g2.getTransform();

        // Set missile color and position
        g2.translate(x, y);

        // Apply rotation based on missile angle
        g2.rotate(Math.toRadians(missileAngle), missileSize / 2, missileSize / 2);

        // Draw missile shape scaled by a factor of 2
        BufferedImage currentImage = shotImages[currentFrame];
        int scaledWidth = currentImage.getWidth() * 2;
        int scaledHeight = currentImage.getHeight() * 2;
        g2.drawImage(currentImage, 0, 0, scaledWidth, scaledHeight, null);

        // Reset graphics transform
        g2.setTransform(oldTransform);
    }

}
