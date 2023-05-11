import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.GradientPaint;

//Jarod Esareesingh 
//816026811
public class HealthRender {

    private final Health health;

    public HealthRender(Health health) {
        this.health = health;
    }
    
    protected void renderHealthBar(Graphics2D g2, Shape shape, double y) {
        if (health.getCurrentHealth() != health.getMAXhealth()) {
            double healthY = shape.getBounds().getY() - y - 10;
    
            // Draw background (black) for health bar
            g2.setColor(Color.BLACK);
            g2.fill(new Rectangle2D.Double(0, healthY, Ship.shipSize, 6));
    
            // Draw gradient health bar
            double healthSize = health.getCurrentHealth() / health.getMAXhealth() * Ship.shipSize;
            GradientPaint gradient = new GradientPaint(0, (float) healthY, new Color(91, 186, 253), (float) healthSize, (float) healthY, new Color(253, 91, 91));
            g2.setPaint(gradient);
            g2.fill(new Rectangle2D.Double(0, healthY + 1, healthSize, 4));
    
            // Draw border around health bar
            g2.setColor(Color.WHITE);
            g2.draw(new Rectangle2D.Double(0, healthY, Ship.shipSize, 6));
    
            // Draw animated glow effect
            long currentTime = System.currentTimeMillis();
            float glowIntensity = (float) (0.5 + 0.5 * Math.sin(currentTime * 0.005));
            Color glowColor = new Color(1.0f, 1.0f, 1.0f, glowIntensity);
            g2.setColor(glowColor);
            g2.draw(new Rectangle2D.Double(0, healthY - 1, healthSize, 8));
        }
    }
    
    
    public boolean updateHealth(double damage) {
        health.setCurrentHealth(health.getCurrentHealth() - damage);
        return health.getCurrentHealth() > 0;
    }
    
    public double getHealth() {
        return health.getCurrentHealth();
    }
    
    public void resetHealth() {
        health.setCurrentHealth(health.getMAXhealth());
    }
}
