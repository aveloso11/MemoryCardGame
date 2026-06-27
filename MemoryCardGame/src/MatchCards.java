import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.sound.sampled.*;
import java.io.File;

public class MatchCards {

    Clip menuClip;
    Clip gameClip;
    boolean muted = false;
    Clip winClip;
    Clip loseClip;
    Clip flipClip;

    // ── MINECRAFT FONT (loaded once, reused everywhere) ──────────────────────
    Font minecraftBase;

    // ── PALETTE ──────────────────────────────────────────────────────────────
    final Color DARK_NAVY   = new Color(0x04, 0x0D, 0x43);
    final Color MEDIUM_NAVY = new Color(0x15, 0x20, 0x55);
    final Color GOLD        = new Color(0xFF, 0xD8, 0x62);
    final Color LIGHT_GRAY  = new Color(0xE0, 0xE6, 0xED);
    final Color BORDER_BLUE = new Color(0x7F, 0x8E, 0xE3);

    class Card {
        String cardName;
        ImageIcon cardImageIcon;

        Card(String cardName, ImageIcon cardImageIcon) {
            this.cardName = cardName;
            this.cardImageIcon = cardImageIcon;
        }

        public String toString() {
            return cardName;
        }
    }

    class Confetti {
        float x, y;
        float dx;
        float speed;
        int size;
        Color color;
        int type;
        float angle;
        float angleSpeed;
        float waveOffset;
        float waveSpeed;

        Confetti(float x, float y, int size, float speed, Color color, int type) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.speed = speed;
            this.color = color;
            this.type = type;
            this.dx = (float)(Math.random() * 2 - 1);
            this.angle = (float)(Math.random() * 360);
            this.angleSpeed = (float)(Math.random() * 6 - 3);
            this.waveOffset = (float)(Math.random() * Math.PI * 2);
            this.waveSpeed = (float)(0.05 + Math.random() * 0.1);
        }

        void fall() {
            y += speed;
            x += dx;
            angle += angleSpeed;
            waveOffset += waveSpeed;
            x += (float)(Math.sin(waveOffset) * 0.5);
        }
    }

    // DIFFICULTY CARD LISTS
    String[] easyCardList = { "mm1", "mm2", "mm3", "mm4", "mm5", "mm6" };
    String[] normalCardList = { "mm1", "mm2", "mm3", "mm4", "mm5", "mm6", "mm7", "mm8" };
    String[] hardCardList = { "mm6", "mm7", "mm8", "mm9", "mm10", "mm11", "mm12", "mm13", "mm14", "mm15" };

    String[] currentCardList;
    int rows = 4;
    int columns = 5;
    int cardWidth = 90;
    int cardHeight = 90;

    ArrayList<Card> cardSet;
    ImageIcon cardBackImageIcon;

    ArrayList<Confetti> confettiList = new ArrayList<>();
    Timer confettiTimer;

    int boardWidth = columns * cardWidth;
    int boardHeight = rows * cardHeight;

    // GAME PANEL
    JFrame frame = new JFrame("Meme Rot Memory");
    JLabel timerLabel = new JLabel();
    JLabel scoreLabel = new JLabel();
    JLabel coinsLabel = new JLabel();
    JLabel difficultyLabel = new JLabel();
    JPanel textPanel = new JPanel();
    JPanel boardPanel = new JPanel();

    // CONFETTI PANEL
    JPanel confettiPanel = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (Confetti c : confettiList) {
                Graphics2D g2 = (Graphics2D) g2d.create();
                g2.setColor(c.color);
                g2.translate(c.x + c.size / 2.0, c.y + c.size / 2.0);
                g2.rotate(Math.toRadians(c.angle));
                int half = c.size / 2;
                switch (c.type) {
                    case 0:
                        g2.fillRect(-half, -half / 2, c.size, c.size / 2);
                        break;
                    case 1:
                        g2.fillOval(-half, -half, c.size, c.size);
                        break;
                    case 2:
                        int[] xp = { 0, half, 0, -half };
                        int[] yp = { -half, 0, half, 0 };
                        g2.fillPolygon(xp, yp, 4);
                        break;
                    case 3:
                        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        java.awt.geom.GeneralPath path = new java.awt.geom.GeneralPath();
                        path.moveTo(-c.size, 0);
                        path.curveTo(-c.size / 2f, -c.size / 2f, c.size / 2f, c.size / 2f, c.size, 0);
                        g2.draw(path);
                        break;
                    default:
                        g2.fillRect(-half, -half / 2, c.size, c.size / 2);
                        break;
                }
                g2.dispose();
            }
        }
    };

    JPanel centerWrapper;
    JPanel bottomPanel;
    JPanel centerPanel;
    JPanel rightPanel;

    JDialog scoreboardDialog;
    JLabel highScoreLabel;
    int highScore = 0;
    JButton pauseButton;

    int boosterCount = 0;
    int hintCount = 0;
    boolean hintActive = false;
    int coins = 0;
    JButton boosterButton;
    JButton tryAgainButton;
    JButton hintButton;

    int timeLeft = 60;
    int score = 0;
    Timer gameTimer;
    boolean gameActive = true;
    ArrayList<JButton> board;
    Timer hideCardTimer;
    boolean gameReady = false;
    JButton card1Selected;
    JButton card2Selected;

    String currentDifficulty = "hard";

    // ── CONSTRUCTOR #1 ────────────────────────────────────────────────────────
    MatchCards() {
        this("hard");
    }

    // ── CONSTRUCTOR #2 ────────────────────────────────────────────────────────
    MatchCards(String difficulty) {
        currentDifficulty = difficulty;

        // LOAD MINECRAFT FONT ONCE
        try {
            minecraftBase = Font.createFont(Font.TRUETYPE_FONT, new File("src/fonts/Minecraft.otf"));
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(minecraftBase);
        } catch (Exception e) {
            minecraftBase = new Font("Monospaced", Font.PLAIN, 12);
        }

        switch (difficulty) {
            case "easy":
                rows = 3; columns = 4;
                cardWidth = 220; cardHeight = 270;
                currentCardList = easyCardList;
                break;
            case "normal":
                rows = 4; columns = 4;
                cardWidth = 200; cardHeight = 220;
                currentCardList = normalCardList;
                break;
            case "hard":
                rows = 4; columns = 5;
                cardWidth = 190; cardHeight = 220;
                currentCardList = hardCardList;
                break;
        }

        boardWidth = columns * cardWidth;
        boardHeight = rows * cardHeight;

        setupCards();
        shuffleCards();
        loadHighScore();

        frame.setLayout(new BorderLayout());

        JPanel westPanel = new JPanel();
        westPanel.setPreferredSize(new Dimension(50, 0));
        westPanel.setOpaque(false);
        frame.add(westPanel, BorderLayout.WEST);

        JPanel eastPanel = new JPanel();
        eastPanel.setPreferredSize(new Dimension(50, 0));
        eastPanel.setOpaque(false);
        frame.add(eastPanel, BorderLayout.EAST);

        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        createScoreboardDialog();

        // TOP PANEL
        textPanel.setLayout(new BorderLayout());
        textPanel.setPreferredSize(new Dimension(boardWidth, 60));
        textPanel.setBackground(new Color(30, 30, 60));

        centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 200, 10));
        centerPanel.setBackground(new Color(30, 30, 60));

        timerLabel.setFont(new Font("Arial", Font.BOLD, 28));
        timerLabel.setText("Time: " + timeLeft + "s");
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        difficultyLabel.setFont(new Font("Arial", Font.BOLD, 28));
        difficultyLabel.setForeground(Color.CYAN);
        difficultyLabel.setText("Difficulty: " + currentDifficulty.toUpperCase());
        difficultyLabel.setHorizontalAlignment(SwingConstants.CENTER);

        scoreLabel.setFont(new Font("Arial", Font.BOLD, 28));
        scoreLabel.setText("Score: 0");
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);

        coinsLabel.setFont(new Font("Arial", Font.BOLD, 28));
        coinsLabel.setForeground(new Color(255, 215, 0));
        coinsLabel.setText("Coins: 0");
        coinsLabel.setHorizontalAlignment(SwingConstants.CENTER);

        centerPanel.add(timerLabel);
        centerPanel.add(difficultyLabel);
        centerPanel.add(scoreLabel);
        centerPanel.add(coinsLabel);

        rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        rightPanel.setBackground(new Color(30, 30, 60));

        pauseButton = new JButton("⏸️");
        pauseButton.setFont(new Font("Segoe UI Symbol", Font.BOLD, 24));
        pauseButton.setBackground(new Color(70, 70, 100));
        pauseButton.setForeground(Color.WHITE);
        pauseButton.setFocusPainted(false);
        pauseButton.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        pauseButton.addActionListener(e -> togglePause());

        JButton soundButton = new JButton("🔊");
        soundButton.setFont(new Font("Segoe UI Symbol", Font.BOLD, 24));
        soundButton.setBackground(new Color(70, 70, 100));
        soundButton.setForeground(Color.WHITE);
        soundButton.setFocusPainted(false);
        soundButton.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        soundButton.addActionListener(e -> toggleSound(soundButton));

        JButton homeButton = new JButton("🏠");
        homeButton.setFont(new Font("Segoe UI Symbol", Font.BOLD, 24));
        homeButton.setBackground(new Color(70, 70, 100));
        homeButton.setForeground(Color.WHITE);
        homeButton.setFocusPainted(false);
        homeButton.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        homeButton.addActionListener(e -> backToMainMenu());

        rightPanel.add(pauseButton);
        rightPanel.add(soundButton);
        rightPanel.add(homeButton);

        textPanel.add(centerPanel, BorderLayout.CENTER);
        textPanel.add(rightPanel, BorderLayout.EAST);
        frame.add(textPanel, BorderLayout.NORTH);

        board = new ArrayList<JButton>();
        boardPanel.setLayout(new GridLayout(rows, columns, 5, 5));
        boardPanel.setOpaque(false);
        board = new ArrayList<JButton>();

        for (int i = 0; i < cardSet.size(); i++) {
            JButton tile = new JButton();
            tile.setPreferredSize(new Dimension(cardWidth, cardHeight));
            tile.setIcon(cardSet.get(i).cardImageIcon);
            tile.setFocusable(false);
            tile.setContentAreaFilled(false);
            tile.setOpaque(false);
            tile.setBorder(BorderFactory.createLineBorder(Color.WHITE, 4));

            // CARD CLICK HANDLER - CORE GAME LOGIC
            tile.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!gameReady) return;
                    JButton tile = (JButton) e.getSource();
                    if (tile.getIcon() != cardBackImageIcon) return; // UNABLE CLICKS ON A CARD THATS FACE UP
                    if (hideCardTimer != null && hideCardTimer.isRunning()) return; // UNABLE CLICKS IF CARDS ARE NOT MATCH

                    // FIRST CARD SELECTION
                    if (card1Selected == null) {
                        card1Selected = tile; // CARD 1 SELECTED
                        int index = board.indexOf(card1Selected);
                        card1Selected.setIcon(cardSet.get(index).cardImageIcon); // FLIP FACE UP
                        playFlipSound();
                    
                    // SECOND CARD SELECTION 
                    } else if (card2Selected == null && tile != card1Selected) {
                        card2Selected = tile; // CARD 2 SELECTED
                        int index = board.indexOf(card2Selected);   
                        card2Selected.setIcon(cardSet.get(index).cardImageIcon); // FLIP FACE UP
                        playFlipSound();
                        
                        // MATCH CARD CHECKER 
                        if (card1Selected.getIcon() != card2Selected.getIcon()) {
                            timeLeft -= 5; // WRONG GUESS -5 SECONDS
                            timerLabel.setText("Time: " + timeLeft + "s"); // ON SCREEN TIMER 
                            hideCardTimer = new Timer(1000, new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    // IF NOT MATCH FLIP BOTH CARDS BACK
                                    card1Selected.setIcon(cardBackImageIcon);
                                    card2Selected.setIcon(cardBackImageIcon);
                                    card1Selected = null; 
                                    card2Selected = null;
                                }
                            });
                            hideCardTimer.setRepeats(false); // FALSE SINCE WE ARE GOING TO SELECT ANOTHER CARDS
                            hideCardTimer.start();
                        } else {
                            // REWARD FOR MATCH CARDS
                            score += 10;
                            timeLeft += 3;
                            timerLabel.setText("Time: " + timeLeft + "s");
                            scoreLabel.setText("Score: " + score);
                            coins += 5;
                            coinsLabel.setText("Coins: " + coins);
                            updateShopButtons();

                            if (score > highScore) {
                                highScore = score;
                                saveHighScore();
                            }
                            // MATCH CARDS STAY FACE UP
                            card1Selected = null;
                            card2Selected = null;
                            // WIN CONDITION CHECK
                            boolean allMatched = true;
                            for (JButton btn : board) {
                                if (btn.isEnabled() && btn.getIcon() == cardBackImageIcon) {
                                    allMatched = false;
                                    break;
                                }
                            }

                            if (allMatched) {
                                // WIN RESULT  
                                gameTimer.stop();
                                gameActive = false;

                                if (gameClip != null && gameClip.isRunning()) gameClip.stop();
                                playWinSound();

                                confettiPanel.setVisible(true);
                                startConfetti();

                                // WIN DIALOG 
                                int response = showStyledDialog(
                                    "Victory!",
                                    "CONGRATS! YOU WIN!\n" +
                                    "Score: " + score + "\n" +
                                    "Coins: " + coins + "\n" +
                                    "High Score: " + highScore + "\n" +
                                    "Time left: " + timeLeft + " seconds\n\n" +
                                    "Play Again?",
                                    true
                                );

                                if (confettiTimer != null) confettiTimer.stop();
                                confettiPanel.setVisible(false);

                                if (response == JOptionPane.YES_OPTION) {
                                    if (winClip != null && winClip.isRunning()) winClip.stop();
                                    resetGame();  // START NEW ROUND WITH THE SAME DIFFICULTY 
                                    playMusic("sounds/game.wav", false);
                                } else {
                                    returnToDifficultyScreen();
                                }
                            }
                        }
                    }
                }
            });
            board.add(tile);
            boardPanel.add(tile);
        }

        centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(new Color(30, 30, 60));
        centerWrapper.add(boardPanel);
        frame.add(centerWrapper, BorderLayout.CENTER);

        // BOTTOM PANEL
        bottomPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        bottomPanel.setBackground(new Color(30, 30, 60));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 100, 20, 100));
        bottomPanel.setPreferredSize(new Dimension(boardWidth, 70));

        boosterButton = new JButton("Booster [" + boosterCount + "] (10 coins to buy)");
        boosterButton.setFont(new Font("Arial", Font.BOLD, 18));
        boosterButton.setBackground(new Color(0x15, 0x20, 0x55));
        boosterButton.setForeground(Color.WHITE);
        boosterButton.setFocusPainted(false);
        boosterButton.addActionListener(e -> useOrBuyBooster());
        boosterButton.setEnabled(false);

        tryAgainButton = new JButton("TRY AGAIN");
        tryAgainButton.setFont(new Font("Arial", Font.BOLD, 14));
        tryAgainButton.setBackground(new Color(0xFF, 0xD8, 0x62));
        tryAgainButton.setForeground(Color.BLACK);
        tryAgainButton.setFocusPainted(false);
        tryAgainButton.addActionListener(e -> tryAgain());

        hintButton = new JButton("Hint [" + hintCount + "] (8 coins to buy)");
        hintButton.setFont(new Font("Arial", Font.BOLD, 18));
        hintButton.setBackground(new Color(0x15, 0x20, 0x55));
        hintButton.setForeground(Color.WHITE);
        hintButton.setFocusPainted(false);
        hintButton.addActionListener(e -> useOrBuyHint());
        hintButton.setEnabled(false);

        bottomPanel.add(boosterButton);
        bottomPanel.add(tryAgainButton);
        bottomPanel.add(hintButton);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        confettiPanel.setOpaque(false);
        frame.setGlassPane(confettiPanel);

        frame.pack();
        frame.setVisible(true);
        playMusic("sounds/game.wav", false);
        setGameBackground();
        setFullScreen();

        hideCardTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                hideCards();
            }
        });
        hideCardTimer.setRepeats(false);
        hideCardTimer.start();

        startTimer();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  STYLED DIALOG SYSTEM
    // ══════════════════════════════════════════════════════════════════════════
    int showStyledDialog(String title, String message, boolean isConfirm) {
        Font fontTitle  = minecraftBase.deriveFont(Font.PLAIN, 15f);
        Font fontBody   = minecraftBase.deriveFont(Font.PLAIN, 11f);
        Font fontButton = minecraftBase.deriveFont(Font.PLAIN, 11f);

        JDialog dialog = new JDialog(frame, title, true);
        dialog.setUndecorated(true);
        dialog.setSize(440, isConfirm ? 240 : 210);
        dialog.setLocationRelativeTo(frame);

        final int[] result = { JOptionPane.NO_OPTION };

        // ROOT
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(DARK_NAVY);
        root.setBorder(BorderFactory.createLineBorder(BORDER_BLUE, 2));

        // HEADER
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MEDIUM_NAVY);
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(fontTitle);
        titleLabel.setForeground(GOLD);
        header.add(titleLabel, BorderLayout.CENTER);

        // BODY
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(DARK_NAVY);
        body.setBorder(BorderFactory.createEmptyBorder(16, 24, 8, 24));

        JPanel msgPanel = new JPanel();
        msgPanel.setLayout(new BoxLayout(msgPanel, BoxLayout.Y_AXIS));
        msgPanel.setBackground(DARK_NAVY);

        for (String line : message.split("\n")) {
            JLabel lbl = new JLabel(line.isEmpty() ? " " : line, SwingConstants.CENTER);
            lbl.setFont(fontBody);
            lbl.setForeground(LIGHT_GRAY);
            lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
            msgPanel.add(lbl);
        }
        body.add(msgPanel);

        // FOOTER
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 12));
        footer.setBackground(MEDIUM_NAVY);

        if (isConfirm) {
            JButton yesBtn = styledBtn("YES", fontButton, GOLD, DARK_NAVY);
            JButton noBtn  = styledBtn("NO",  fontButton, BORDER_BLUE, DARK_NAVY);
            yesBtn.addActionListener(e -> { result[0] = JOptionPane.YES_OPTION; dialog.dispose(); });
            noBtn.addActionListener(e  -> { result[0] = JOptionPane.NO_OPTION;  dialog.dispose(); });
            yesBtn.addMouseListener(hoverEffect(yesBtn, GOLD,       new Color(0xFF, 0xC0, 0x30)));
            noBtn.addMouseListener(hoverEffect(noBtn,  LIGHT_GRAY,  new Color(0xB0, 0xB8, 0xC4)));
            footer.add(yesBtn);
            footer.add(noBtn);
        } else {
            JButton okBtn = styledBtn("OK", fontButton, GOLD, DARK_NAVY);
            okBtn.addActionListener(e -> { result[0] = JOptionPane.OK_OPTION; dialog.dispose(); });
            okBtn.addMouseListener(hoverEffect(okBtn, GOLD, new Color(0xFF, 0xC0, 0x30)));
            footer.add(okBtn);
        }

        root.add(header, BorderLayout.NORTH);
        root.add(body,   BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setVisible(true);
        return result[0];
    }

    // ── Button factory ────────────────────────────────────────────────────────
    JButton styledBtn(String text, Font font, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(font);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 28, 8, 28));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Hover factory ─────────────────────────────────────────────────────────
    MouseAdapter hoverEffect(JButton btn, Color normal, Color hover) {
        return new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(normal); }
        };
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  GAME METHODS
    // ══════════════════════════════════════════════════════════════════════════

    void setGameBackground() {
        try {
            ImageIcon bgIcon = new ImageIcon("src/img/bg_game.png");
            Image bgImage = bgIcon.getImage();

            textPanel.setOpaque(false);
            centerWrapper.setOpaque(false);
            bottomPanel.setOpaque(false);
            centerPanel.setOpaque(false);
            rightPanel.setOpaque(false);
            boardPanel.setOpaque(false);

            JPanel bgPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                }
            };
            bgPanel.setLayout(new BorderLayout());
            bgPanel.add(textPanel, BorderLayout.NORTH);
            bgPanel.add(centerWrapper, BorderLayout.CENTER);
            bgPanel.add(bottomPanel, BorderLayout.SOUTH);

            frame.setContentPane(bgPanel);
            frame.revalidate();
            frame.repaint();
        } catch (Exception e) {
            System.out.println("Game background not found");
            frame.getContentPane().setBackground(new Color(30, 30, 60));
        }
    }

    // ── PAUSE ────────────────────────────────────────────────────────────────
    void togglePause() {
        if (gameActive && gameReady) {
            gameActive = false;
            gameReady = false;
            if (gameTimer != null) gameTimer.stop();
            if (hideCardTimer != null) hideCardTimer.stop();
            if (gameClip != null && gameClip.isRunning()) gameClip.stop();

            pauseButton.setText("▶️");

            showStyledDialog("Game Paused", "GAME PAUSED\n\nClick OK to resume.", false);

            gameActive = true;
            gameReady = true;
            if (gameTimer != null) gameTimer.start();
            if (!muted && gameClip != null) {
                gameClip.loop(Clip.LOOP_CONTINUOUSLY);
                gameClip.start();
            }
            pauseButton.setText("⏸️");
        }
    }

    // ── SOUND TOGGLE ─────────────────────────────────────────────────────────
    void toggleSound(JButton soundButton) {
        muted = !muted;
        if (muted) {
            soundButton.setText("🔇");
            if (gameClip != null && gameClip.isRunning()) gameClip.stop();
            if (menuClip != null && menuClip.isRunning()) menuClip.stop();
            if (winClip  != null && winClip.isRunning())  winClip.stop();
            if (loseClip != null && loseClip.isRunning()) loseClip.stop();
        } else {
            soundButton.setText("🔊");
            if (gameClip != null) gameClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    // ── BACK TO MAIN MENU ─────────────────────────────────────────────────────
    void backToMainMenu() {
        int confirm = showStyledDialog(
            "Exit Game",
            "Return to Difficulty Selection?\nYour current progress will be lost.",
            true
        );

        if (confirm == JOptionPane.YES_OPTION) {
            if (gameTimer != null) gameTimer.stop();
            if (hideCardTimer != null) hideCardTimer.stop();

            if (gameClip != null) { gameClip.stop(); gameClip.close(); gameClip = null; }
            if (winClip  != null) { winClip.stop();  winClip.close(); }
            if (loseClip != null) { loseClip.stop(); loseClip.close(); }

            frame.dispose();
            MemeRotMemory.openDifficultyScreen();
        }
    }

    // ── SOUNDS ───────────────────────────────────────────────────────────────
    void playLoseSound() {
        try {
            if (gameClip != null && gameClip.isRunning()) gameClip.stop();
            if (loseClip != null && loseClip.isRunning()) { loseClip.stop(); loseClip.close(); }

            AudioInputStream audio = AudioSystem.getAudioInputStream(new File("sounds/lose.wav"));
            loseClip = AudioSystem.getClip();
            loseClip.open(audio);
            loseClip.setFramePosition(0);
            loseClip.start();
        } catch (Exception e) { e.printStackTrace(); }
    }

    void playWinSound() {
        try {
            if (gameClip != null && gameClip.isRunning()) gameClip.stop();
            if (winClip  != null && winClip.isRunning())  { winClip.stop(); winClip.close(); }

            AudioInputStream audio = AudioSystem.getAudioInputStream(new File("sounds/win.wav"));
            winClip = AudioSystem.getClip();
            winClip.open(audio);
            winClip.setFramePosition(0);
            winClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) { e.printStackTrace(); }
    }

    void playFlipSound() {
        try {
            if (muted) return;
            AudioInputStream audio = AudioSystem.getAudioInputStream(new File("sounds/flip.wav"));
            flipClip = AudioSystem.getClip();
            flipClip.open(audio);
            FloatControl gain = (FloatControl) flipClip.getControl(FloatControl.Type.MASTER_GAIN);
            gain.setValue(-10.0f);
            flipClip.start();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // CONFETTI ANNIMATION CONTROLLER
    void startConfetti() {
        confettiList.clear();
        Color[] colors = {
            Color.RED, Color.YELLOW, new Color(0, 200, 100), Color.CYAN,
            Color.MAGENTA, Color.ORANGE, Color.PINK, new Color(255, 215, 0),
            new Color(130, 80, 255), new Color(255, 100, 180)
        };
        for (int i = 0; i < 250; i++) {
            float x = (float)(Math.random() * frame.getWidth());
            float y = (float)(Math.random() * -800);
            int size = 8 + (int)(Math.random() * 14);
            float speed = 1.5f + (float)(Math.random() * 4);
            int type = (int)(Math.random() * 4);
            Color color = colors[(int)(Math.random() * colors.length)];
            confettiList.add(new Confetti(x, y, size, speed, color, type));
        }
        // ANIMATION LOOP FOR CONFETTI
        confettiTimer = new Timer(30, e -> {
            for (Confetti c : confettiList) {
                c.fall();
                if (c.y > frame.getHeight()) {
                    c.y = -20;
                    c.x = (float)(Math.random() * frame.getWidth());
                    c.size = 8 + (int)(Math.random() * 14);
                    c.speed = 1.5f + (float)(Math.random() * 4);
                    c.type = (int)(Math.random() * 4);
                    c.color = colors[(int)(Math.random() * colors.length)];
                    c.dx = (float)(Math.random() * 2 - 1);
                    c.angleSpeed = (float)(Math.random() * 6 - 3);
                }
            }
            confettiPanel.repaint(); // CALLS SWING TO REDRAW THE PANEL
        });
        confettiTimer.start(); // START TO ANIMATION LOOP
    }

    // ── SOUND SYSTEM ─────────────────────────────────────────────────────────
    void loadSound(String path, boolean loop, boolean isMenuSound) {
        try {
            AudioInputStream audio = AudioSystem.getAudioInputStream(new File(path));
            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            gain.setValue(-15.0f);
            if (loop) clip.loop(Clip.LOOP_CONTINUOUSLY);
            if (isMenuSound) menuClip = clip;
            else gameClip = clip;
        } catch (Exception e) { e.printStackTrace(); }
    }

    void playMusic(String path, boolean isMenu) {
        if (muted) return;
        if (isMenu) {
            if (menuClip != null) { menuClip.stop(); menuClip.close(); menuClip = null; }
            loadSound(path, true, true);
        } else {
            if (gameClip != null) { gameClip.stop(); gameClip.close(); gameClip = null; }
            loadSound(path, true, false);
        }
    }

    void stopMusic(boolean isMenu) {
        if (isMenu) { if (menuClip != null) menuClip.stop(); }
        else        { if (gameClip != null) gameClip.stop(); }
    }

    // ── SETUP CARDS ──────────────────────────────────────────────────────────
    void setupCards() {
        cardSet = new ArrayList<Card>();
        for (String cardName : currentCardList) {
            Image cardImg = new ImageIcon(getClass().getResource("./img/" + cardName + ".jpg")).getImage();
            ImageIcon cardImageIcon = new ImageIcon(cardImg.getScaledInstance(cardWidth, cardHeight, java.awt.Image.SCALE_FAST));
            cardSet.add(new Card(cardName, cardImageIcon));
        }
        cardSet.addAll(new ArrayList<>(cardSet));
        Image cardBackImg = new ImageIcon(getClass().getResource("./img/card_back.jpg")).getImage();
        cardBackImageIcon = new ImageIcon(cardBackImg.getScaledInstance(cardWidth, cardHeight, java.awt.Image.SCALE_FAST));
    }

    // ── SHOP BUTTON STATE ────────────────────────────────────────────────────
    void updateShopButtons() {
        boosterButton.setText(boosterCount > 0
            ? "Booster [" + boosterCount + "] (Click to USE)"
            : "Booster [0] (10 coins to BUY)");

        hintButton.setText(hintCount > 0
            ? "Hint [" + hintCount + "] (Click to USE)"
            : "Hint [0] (8 coins to BUY)");

        boosterButton.setEnabled((boosterCount > 0 || coins >= 10) && gameReady);
        hintButton.setEnabled((hintCount > 0 || coins >= 8) && gameReady);
    }

    // ── BOOSTER ──────────────────────────────────────────────────────────────
    void useOrBuyBooster() {
        if (!gameActive || !gameReady) {
            showStyledDialog("Not Available", "Game is not active right now!", false);
            return;
        }
        if (boosterCount > 0) {
            boosterCount--;
            timeLeft += 10;
            timerLabel.setText("Time: " + timeLeft + "s");
            showStyledDialog("Booster Used", "BOOSTER USED!\n+10 seconds added!", false);
            updateShopButtons();
        } else if (coins >= 10) {
            coins -= 10;
            boosterCount++;
            coinsLabel.setText("Coins: " + coins);
            showStyledDialog("Booster Bought",
                "BOOSTER BOUGHT!\nYou have " + boosterCount + " booster.\nClick again to use!", false);
            updateShopButtons();
        } else {
            showStyledDialog("Not Enough Coins",
                "Need 10 coins to buy\nOR have a booster to use!", false);
        }
    }

    // ── HINT ─────────────────────────────────────────────────────────────────
    void useOrBuyHint() {
        if (!gameActive || !gameReady) {
            showStyledDialog("Not Available", "Game is not active right now!", false);
            return;
        }
        if (hintActive) {
            showStyledDialog("Hint Active", "Hint already active!\nWait a moment.", false);
            return;
        }
        if (hintCount > 0) {
            hintActive = true;
            gameActive = false;
            gameReady = false;
            if (gameTimer != null) gameTimer.stop();

            showStyledDialog("Hint", "HINT USED!\nCards will be shown for 1.5 seconds!\nClick OK to continue.", false);

            hintCount--;
            updateShopButtons();

            ArrayList<Integer> unmatchedIndices = new ArrayList<>();
            for (int i = 0; i < board.size(); i++) {
                if (board.get(i).getIcon() == cardBackImageIcon) {
                    unmatchedIndices.add(i);
                    board.get(i).setIcon(cardSet.get(i).cardImageIcon);
                }
            }

            Timer hintTimer = new Timer(1500, ev -> {
                for (int index : unmatchedIndices) board.get(index).setIcon(cardBackImageIcon);
                gameActive = true;
                gameReady = true;
                if (gameTimer != null) gameTimer.start();
                updateShopButtons();
                hintActive = false;
            });
            hintTimer.setRepeats(false);
            hintTimer.start();

        } else if (coins >= 8) {
            coins -= 8;
            hintCount++;
            coinsLabel.setText("Coins: " + coins);
            showStyledDialog("Hint Bought",
                "HINT BOUGHT!\nYou have " + hintCount + " hint.\nClick again to use!", false);
            updateShopButtons();
        } else {
            showStyledDialog("Not Enough Coins",
                "Need 8 coins to buy\nOR have a hint to use!", false);
        }
    }

    // ── TRY AGAIN ────────────────────────────────────────────────────────────
    void tryAgain() {
        if (loseClip != null && loseClip.isRunning()) loseClip.stop();

        int confirm = showStyledDialog(
            "Try Again",
            "Start a new game?\nYour current progress will be lost.",
            true
        );

        if (confirm == JOptionPane.YES_OPTION) {
            timeLeft = 60; score = 0; coins = 0; boosterCount = 0; hintCount = 0;
            timerLabel.setText("Time: " + timeLeft + "s");
            scoreLabel.setText("Score: " + score);
            coinsLabel.setText("Coins: " + coins);
            updateShopButtons();

            gameActive = true;
            gameReady = false;
            card1Selected = null;
            card2Selected = null;

            if (gameTimer != null) gameTimer.stop();
            if (hideCardTimer != null) hideCardTimer.stop();

            shuffleCards();

            for (int i = 0; i < board.size(); i++) {
                board.get(i).setIcon(cardSet.get(i).cardImageIcon);
                board.get(i).setBorder(null);
                board.get(i).setEnabled(true);
            }

            hideCardTimer = new Timer(1500, e -> {
                for (int i = 0; i < board.size(); i++) board.get(i).setIcon(cardBackImageIcon);
                gameReady = true;
                startTimer();
            });
            hideCardTimer.setRepeats(false);
            hideCardTimer.start();
        }
    }

    // ── SCOREBOARD ───────────────────────────────────────────────────────────
    void createScoreboardDialog() {
        Font fontTitle = minecraftBase.deriveFont(Font.PLAIN, 16f);
        Font fontBody  = minecraftBase.deriveFont(Font.PLAIN, 12f);

        scoreboardDialog = new JDialog(frame, "Scoreboard", false);
        scoreboardDialog.setUndecorated(true);
        scoreboardDialog.setSize(340, 280);
        scoreboardDialog.setLocationRelativeTo(frame);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(DARK_NAVY);
        root.setBorder(BorderFactory.createLineBorder(BORDER_BLUE, 2));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MEDIUM_NAVY);
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        JLabel titleLbl = new JLabel("SCOREBOARD", SwingConstants.CENTER);
        titleLbl.setFont(fontTitle);
        titleLbl.setForeground(GOLD);
        header.add(titleLbl, BorderLayout.CENTER);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(DARK_NAVY);
        body.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel currentScoreLabel = new JLabel("Current Score: 0");
        currentScoreLabel.setFont(fontBody);
        currentScoreLabel.setForeground(LIGHT_GRAY);
        currentScoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        currentScoreLabel.setName("currentScore");

        JLabel currentCoinsLabel = new JLabel("Current Coins: 0");
        currentCoinsLabel.setFont(fontBody);
        currentCoinsLabel.setForeground(GOLD);
        currentCoinsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        currentCoinsLabel.setName("currentCoins");

        highScoreLabel = new JLabel("High Score: 0");
        highScoreLabel.setFont(fontBody);
        highScoreLabel.setForeground(new Color(0x00, 0xE5, 0x76));
        highScoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        highScoreLabel.setName("highScore");

        body.add(currentScoreLabel);
        body.add(Box.createVerticalStrut(14));
        body.add(currentCoinsLabel);
        body.add(Box.createVerticalStrut(14));
        body.add(highScoreLabel);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 12));
        footer.setBackground(MEDIUM_NAVY);
        JButton closeBtn = styledBtn("CLOSE", minecraftBase.deriveFont(Font.PLAIN, 11f), GOLD, DARK_NAVY);
        closeBtn.addActionListener(e -> scoreboardDialog.setVisible(false));
        closeBtn.addMouseListener(hoverEffect(closeBtn, GOLD, new Color(0xFF, 0xC0, 0x30)));
        footer.add(closeBtn);

        root.add(header, BorderLayout.NORTH);
        root.add(body,   BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        scoreboardDialog.setContentPane(root);
    }

    void showScoreboard() {
        for (Component comp : scoreboardDialog.getContentPane().getComponents()) {
            if (comp instanceof JPanel) {
                for (Component c : ((JPanel) comp).getComponents()) {
                    if (c instanceof JLabel) {
                        JLabel label = (JLabel) c;
                        if (label.getName() != null) {
                            if (label.getName().equals("currentScore")) label.setText("Current Score: " + score);
                            if (label.getName().equals("highScore"))    label.setText("High Score: " + highScore);
                        }
                    }
                }
            }
        }
        scoreboardDialog.setVisible(true);
    }

    // ── HIGH SCORE I/O ────────────────────────────────────────────────────────
    void loadHighScore() {
        try {
            java.io.File file = new java.io.File("highscore.txt");
            if (file.exists()) {
                java.util.Scanner scanner = new java.util.Scanner(file);
                highScore = scanner.nextInt();
                scanner.close();
            }
        } catch (Exception e) { highScore = 0; }
    }

    void saveHighScore() {
        try {
            java.io.PrintWriter writer = new java.io.PrintWriter("highscore.txt");
            writer.println(highScore);
            writer.close();
        } catch (Exception e) {}
    }

    // ── TIMER ─────────────────────────────────────────────────────────────────
    void startTimer() {
        gameTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!gameActive) return;
                timeLeft--;
                timerLabel.setText("Time: " + timeLeft + "s");

                if (timeLeft <= 0) {
                    gameTimer.stop();
                    gameActive = false;
                    playLoseSound();

                    // ── GAME OVER DIALOG ──────────────────────────────────
                    int response = showStyledDialog(
                        "Game Over",
                        "TIME'S UP!\n\nWould you like to try again?",
                        true
                    );

                    if (response == JOptionPane.YES_OPTION) {
                        resetGame();
                    } else {
                        returnToDifficultyScreen();
                    }
                }
            }
        });
        gameTimer.start();
    }

    // ── SHUFFLE ───────────────────────────────────────────────────────────────
    void shuffleCards() {
        for (int i = 0; i < cardSet.size(); i++) {
            int j = (int)(Math.random() * cardSet.size());
            Card temp = cardSet.get(i);
            // RANDMOMIZES CARD POSITIONS
            cardSet.set(i, cardSet.get(j));
            cardSet.set(j, temp);
        }
    }

    // ── RESET ─────────────────────────────────────────────────────────────────
    void resetGame() {
        if (loseClip != null && loseClip.isRunning()) loseClip.stop();
        timeLeft = 60; score = 0; coins = 0;
        timerLabel.setText("Time: " + timeLeft + "s");
        scoreLabel.setText("Score: " + score);
        coinsLabel.setText("Coins: " + coins);
        boosterCount = 0; hintCount = 0;
        updateShopButtons();

        gameActive = true;
        gameReady = false;
        shuffleCards();

        for (int i = 0; i < board.size(); i++) {
            board.get(i).setIcon(cardSet.get(i).cardImageIcon);
            board.get(i).setBorder(null);
            board.get(i).setEnabled(true);
        }

        if (hideCardTimer != null) hideCardTimer.stop();
        hideCardTimer = new Timer(1500, e -> {
            for (int i = 0; i < board.size(); i++) board.get(i).setIcon(cardBackImageIcon);
            gameReady = true;
        });
        hideCardTimer.setRepeats(false);
        hideCardTimer.start();

        startTimer();
        playMusic("sounds/game.wav", false);
    }

    // ── HIDE CARDS ────────────────────────────────────────────────────────────
    void hideCards() {
        if (card1Selected != null && card2Selected != null) {
            card1Selected.setIcon(cardBackImageIcon);
            card2Selected.setIcon(cardBackImageIcon);
            card1Selected = null;
            card2Selected = null;
        }
        for (int i = 0; i < board.size(); i++) board.get(i).setIcon(cardBackImageIcon);
        gameReady = true;
    }

    // ── FULL SCREEN ───────────────────────────────────────────────────────────
    void setFullScreen() {
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    // ── RETURN TO MENU ────────────────────────────────────────────────────────
    void returnToDifficultyScreen() {
        if (gameTimer != null)    gameTimer.stop();
        if (hideCardTimer != null) hideCardTimer.stop();
        if (confettiTimer != null) confettiTimer.stop();

        if (gameClip != null) { gameClip.stop(); gameClip.close(); gameClip = null; }
        if (winClip  != null) { winClip.stop();  winClip.close(); }
        if (loseClip != null) { loseClip.stop(); loseClip.close(); }
        if (flipClip != null) { flipClip.stop(); flipClip.close(); }

        frame.dispose();
        MemeRotMemory.openDifficultyScreen();
    }
}