import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class MemeRotMemory {

    static Clip menuClip;
    static Font minecraftFont;

  static void setFullScreen(JFrame window) {
    window.setExtendedState(JFrame.MAXIMIZED_BOTH); 
}


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
             super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            
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

        BackgroundPanel background = new BackgroundPanel("src/img/landscape.jpg");
        background.setLayout(new BoxLayout(background, BoxLayout.Y_AXIS));

        // Create rounded buttons
        RoundedButton easy = new RoundedButton("EASY");
        RoundedButton normal = new RoundedButton("NORMAL");
        RoundedButton hard = new RoundedButton("HARD");
        RoundedButton backButton = new RoundedButton("BACK");

        RoundedButton[] buttons = {easy, normal, hard, backButton};

        for (RoundedButton btn : buttons) {
            btn.setFont(minecraftFont.deriveFont(24f));
            btn.setMaximumSize(new Dimension(250, 60));
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        easy.setBackground(new Color(46, 204, 113));
        normal.setBackground(new Color(241, 196, 15));
        hard.setBackground(new Color(231, 76, 60));
        backButton.setBackground(Color.GRAY);

        easy.setForeground(Color.WHITE);
        normal.setForeground(Color.WHITE);
        hard.setForeground(Color.WHITE);
        backButton.setForeground(Color.WHITE);

        background.add(Box.createVerticalStrut(600));
        background.add(easy);
        background.add(Box.createVerticalStrut(20));
        background.add(normal);
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

        normal.addActionListener(e -> {
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
         setFullScreen(window);

    }

    public static void openMainMenu() {
        JFrame window = new JFrame();
        window.setTitle("Meme Rot Memory");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(800, 800);
        window.setLocationRelativeTo(null);
        window.setResizable(true);

        BackgroundPanel menuBackground = new BackgroundPanel("src/img/landscape.jpg");
        menuBackground.setLayout(new BoxLayout(menuBackground, BoxLayout.Y_AXIS));

        RoundedButton start = new RoundedButton("PLAY");
        RoundedButton howToPlay = new RoundedButton("HOW TO PLAY"); 
        RoundedButton exit = new RoundedButton("EXIT");

        start.setFont(minecraftFont.deriveFont(28f));
        howToPlay.setFont(minecraftFont.deriveFont(24f)); 
        exit.setFont(minecraftFont.deriveFont(28f));

        start.setMaximumSize(new Dimension(250, 70));
        howToPlay.setMaximumSize(new Dimension(250, 60));
        exit.setMaximumSize(new Dimension(250, 70));

        start.setAlignmentX(Component.CENTER_ALIGNMENT);
        howToPlay.setAlignmentX(Component.CENTER_ALIGNMENT);
        exit.setAlignmentX(Component.CENTER_ALIGNMENT);

        start.setBackground(new Color(46, 204, 113));
        howToPlay.setBackground(new Color(52, 152, 219));
        exit.setBackground(new Color(231, 76, 60));

        start.setForeground(Color.WHITE);
        howToPlay.setForeground(Color.WHITE);
        exit.setForeground(Color.WHITE);

        menuBackground.add(Box.createVerticalStrut(600));
        menuBackground.add(start);
        menuBackground.add(Box.createVerticalStrut(20));
        menuBackground.add(howToPlay);
        menuBackground.add(Box.createVerticalStrut(25));
        menuBackground.add(exit);
        menuBackground.add(Box.createVerticalGlue());

        start.addActionListener(e -> {
            window.dispose();
            MemeRotMemory.openDifficultyScreen();
        });

        howToPlay.addActionListener(e -> {
             showHowToPlayDialog(window);
    });

        exit.addActionListener(e -> System.exit(0));

        window.add(menuBackground);
        window.setVisible(true);
        setFullScreen(window);
    }

    public static void showHowToPlayDialog(JFrame parent) {
    Color darkNavy   = new Color(0x04, 0x0D, 0x43);
    Color mediumNavy = new Color(0x15, 0x20, 0x55);
    Color gold       = new Color(0xFF, 0xD8, 0x62);
    Color lightGray  = new Color(0xE0, 0xE6, 0xED);

    // ── Load Minecraft font ──────────────────────────────────────────────────
    Font minecraftBase;
    try {
        minecraftBase = Font.createFont(Font.TRUETYPE_FONT, new File("src/fonts/Minecraft.otf"));
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(minecraftBase);
    } catch (FontFormatException | IOException e) {
        e.printStackTrace();
        minecraftBase = new Font("Monospaced", Font.PLAIN, 12); // fallback
    }

    // Derived sizes
    final Font fontTitle    = minecraftBase.deriveFont(Font.PLAIN, 20f);
    final Font fontSubtitle = minecraftBase.deriveFont(Font.PLAIN, 11f);
    final Font fontHeading  = minecraftBase.deriveFont(Font.PLAIN, 13f);
    final Font fontBody     = minecraftBase.deriveFont(Font.PLAIN, 11f);
    final Font fontButton   = minecraftBase.deriveFont(Font.PLAIN, 12f);
    final Font fontFooter   = minecraftBase.deriveFont(Font.PLAIN, 11f);

    JDialog dialog = new JDialog(parent, "HOW TO PLAY - Meme Rot Memory", true);
    dialog.setSize(520, 640);
    dialog.setLocationRelativeTo(parent);
    dialog.setResizable(false);

    // ── Root panel ───────────────────────────────────────────────────────────
    JPanel root = new JPanel(new BorderLayout());
    root.setBackground(darkNavy);
    root.setBorder(BorderFactory.createLineBorder(gold, 2));

    // ── Header ───────────────────────────────────────────────────────────────
    JPanel header = new JPanel(new BorderLayout());
    header.setBackground(mediumNavy);
    header.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

    JLabel title = new JLabel("HOW TO PLAY", SwingConstants.CENTER);
    title.setFont(fontTitle);
    title.setForeground(gold);

    JLabel subtitle = new JLabel("Meme Rot Memory", SwingConstants.CENTER);
    subtitle.setFont(fontSubtitle);
    subtitle.setForeground(lightGray);

    header.add(title, BorderLayout.CENTER);
    header.add(subtitle, BorderLayout.SOUTH);

    // ── Scroll body ──────────────────────────────────────────────────────────
    JPanel body = new JPanel();
    body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
    body.setBackground(darkNavy);
    body.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

    body.add(buildSection("GAME RULES", new String[]{
        "* Click on cards to flip them over",
        "* Find and match two identical cards",
        "* Match all pairs before time runs out!"
    }, gold, lightGray, mediumNavy, fontHeading, fontBody));

    body.add(Box.createVerticalStrut(10));

    body.add(buildSection("TIME SYSTEM", new String[]{
        "* Wrong match:   -5 seconds",
        "* Correct match: +3 seconds"
    }, gold, lightGray, mediumNavy, fontHeading, fontBody));

    body.add(Box.createVerticalStrut(10));

    body.add(buildSection("SCORE & COINS", new String[]{
        "* Each correct match: +10 points",
        "* Each correct match: +5 coins",
        "* High score is saved automatically!"
    }, gold, lightGray, mediumNavy, fontHeading, fontBody));

    body.add(Box.createVerticalStrut(10));

    body.add(buildSection("SHOP ITEMS", new String[]{
        "* BOOSTER (10 coins): +10 seconds to timer",
        "* HINT   (8 coins):  Shows cards for 1.5s"
    }, gold, lightGray, mediumNavy, fontHeading, fontBody));

    body.add(Box.createVerticalStrut(10));

    body.add(buildSection("TIPS", new String[]{
        "* Try to remember card positions",
        "* Buy boosters when time is low",
        "* Use hints to find hard-to-match pairs",
        "* Save coins for boosters and hints"
    }, gold, lightGray, mediumNavy, fontHeading, fontBody));

    JScrollPane scroll = new JScrollPane(body);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    scroll.setBackground(darkNavy);
    scroll.getViewport().setBackground(darkNavy);
    scroll.getVerticalScrollBar().setUnitIncrement(12);

    // ── Footer ───────────────────────────────────────────────────────────────
    JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 12));
    footer.setBackground(mediumNavy);

    JLabel luck = new JLabel("Good luck and have fun!");
    luck.setFont(fontFooter);
    luck.setForeground(gold);

    JButton closeBtn = new JButton("Got it!");
    closeBtn.setFont(fontButton);
    closeBtn.setBackground(gold);
    closeBtn.setForeground(darkNavy);
    closeBtn.setFocusPainted(false);
    closeBtn.setBorder(BorderFactory.createEmptyBorder(8, 28, 8, 28));
    closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    closeBtn.addActionListener(e -> dialog.dispose());

    closeBtn.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseEntered(java.awt.event.MouseEvent e) {
            closeBtn.setBackground(new Color(0xFF, 0xC0, 0x30));
        }
        public void mouseExited(java.awt.event.MouseEvent e) {
            closeBtn.setBackground(gold);
        }
    });

    footer.add(luck);
    footer.add(Box.createHorizontalStrut(20));
    footer.add(closeBtn);

    // ── Assemble ─────────────────────────────────────────────────────────────
    root.add(header, BorderLayout.NORTH);
    root.add(scroll,  BorderLayout.CENTER);
    root.add(footer, BorderLayout.SOUTH);

    dialog.setContentPane(root);
    dialog.setVisible(true);
}

// ── Section builder ───────────────────────────────────────────────────────────
private static JPanel buildSection(String heading, String[] lines,
                                   Color gold, Color lightGray, Color mediumNavy,
                                   Font fontHeading, Font fontBody) {
    JPanel card = new JPanel();
    card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
    card.setBackground(mediumNavy);
    card.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(gold.darker(), 1),
        BorderFactory.createEmptyBorder(10, 14, 12, 14)
    ));
    card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

    JLabel hdr = new JLabel(heading);
    hdr.setFont(fontHeading);
    hdr.setForeground(gold);
    hdr.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
    card.add(hdr);

    for (String line : lines) {
        JLabel lbl = new JLabel(line);
        lbl.setFont(fontBody);
        lbl.setForeground(lightGray);
        lbl.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 0));
        card.add(lbl);
    }

    return card;
}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> showLoadingScreen());
    }
     
}