import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.sound.sampled.*;
import java.io.File;

public class MemeRotMemory {

    static Clip menuClip;
    //static boolean muted = false;
    static Font minecraftFont;

    // LOAD MINECRAFT FONT
    static void loadMinecraftFont() {
        try {
            // Try to load custom Minecraft font from file
            minecraftFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/fonts/Minecraft.otf"));
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(minecraftFont);
            minecraftFont = minecraftFont.deriveFont(Font.BOLD, 28f);
        } catch (Exception e) {
            // Fallback to default bold font if Minecraft font not found
            minecraftFont = new Font("Arial", Font.BOLD, 28);
            System.out.println("Minecraft font not found, using Arial instead");
        }
    }

    // ROUNDED BUTTON CLASS
    static class RoundedButton extends JButton {
        private int radius = 20;
        
        public RoundedButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw rounded background
            if (getModel().isPressed()) {
                g2.setColor(getBackground().darker());
            } else if (getModel().isRollover()) {
                g2.setColor(getBackground().brighter());
            } else {
                g2.setColor(getBackground());
            }
            
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), radius, radius));
            
            // Draw text
            g2.setColor(getForeground());
            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(getText(), x, y);
            
            g2.dispose();
        }
    }

    // CUSTOM JPANEL TO DRAW THE IMAGE PROPERLY
    static class BackgroundPanel extends JPanel {
        private Image img;

        public BackgroundPanel(String filePath) {
            this.img = new ImageIcon(filePath).getImage();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
        }
    }

    // LOADING SCREEN
    public static void showLoadingScreen() {
        loadMinecraftFont();
        
        JWindow splash = new JWindow();
        splash.setSize(800, 800);
        splash.setLocationRelativeTo(null);

        BackgroundPanel splashPanel = new BackgroundPanel("src/img/bgmenu.png");
        splashPanel.setLayout(new BoxLayout(splashPanel, BoxLayout.Y_AXIS));

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setFont(minecraftFont.deriveFont(14f));
        progressBar.setForeground(new Color(241, 196, 15));
        progressBar.setBackground(new Color(20, 24, 46));
        progressBar.setMaximumSize(new Dimension(400, 30));
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        splashPanel.add(Box.createVerticalStrut(710));
        splashPanel.add(progressBar);
        splashPanel.add(Box.createVerticalGlue());

        splash.add(splashPanel);
        splash.setVisible(true);

        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (int i = 0; i <= 100; i += 4) {
                    Thread.sleep(80);
                    publish(i);
                }
                playMenuMusic();
                return null;
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                int latestProgress = chunks.get(chunks.size() - 1);
                progressBar.setValue(latestProgress);
            }

            @Override
            protected void done() {
                splash.dispose();
                openMainMenu();
            }
        };
        worker.execute();
    }

    public static void playMenuMusic() {
        try {
            if (menuClip != null && menuClip.isRunning()) {
                menuClip.stop();
                menuClip.close();
                menuClip = null;
            }

            AudioInputStream audio = AudioSystem.getAudioInputStream(new File("sounds/menu.wav"));
            menuClip = AudioSystem.getClip();
            menuClip.open(audio);
            menuClip.loop(Clip.LOOP_CONTINUOUSLY);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void openDifficultyScreen() {
        if (menuClip != null) {
            menuClip.stop();
            menuClip.close();
            menuClip = null;
        }
        playMenuMusic();

        JFrame window = new JFrame();
        window.setTitle("Meme Rot Memory");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(800, 800);
        window.setLocationRelativeTo(null);
        window.setResizable(true);

        BackgroundPanel background = new BackgroundPanel("background.png");
        background.setLayout(new BoxLayout(background, BoxLayout.Y_AXIS));

        // Create rounded buttons
        RoundedButton easy = new RoundedButton("EASY");
        RoundedButton medium = new RoundedButton("MEDIUM");
        RoundedButton hard = new RoundedButton("HARD");
        RoundedButton backButton = new RoundedButton("BACK");

        RoundedButton[] buttons = {easy, medium, hard, backButton};

        for (RoundedButton btn : buttons) {
            btn.setFont(minecraftFont.deriveFont(24f));
            btn.setMaximumSize(new Dimension(250, 60));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        easy.setBackground(new Color(46, 204, 113));
        medium.setBackground(new Color(241, 196, 15));
        hard.setBackground(new Color(231, 76, 60));
        backButton.setBackground(Color.GRAY);

        easy.setForeground(Color.WHITE);
        medium.setForeground(Color.WHITE);
        hard.setForeground(Color.WHITE);
        backButton.setForeground(Color.WHITE);

        background.add(Box.createVerticalStrut(400));
        background.add(easy);
        background.add(Box.createVerticalStrut(20));
        background.add(medium);
        background.add(Box.createVerticalStrut(20));
        background.add(hard);
        background.add(Box.createVerticalStrut(20));
        background.add(backButton);
        background.add(Box.createVerticalGlue());

        easy.addActionListener(e -> {
            if (menuClip != null) {
                menuClip.stop();
                menuClip.close();
                menuClip = null;
            }
            window.dispose();
            new MatchCards("easy");
        });

        medium.addActionListener(e -> {
            if (menuClip != null) {
                menuClip.stop();
                menuClip.close();
                menuClip = null;
            }
            window.dispose();
            new MatchCards("normal");
        });

        hard.addActionListener(e -> {
            if (menuClip != null) {
                menuClip.stop();
                menuClip.close();
                menuClip = null;
            }
            window.dispose();
            new MatchCards("hard");
        });

        backButton.addActionListener(e -> {
            window.dispose();
            openMainMenu();
        });

        window.add(background);
        window.setVisible(true);
    }

    public static void openMainMenu() {
        JFrame window = new JFrame();
        window.setTitle("Meme Rot Memory");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(800, 800);
        window.setLocationRelativeTo(null);
        window.setResizable(true);

        BackgroundPanel menuBackground = new BackgroundPanel("background.png");
        menuBackground.setLayout(new BoxLayout(menuBackground, BoxLayout.Y_AXIS));

        RoundedButton start = new RoundedButton("PLAY");
        RoundedButton exit = new RoundedButton("EXIT");

        start.setFont(minecraftFont.deriveFont(28f));
        exit.setFont(minecraftFont.deriveFont(28f));

        start.setMaximumSize(new Dimension(250, 70));
        exit.setMaximumSize(new Dimension(250, 70));
        start.setAlignmentX(Component.CENTER_ALIGNMENT);
        exit.setAlignmentX(Component.CENTER_ALIGNMENT);

        start.setBackground(new Color(46, 204, 113));
        exit.setBackground(new Color(231, 76, 60));
        start.setForeground(Color.WHITE);
        exit.setForeground(Color.WHITE);

        menuBackground.add(Box.createVerticalStrut(420));
        menuBackground.add(start);
        menuBackground.add(Box.createVerticalStrut(25));
        menuBackground.add(exit);
        menuBackground.add(Box.createVerticalGlue());

        start.addActionListener(e -> {
            window.dispose();
            MemeRotMemory.openDifficultyScreen();
        });

        exit.addActionListener(e -> System.exit(0));

        window.add(menuBackground);
        window.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> showLoadingScreen());
    }
}