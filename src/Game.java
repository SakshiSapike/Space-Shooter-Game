import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

// BackgroundImagePanel class
class BackgroundImagePanel extends JPanel {
    private final Image backgroundImage;

    public BackgroundImagePanel() {
        // Load the image from the specified path
        ImageIcon icon = new ImageIcon("C:\\Users\\saksh\\IdeaProjects\\DSA\\out\\production\\DSA\\ssss.jpg");
        if (icon.getImageLoadStatus() != MediaTracker.ERRORED) {
            backgroundImage = icon.getImage();
        } else {
            System.err.println("Image not found!");
            // Set a default background color
            backgroundImage = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
            Graphics g = backgroundImage.getGraphics();
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, 800, 600);
            g.dispose();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw the background image
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
    }
}

// Game class
public class Game extends BackgroundImagePanel implements ActionListener {
    private Timer timer;
    private Player player;
    ArrayList<Bullet> bullets;
    private ArrayList<Enemy> enemies;
    private boolean gameOver;
    private int score; // Score variable

    public Game() {
        setFocusable(true);
        setDoubleBuffered(true);

        player = new Player(400, 500, this);  // Pass 'this' to provide Game reference to Player
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();
        score = 0; // Initialize score

        timer = new Timer(10, this); // 10 ms game loop
        timer.start();

        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
                if (!gameOver) {
                    player.keyPressed(evt);
                }
            }

            public void keyReleased(KeyEvent evt) {
                if (!gameOver) {
                    player.keyReleased(evt);
                }
            }
        });

        gameOver = false;
        spawnEnemies();
    }

    private void spawnEnemies() {
        // Spawn enemies at random positions with delay for continuous spawning
        new Thread(() -> {
            while (!gameOver) {
                try {
                    Thread.sleep(2000); // 2 seconds between enemy spawns
                    enemies.add(new Enemy((int) (Math.random() * 800), -20)); // spawn above the screen
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g); // Call the superclass method to draw the background
        if (!gameOver) {
            player.draw(g);

            for (Bullet b : bullets) {
                b.draw(g);
            }

            for (Enemy e : enemies) {
                e.draw(g);
            }

            // Display score
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Score: " + score, 10, 20); // Draw score at top-left corner
        } else {
            // Display game over screen
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("Game Over!", 300, 300);

            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Score: " + score, 350, 350); // Show final score
            g.drawString("Press ENTER to Play Again", 290, 400);
        }

        Toolkit.getDefaultToolkit().sync();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (!gameOver) {
            player.move();

            for (Bullet b : bullets) {
                b.move();
            }

            for (Enemy e : enemies) {
                e.move();
            }

            checkCollisions();
        }
        repaint();
    }

    public void checkCollisions() {
        ArrayList<Bullet> bulletsToRemove = new ArrayList<>();
        ArrayList<Enemy> enemiesToRemove = new ArrayList<>();

        // Check bullet-enemy collisions
        for (Bullet b : bullets) {
            for (Enemy e : enemies) {
                if (b.getBounds().intersects(e.getBounds())) {
                    bulletsToRemove.add(b);
                    enemiesToRemove.add(e);
                    score++; // Increment score when an enemy is destroyed
                }
            }
        }

        bullets.removeAll(bulletsToRemove);
        enemies.removeAll(enemiesToRemove);


        // Check player-enemy collisions (Game Over condition)
        for (Enemy e : enemies) {
            if (player.getBounds().intersects(e.getBounds())) {
                gameOver = true;
                timer.stop();
                askPlayAgain();
                return;
            }
        }
    }

    private void askPlayAgain() {
        // Create a custom JPanel to hold the game over message and score
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.BLACK);

        JLabel gameOverLabel = new JLabel("Game Over!");
        gameOverLabel.setFont(new Font("Arial", Font.BOLD, 30));
        gameOverLabel.setForeground(Color.RED);
        gameOverLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel scoreLabel = new JLabel("Your Score: " + score);
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 20));
        scoreLabel.setForeground(Color.white);
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel playAgainLabel = new JLabel("Do you want to play again?");
        playAgainLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        playAgainLabel.setForeground(Color.YELLOW);
        playAgainLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add labels to the panel
        panel.add(gameOverLabel);
        panel.add(Box.createVerticalStrut(10)); // Adds spacing between components
        panel.add(scoreLabel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(playAgainLabel);

        // Display the dialog with custom panel
        int response = JOptionPane.showConfirmDialog(
                this,
                panel,
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (response == JOptionPane.YES_OPTION) {
            resetGame();
        } else {
            System.exit(0);
        }
    }



    private void resetGame() {
        gameOver = false;
        player = new Player(400, 500, this);
        bullets.clear();
        enemies.clear();
        score = 0; // Reset score
        timer.start();
        spawnEnemies(); // Restart enemy spawning
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Space Shooter");
        Game game = new Game();
        frame.add(game);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Set full-screen mode
        frame.setUndecorated(true); // Remove borders
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

}

// GameObject class
abstract class GameObject {
    protected int x, y, width, height;

    public GameObject(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void move();
    public abstract void draw(Graphics g);

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}

// Bullet class
class Bullet extends GameObject {
    private Image bulletImage;  // Add bullet image

    public Bullet(int x, int y) {
        super(x, y, 15, 75); // bullet size

        // Load the bullet image
        ImageIcon icon = new ImageIcon("C:\\Users\\saksh\\IdeaProjects\\DSA\\out\\production\\DSA\\bullet.png"); // Set your own image path
        if (icon.getImageLoadStatus() != MediaTracker.ERRORED) {
            bulletImage = icon.getImage();
        } else {
            System.err.println("Bullet image not found!");
            // Use a default visual if image is not found
            bulletImage = new BufferedImage(5, 10, BufferedImage.TYPE_INT_RGB);
            Graphics g = bulletImage.getGraphics();
            g.setColor(Color.YELLOW);
            g.fillRect(0, 0, 5, 10);
            g.dispose();
        }
    }

    @Override
    public void move() {
        y -= 5;  // Bullet moves up
    }

    @Override
    public void draw(Graphics g) {
        // Draw bullet image
        g.drawImage(bulletImage, x, y, width, height, null);
    }
}


// Enemy class
class Enemy extends GameObject {
    private Image enemyImage;

    public Enemy(int x, int y) {
        super(x, y, 50, 50);  // enemy size

        // Load the enemy image
        ImageIcon icon = new ImageIcon("C:\\Users\\saksh\\IdeaProjects\\DSA\\out\\production\\DSA\\monste.png"); // Set your own image path
        if (icon.getImageLoadStatus() != MediaTracker.ERRORED) {
            enemyImage = icon.getImage();
        } else {
            System.err.println("Enemy image not found!");
            // Use a default visual if image is not found
            enemyImage = new BufferedImage(80, 80, BufferedImage.TYPE_INT_RGB);
            Graphics g = enemyImage.getGraphics();
            g.setColor(Color.RED);
            g.fillRect(0, 0, 60, 60);
            g.dispose();
        }
    }

    @Override
    public void move() {
        y += 1;  // Enemy moves down
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(enemyImage, x, y, width, height, null);  // Draw the enemy image
    }
}


// Player class
class Player extends GameObject {
    private int dx, dy;
    private Game game;  // Reference to Game
    private Image playerImage;  // Add player image

    public Player(int x, int y, Game game) {
        super(x, y, 80, 80);  // player size
        this.game = game;     // Store the Game reference

        // Load the player image
        ImageIcon icon = new ImageIcon("C:\\Users\\saksh\\IdeaProjects\\DSA\\out\\production\\DSA\\soilder.png"); // Set your own image path
        if (icon.getImageLoadStatus() != MediaTracker.ERRORED) {
            playerImage = icon.getImage();
        } else {
            System.err.println("Player image not found!");
            // Use a default visual if image is not found
            playerImage = new BufferedImage(30, 30, BufferedImage.TYPE_INT_RGB);
            Graphics g = playerImage.getGraphics();
            g.setColor(Color.GREEN);
            g.fillRect(0, 0, 30, 30);
            g.dispose();
        }
    }

    public void move() {
        x += dx;
        y += dy;
        if (x < 0) x = 0;
        if (x > 770) x = 770;
        if (y < 0) y = 0;
        if (y > 570) y = 570;
    }

    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT) {
            dx = -2;
        }

        if (key == KeyEvent.VK_RIGHT) {
            dx = 2;
        }

        if (key == KeyEvent.VK_UP) {
            dy = -2;
        }

        if (key == KeyEvent.VK_DOWN) {
            dy = 2;
        }

        if (key == KeyEvent.VK_SPACE) {
            fire();
        }
    }

    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT) {
            dx = 0;
        }

        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN) {
            dy = 0;
        }
    }

    public void fire() {
        // Add bullet to the Game's bullets list
        game.bullets.add(new Bullet(x + width / 2 - 2, y));
    }

    public void draw(Graphics g) {
        // Draw player image
        g.drawImage(playerImage, x, y, width, height, null);
    }
}