        import java.awt.*; // GRAPHICS, COLOR, LAYOUTS
        import java.awt.event.*; // BUTTON CLICKS AND MOUSE EVENTS
        import java.util.ArrayList; // DYNAMIC ARRAYS FOR CARDS AND CONFETTI
        import javax.swing.*; // GUI COMPONENTS (JFRAME, JBUTTON, JPANEL)
        import javax.sound.sampled.*; // AUDIO PLAYBACK FOR SOUNDS
        import java.io.File; // LOADING SOUND FILES

        public class MatchCards {

            Clip menuClip; // MENU BACKGROUND MUSIC
            Clip gameClip; // GAME BACKGROUND MUSIC
            boolean muted = false; // SOUND ON/OFF TOGGLE
            Clip winClip; // VICTORY SOUND 
            Clip loseClip; // GAME OVER SOUND 
            Clip flipClip; // CARD FLIPPING SOUND

            class Card {
                    String cardName; // CARD NAME 
                    ImageIcon cardImageIcon; // PICTURE OF THE CARD

                    Card(String cardName, ImageIcon cardImageIcon) {
                        this.cardName = cardName;
                        this.cardImageIcon = cardImageIcon;
                }

                public String toString() {
                    return cardName;
                }
            }

            class Confetti {
                int x , y, size, speed; // POSITION AND SPEED OF THE CONFETTI
                Color color; // COLOR OF CONFETTI
                int type;  // SHAPE OF CONFETTI

                Confetti(int x, int y, int size, int speed, Color color, int type) {
                    this.x = x;
                    this.y = y;
                    this.size = size;
                    this.speed = speed;
                    this.color = color;
                    this.type = type;
                }

                void fall() {
                    y += speed;
                }
            }

            // DIFFICULTY CARD LISTS 
            String[] easyCardList = { // EASY MODE (6 PAIRS)
                "mm1", "mm2", "mm3", "mm4", "mm5", "mm6" 
            };

            String[] normalCardList ={ // MEDIUM MODE (8 PAIRS)
                "mm1", "mm2", "mm3", "mm4", "mm5", "mm6", "mm7", "mm8"
            };

            String[] hardCardList = { // HARD MODE (10 PAIRS)
                "mm6", "mm7", "mm8", "mm9", "mm10", "mm11", "mm12", "mm13", "mm14", "mm15"
            };

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
                    g2d.setColor(c.color);
                    
                    // Draw different shapes based on type
                    switch (c.type) {
                        case 0: // RECTANGLE (strip)
                            g2d.fillRect(c.x, c.y, c.size, c.size / 2);
                            break;
                        case 1: // CIRCLE
                            g2d.fillOval(c.x, c.y, c.size, c.size);
                            break;
                        case 2: // DIAMOND (square rotated)
                            int[] xPoints = {
                                c.x + c.size/2, 
                                c.x + c.size, 
                                c.x + c.size/2, 
                                c.x
                            };
                            int[] yPoints = {
                                c.y, 
                                c.y + c.size/2, 
                                c.y + c.size, 
                                c.y + c.size/2
                            };
                            g2d.fillPolygon(xPoints, yPoints, 4);
                            break;
                        case 3: // TRIANGLE
                            int[] triX = {c.x + c.size/2, c.x + c.size, c.x};
                            int[] triY = {c.y, c.y + c.size, c.y + c.size};
                            g2d.fillPolygon(triX, triY, 3);
                            break;
                        default: // RECTANGLE
                            g2d.fillRect(c.x, c.y, c.size, c.size / 2);
                            break;
                    }
                }
            }
        };
            
            // BOARDER
            JPanel centerWrapper;
            JPanel bottomPanel;
            JPanel centerPanel;
            JPanel rightPanel;

            // Menu and Scoreboard variables
            JDialog scoreboardDialog;
            JLabel highScoreLabel;
            int highScore = 0;
            JButton pauseButton;
        
            // Booster and Hint variables
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

            String currentDifficulty ="hard";

            // DEFAULT CONSTRUCTOR # 1
            MatchCards() {
                this("hard");
            }

            // CONSTRUCTOR WITH DIFFICULTY (#2)
            MatchCards(String difficulty) {
                currentDifficulty = difficulty;

                // GRID SIZE BASED ON DIFFICULTY
                switch(difficulty) {
                    case "easy":
                        rows = 3;
                        columns = 4;
                        cardWidth = 220;
                        cardHeight = 270;
                        currentCardList = easyCardList;
                        break;
                    case "normal":
                        rows = 4;
                        columns = 4;
                        cardWidth = 200;
                        cardHeight = 220;
                        currentCardList = normalCardList;
                        break;
                    case "hard":
                        rows = 4;
                        columns = 5;
                        cardWidth = 190;
                        cardHeight = 220;
                        currentCardList = hardCardList;
                        break;
                }

        boardWidth = columns * cardWidth;
        boardHeight = rows * cardHeight; 

            setupCards();
            shuffleCards();
            loadHighScore();

            frame.setLayout(new BorderLayout());
            
            // WEST PANEL 
            JPanel westPanel = new JPanel();
            westPanel.setPreferredSize(new Dimension(50, 0));
            westPanel.setOpaque(false);
            frame.add(westPanel, BorderLayout.WEST);

            // EAST PANEL
            JPanel eastPanel = new JPanel();
            eastPanel.setPreferredSize(new Dimension(50, 0));
            eastPanel.setOpaque(false);
            frame.add(eastPanel, BorderLayout.EAST);


            //frame.add(centerWrapper, BorderLayout.CENTER);
            frame.setSize(boardWidth, boardHeight);
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Scoreboard Dialog 
            createScoreboardDialog();

            // Top panel with timer and score
            textPanel.setLayout(new BorderLayout());
            textPanel.setPreferredSize(new Dimension(boardWidth, 60));
            textPanel.setBackground(new Color(30, 30, 60));

            JPanel infoPanel = new JPanel(new GridLayout(1, 4, 20, 0));
            infoPanel.setBackground(new Color(30, 30, 60));
            infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            
            // Timer and score panel (left side)
            centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 200, 10));
            centerPanel.setBackground(new Color(30, 30, 60));
            
            // TIMER LABEL
            timerLabel.setFont(new Font("Arial", Font.BOLD, 28));
            timerLabel.setText("Time: " + timeLeft + "s");
            timerLabel.setForeground(Color.WHITE);
            timerLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            // DIFFICULTY LABEL
            difficultyLabel.setFont(new Font("Arial", Font.BOLD, 28));
            difficultyLabel.setForeground(Color.CYAN);
            difficultyLabel.setText("Difficulty: " + currentDifficulty.toUpperCase());
            difficultyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            // SCORE LABEL
            scoreLabel.setFont(new Font("Arial", Font.BOLD, 28));
            scoreLabel.setText("Score: 0");
            scoreLabel.setForeground(Color.WHITE);
            scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);

            // COINS LABEL
            coinsLabel.setFont(new Font("Arial", Font.BOLD,28));
            coinsLabel.setForeground(new Color(255, 215, 0));
            coinsLabel.setText("Coins: 0");
            coinsLabel.setHorizontalAlignment(SwingConstants.CENTER);

            // ADD ALL TO CENTER PANEL
            centerPanel.add(timerLabel);
            centerPanel.add(difficultyLabel);
            centerPanel.add(scoreLabel);
            centerPanel.add(coinsLabel);
            
            // RIGHT SIDE PANEL BUTTON
            rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
            rightPanel.setBackground(new Color(30, 30, 60));

            pauseButton = new JButton("⏸️");
            pauseButton.setFont(new Font("Segoe UI Symbol", Font.BOLD, 24));
            pauseButton.setBackground(new Color(70, 70, 100));
            pauseButton.setForeground(Color.WHITE);
            pauseButton.setFocusPainted(false);
            pauseButton.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
            pauseButton.addActionListener(e -> togglePause());

            // SOUND BUTTON
            JButton soundButton = new JButton("🔊");
            soundButton.setFont(new Font("Segoe UI Symbol", Font.BOLD, 24));
            soundButton.setBackground(new Color(70, 70, 100));
            soundButton.setForeground(Color.WHITE);
            soundButton.setFocusPainted(false);
            soundButton.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
            soundButton.addActionListener(e -> toggleSound(soundButton));

            // HOME BUTTON (BACK TO MAIN MENU)
            JButton homeButton = new JButton("🏠");
            homeButton.setFont(new Font("Segoe UI Symbol", Font.BOLD, 24));
            homeButton.setBackground(new Color(70, 70, 100));
            homeButton.setForeground(Color.WHITE);
            homeButton.setFocusPainted(false);
            homeButton.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
            homeButton.addActionListener(e -> backToMainMenu());
            
            // ADD ALL TO RIGHT PANEL
            rightPanel.add(pauseButton);
            rightPanel.add(soundButton);
            rightPanel.add(homeButton);

            textPanel.add(centerPanel, BorderLayout.CENTER);
            textPanel.add(rightPanel, BorderLayout.EAST);
            frame.add(textPanel, BorderLayout.NORTH);

            // MEME ROT MEMORY GAME BOARD
            board = new ArrayList<JButton>();
            boardPanel.setLayout(new GridLayout(rows, columns, 5, 5));
            boardPanel.setOpaque(false); 
            board = new ArrayList<JButton>();
            
            // CARD BUTTON LOOP
            for (int i = 0; i < cardSet.size(); i++) {
                JButton tile = new JButton();
                tile.setPreferredSize(new Dimension(cardWidth, cardHeight));
                tile.setIcon(cardSet.get(i).cardImageIcon);
                tile.setFocusable(false);

                tile.setContentAreaFilled(false);
                tile.setOpaque(false);

                tile.setBorder(BorderFactory.createLineBorder(Color.WHITE, 4));

                // Action cards 
                tile.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (!gameReady) {
                            return;   
                        }
                        JButton tile = (JButton) e.getSource();
                        if (tile.getIcon() != cardBackImageIcon) return;  
                        if (hideCardTimer != null && hideCardTimer.isRunning()) return;
                        
                        if (card1Selected == null) {
                            card1Selected = tile;
                            int index = board.indexOf(card1Selected);
                            card1Selected.setIcon(cardSet.get(index).cardImageIcon);
                            playFlipSound();
                        }
                        else if (card2Selected == null && tile != card1Selected) {
                            card2Selected = tile;
                            int index = board.indexOf(card2Selected);
                            card2Selected.setIcon(cardSet.get(index).cardImageIcon);
                            playFlipSound();

                            if(card1Selected.getIcon() != card2Selected.getIcon()){
                                timeLeft -= 5;
                                timerLabel.setText("Time: " + timeLeft + "s");
                                hideCardTimer = new Timer(1000, new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        card1Selected.setIcon(cardBackImageIcon);
                                        card2Selected.setIcon(cardBackImageIcon);
                                        card1Selected = null;
                                        card2Selected = null;
                                    }
                                });
                                hideCardTimer.setRepeats(false);
                                hideCardTimer.start();
                            } else {
                                // Match found - ADD SCORE +10 AND TIME BONUS +3
                                score += 10;
                                timeLeft += 3;
                                timerLabel.setText("Time: " + timeLeft + "s"); 
                                scoreLabel.setText("Score: " + score);

                                // Earn coins 
                                coins += 5;
                                coinsLabel.setText("Coins:" + coins);
                                updateShopButtons();

                                
                                
                                // Update high score
                                if (score > highScore) {
                                    highScore = score;
                                    saveHighScore();
                                }
                                
                                
                                card1Selected = null;
                                card2Selected = null;

                                boolean allMatched = true;
                                for (JButton btn : board) {
                                    if (btn.isEnabled() && btn.getIcon() == cardBackImageIcon) {
                                        allMatched = false;
                                        break;
                                    }
                                }
                                if (allMatched) {
                                    gameTimer.stop();
                                    gameActive = false;

                                    // STOP GAME SOUND
                                    if (gameClip != null && gameClip.isRunning()){
                                        gameClip.stop();
                                    }

                                    // PLAY WIN SOUND 
                                    playWinSound();
                                    confettiPanel.setVisible(true);
                                    startConfetti();

                                    // START CONFETTI (LOOPING)
                                    confettiPanel.setVisible(true);
                                    startConfetti();

                                    int response = JOptionPane.showConfirmDialog(frame, "CONGRATS! YOU WIN!\nScore: " + score + "\nCoins: " + coins + "\nHigh Score: " + highScore + "\nTime left: " + timeLeft + " seconds\n\nPlay Again?",
                                        "Victory!", JOptionPane.YES_NO_OPTION);

                                        if (confettiTimer != null) {
                                        confettiTimer.stop();
                                    }

                                    confettiPanel.setVisible(false);
                                    
                                    if (response == JOptionPane.YES_OPTION) {

                                        // STOP WIN SOUND
                                        if (winClip != null && winClip.isRunning()) {
                                            winClip.stop();
                                        }
                                        // RESTART GAME 
                                        resetGame();

                                        // RESTART GAME MUSIC
                                        playMusic("sounds/game.wav", false);
                                    } else { 

                                        // GO BACK TO MAIN MENU
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
            centerWrapper.setBackground(new Color(30, 30, 60)); // background color
            centerWrapper.add(boardPanel);

            frame.add(centerWrapper, BorderLayout.CENTER);

            // BOTTOM PANEL (BOOSTER & TRY AGAIN & HINT) BUTTONS
            bottomPanel = new JPanel(new GridLayout(1, 3, 15, 0));
            bottomPanel.setBackground(new Color(30, 30, 60));
            bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 100, 20, 100));
            bottomPanel.setPreferredSize(new Dimension(boardWidth, 70));

            // BOOSTER BUTTON 
            boosterButton = new JButton("Booster  [" + boosterCount + "] (10 coins to buy)");
            boosterButton.setFont(new Font("Arial", Font.BOLD, 18));
            boosterButton.setBackground(new Color(255, 140, 50));
            boosterButton.setForeground(Color.WHITE);
            boosterButton.setFocusPainted(false);
            boosterButton.addActionListener(e -> useOrBuyBooster());  // button for buy and use booster
            boosterButton.setEnabled(false); // start disabled 0 coins 

            // TRY AGAIN BUTTON 
            tryAgainButton = new JButton("TRY AGAIN ");
            tryAgainButton.setFont(new Font("Arial", Font.BOLD, 14));
            tryAgainButton.setBackground(new Color(100, 100, 100));
            tryAgainButton.setForeground(Color.WHITE);
            tryAgainButton.setFocusPainted(false);
            tryAgainButton.addActionListener(e -> tryAgain());

            // HINT BUTTON 
            hintButton = new JButton("Hint [" + hintCount + "] (8 coins to buy)");
            hintButton.setFont(new Font("Arial", Font.BOLD, 18));
            hintButton.setBackground(new Color(80, 120, 255));
            hintButton.setForeground(Color.WHITE);
            hintButton.setFocusPainted(false);
            hintButton.addActionListener(e -> useOrBuyHint()); // button for buy and use hint
            hintButton.setEnabled(false); // start disabled 0 coins 

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

        void setGameBackground() {
            try {
                // Load game background image
                ImageIcon bgIcon = new ImageIcon("src/img/bg_game.png");
                Image bgImage = bgIcon.getImage();
                
                // Make all game panels transparent
                textPanel.setOpaque(false);
                centerWrapper.setOpaque(false);
                bottomPanel.setOpaque(false);
                centerPanel.setOpaque(false);
                rightPanel.setOpaque(false);
                boardPanel.setOpaque(false);
                
                // Create background panel
                JPanel bgPanel = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                    }
                };
                bgPanel.setLayout(new BorderLayout());
                
                // Add your game panels to background
                bgPanel.add(textPanel, BorderLayout.NORTH);
                bgPanel.add(centerWrapper, BorderLayout.CENTER);
                bgPanel.add(bottomPanel, BorderLayout.SOUTH);
                
                // Set as content pane
                frame.setContentPane(bgPanel);
                frame.revalidate();
                frame.repaint();
                
            } catch (Exception e) {
                System.out.println("Game background not found");
                frame.getContentPane().setBackground(new Color(30, 30, 60));
            }
        }

        // TOGGLE PAUSE/RESUME GAME
        void togglePause() {
            if (gameActive && gameReady) {

                // PAUSE GAME
                gameActive = false;
                gameReady = false;
                
                // PAUSE TIMER
                if (gameTimer != null) gameTimer.stop();
                if (hideCardTimer != null) hideCardTimer.stop();

                // PAUSE THE GAME MUSIC
                if (gameClip != null && gameClip.isRunning()) {
                    gameClip.stop();
                }

                pauseButton.setText("▶️");
                JOptionPane.showMessageDialog(frame, "⏸️ GAME PAUSED ⏸️\n\nClick OK to resume.");

                // RESUME GAME AFTER CLICKING OK
                gameActive = true;
                gameReady = true;

                // RESUME TIMER 
                if (gameTimer != null) gameTimer.start();

                // RESUME GAME MUSIC (only if not muted)
                if (!muted && gameClip != null) {
                    gameClip.loop(Clip.LOOP_CONTINUOUSLY);
                    gameClip.start();
                }

                pauseButton.setText("⏸️");
            }
        }

        // TOGGLE SOUND ON/OFF 
        void toggleSound(JButton soundButton) {
            muted = !muted;

            if (muted) {
                soundButton.setText("🔇");
                if (gameClip != null && gameClip.isRunning()) gameClip.stop();
                if (menuClip != null && menuClip.isRunning()) menuClip.stop();
                if (winClip != null && winClip.isRunning()) winClip.stop();
                if (loseClip != null && loseClip.isRunning()) loseClip.stop();
            } else {
                soundButton.setText("🔊");
                if (gameClip != null) gameClip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        }

        void backToMainMenu() {
            int confirm = JOptionPane.showConfirmDialog(frame, "Return to Difficulty Selection?\nYour current progress will be lost.", "Exit Game", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {

                // STOP TIMER FIRST
                if (gameTimer != null) gameTimer.stop();
                if (hideCardTimer != null) hideCardTimer.stop();

                // STOP GAME SOUND
                if (gameClip != null) {
                    gameClip.stop();
                    gameClip.close();
                    gameClip = null;
                }
                
                // STOP WIN/CLOSE SOUNDS
                if (winClip != null) {
                    winClip.stop();
                    winClip.close();
                }
                if (loseClip != null) {
                    loseClip.stop();
                    loseClip.close();
                }

                // CLOSE CURRENT GAME WINDOW
                frame.dispose();

                // OPEN DIFFICULTY SCREEN (will play fresh menu music)
                MemeRotMemory.openDifficultyScreen();
            }
        }

        void playLoseSound() {
            try {

                // STOP GAME MUSIC so lose sound is clear
                if (gameClip != null && gameClip.isRunning()) {
                    gameClip.stop();
                }

                if (loseClip != null && loseClip.isRunning()) {
                    loseClip.stop();
                    loseClip.close();
                }

                AudioInputStream audio =
                    AudioSystem.getAudioInputStream(new File("sounds/lose.wav"));

                loseClip = AudioSystem.getClip();
                loseClip.open(audio);

                loseClip.setFramePosition(0);
                loseClip.start(); // no looping in lose sound

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        void playWinSound() {
            try {

                // STOP GAME MUSICC
                if (gameClip != null && gameClip.isRunning()) {
                    gameClip.stop();
                }

                if (winClip != null && winClip.isRunning()) {
                    winClip.stop();
                    winClip.close();
                }
                
                AudioInputStream audio = AudioSystem.getAudioInputStream(new File("sounds/win.wav")); 
                
                winClip = AudioSystem.getClip();
                winClip.open(audio);

                // START FROM BEGINNING
                winClip.setFramePosition(0);

                // LOOP FOREVER
                winClip.loop(Clip.LOOP_CONTINUOUSLY);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        void playFlipSound () {
            try {

                if (muted) return;

                AudioInputStream audio = 
                AudioSystem.getAudioInputStream(new File("sounds/flip.wav"));

                flipClip = AudioSystem.getClip();
                flipClip.open(audio);

                // ADJUST VOLUME 
                FloatControl gainControl = 
                (FloatControl) flipClip.getControl(FloatControl.Type.MASTER_GAIN);

                gainControl.setValue(-10.0f);

                flipClip.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        void startConfetti() {

            // Clear existing confetti
            confettiList.clear();

            // Create initial confetti
            for (int i = 0; i < 200; i++) {
                int x = (int)(Math.random() * frame.getWidth());
                int y = (int)(Math.random() * -500);
                int size = 5 + (int)(Math.random() * 12);
                int speed = 2 + (int)(Math.random() * 8);
                int type = (int)(Math.random() * 4);

                Color[] colors = {
                    Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN,
                    Color.MAGENTA, Color.ORANGE, Color.PINK, new Color(255, 215, 0)
                };
                Color randomColor = colors[(int)(Math.random() * colors.length)];

                confettiList.add(new Confetti(x, y, size, speed, randomColor, type));
            }

            confettiTimer = new Timer(30, e -> {
                // Update all confetti
                for (int i = 0; i < confettiList.size(); i++) {
                    Confetti c = confettiList.get(i);
                    c.fall();
                    
                    // If confetti falls off screen, respawn at top
                    if (c.y > frame.getHeight()) {
                        c.y = -50;
                        c.x = (int)(Math.random() * frame.getWidth());
                        c.size = 5 + (int)(Math.random() * 12);
                        c.speed = 2 + (int)(Math.random() * 8);
                        c.type = (int)(Math.random() * 4);
                        
                        Color[] colors = {
                            Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN,
                            Color.MAGENTA, Color.ORANGE, Color.PINK, new Color(255, 215, 0)
                        };
                        c.color = colors[(int)(Math.random() * colors.length)];
                    }
                }
                confettiPanel.repaint();
            });
            confettiTimer.start();
        }


        // SOUND SYSTEM
        void loadSound(String path, boolean loop, boolean isMenuSound) {
            try {
                AudioInputStream audio = AudioSystem.getAudioInputStream(new File(path));
                Clip clip = AudioSystem.getClip();
                clip.open(audio);

                // ADJUST VOLUME
                FloatControl gainControl =
                    (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

                gainControl.setValue(-15.0f); // change volume here

                if (loop) {
                    clip.loop(Clip.LOOP_CONTINUOUSLY);
                }

                if (isMenuSound) {
                    menuClip = clip;
                } else {
                    gameClip = clip;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        void playMusic(String path, boolean isMenu) {
            if (muted) return;

            if (isMenu) {
                if (menuClip != null) {
                    menuClip.stop();
                    menuClip.close();
                    menuClip = null;
                }
                loadSound(path, true, true);
            } else {
                if (gameClip != null) {
                    gameClip.stop();
                    gameClip.close();
                    gameClip = null;
                }       
                loadSound(path, true, false);
            }
        }

        void stopMusic(boolean isMenu) {
            if (isMenu) {
                if (menuClip != null) menuClip.stop();
            } else {
                if (gameClip != null) gameClip.stop();
            }
        }

        void setupCards() {
            cardSet = new ArrayList<Card>();
            for (String cardName : currentCardList) {
                Image cardImg = new ImageIcon(getClass().getResource("./img/" + cardName + ".jpg")).getImage();
                ImageIcon cardImageIcon = new ImageIcon(cardImg.getScaledInstance(cardWidth, cardHeight, java.awt.Image.SCALE_FAST));
                Card card = new Card(cardName, cardImageIcon);
                cardSet.add(card);
            }
            cardSet.addAll(cardSet);
            
            Image cardBackImg = new ImageIcon(getClass().getResource("./img/card_back.jpg")).getImage();
            cardBackImageIcon = new ImageIcon(cardBackImg.getScaledInstance(cardWidth, cardHeight, java.awt.Image.SCALE_FAST));
        }


        void updateShopButtons() {
            
            // UPDATE BOOSTERR BUTTON TEXT BASED OWNERSHIP
            if (boosterCount > 0) {
                boosterButton.setText("Booster [" + boosterCount + "] (Click to USE)");
            } else {
                 boosterButton.setText("Booster [0] (10 coins to BUY)");
            }

            // UPDATE HINT BUTTON TEXT BASED ON OWNERSHIP
             if (hintCount > 0) {
                hintButton.setText("Hint [" + hintCount + "] (Click to USE)");
             } else { 
                hintButton.setText("Hint [0] (8 coins to BUY)");
             }

             // ENABLE LOGIC (KEEPS BUTTON GRAYED OUT WHEN NOT USABLE)
            if ((boosterCount > 0 || coins >= 10) && gameReady) {
                boosterButton.setEnabled(true);
            } else {
                boosterButton.setEnabled(false);
            }
 
            if ((hintCount > 0 || coins >= 8) && gameReady) {   
                hintButton.setEnabled(true);
            } else {
                hintButton.setEnabled(false);
            }
        }


        

        // USE OR BUY 
        void useOrBuyBooster() {
            if (!gameActive || !gameReady) {
                JOptionPane.showMessageDialog(frame, "Game is not active right now!"); 
                return;
            }

            if (boosterCount > 0 && gameReady) {
                // USE BOOSTER
                boosterCount--;
                timeLeft += 10;
                timerLabel.setText("Time: " + timeLeft + "s");
                JOptionPane.showMessageDialog(frame,"BOOSTER USED! + 10 seconds!");
                updateShopButtons();
            }
            else if (coins >= 10 && gameReady) {
                // BUY BOOSTER
                coins -= 10;
                boosterCount++;
                coinsLabel.setText("Coins: " + coins);
                JOptionPane.showMessageDialog(frame, "BOOSTER BOUGHT! You have " + boosterCount + " booster. Click again to use!");
                updateShopButtons();
            } 
            else {
                JOptionPane.showMessageDialog(frame, "Need 10 coins to buy OR have a booster to use!");
            }
        }

        // USE OR BUY
        void  useOrBuyHint() {
             if (!gameActive || !gameReady) {
                JOptionPane.showMessageDialog(frame, "Game is not active right now!");
                return;
             }

             if (hintActive) {
                  JOptionPane.showMessageDialog(frame, "Hint already active! Wait a moment.");
                    return;
             }

            if (hintCount > 0 && gameReady) {
                hintActive = true;
                // PAUSE THE GAME FIRST 
                gameActive = false;
                gameReady = false;  
                if (gameTimer != null) gameTimer.stop(); 

                // SHOWING MESSAGE FIRST NOT FLIPPING THE CARDS YET
                JOptionPane.showMessageDialog(frame, "HINT USED! Cards will be shown for 1.5 seconds!\nClick OK to Continue.");

            // USE HINT  AFTER CLICKING "OK" 
            hintCount--;
            updateShopButtons();

            // NOW IT FLIPS ALL UNMATCHED CARDS FACE UP AFTER CLICKING "OK"
            ArrayList<Integer> unmatchedIndices = new ArrayList<>();
            for (int i = 0; i < board.size(); i++) {
                if (board.get(i).getIcon() == cardBackImageIcon) {
                    unmatchedIndices.add(i);
                    board.get(i).setIcon(cardSet.get(i).cardImageIcon);
                }
            }

            // AFTER CLICKING OK FLIP CARDS BACK AFTER 1.5 SECONDS 
            Timer hintTimer = new Timer(1500, ev -> {
                for (int index : unmatchedIndices) {
                    board.get(index).setIcon(cardBackImageIcon);
                }

                // RESUME GAME 
                gameActive = true;
                gameReady = true;
                if (gameTimer !=null) gameTimer.start();
                updateShopButtons();
                hintActive = false;

            });
            hintTimer.setRepeats(false);
            hintTimer.start();

        }
        else if (coins >= 8 && gameReady) {
            // BUY HINT 
            coins -= 8;
            hintCount++;
            coinsLabel.setText("Coins: " + coins);
            JOptionPane.showMessageDialog(frame, "HINT BOUGHT! You have " + hintCount + "hint. Click again to USE!");
            updateShopButtons();
        }
        else {
            JOptionPane.showMessageDialog(frame, "Need 8 coins to buy OR have a hint to use!");
        }
    }

            // TRY AGAIN 
            void tryAgain() {
                if (loseClip != null && loseClip.isRunning()) {
                    loseClip.stop();
            }
                int confirm = JOptionPane.showConfirmDialog(frame, "Start a new game?\nYour current progress will be lost.", "Try Again", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    timeLeft = 60;
                    score = 0; 
                    coins = 0;
                    boosterCount = 0;
                    hintCount = 0;

                    timerLabel.setText("Time: " + timeLeft + "s");
                    scoreLabel.setText("Score: " + score);
                    coinsLabel.setText("Coins: " + coins);

                    updateShopButtons();

                    gameActive = true;
                    gameReady = false;
                    card1Selected = null;       
                    card2Selected = null;

                    if (gameTimer != null) gameTimer.stop();
                    if (hideCardTimer !=null) hideCardTimer.stop();

                    shuffleCards();

                    for (int i = 0; i < board.size(); i++) {
                        board.get(i).setIcon(cardSet.get(i).cardImageIcon);
                        board.get(i).setBorder(null);
                        board.get(i).setEnabled(true);
                    }

                    hideCardTimer = new Timer(1500, e -> {
                        for (int i = 0; i < board.size(); i++) {
                            board.get(i).setIcon(cardBackImageIcon);
                        }
                        gameReady = true;
                        startTimer();
                    });
                    hideCardTimer.setRepeats(false);
                    hideCardTimer.start();
                    
                } else {
                    
                }
            }


            // SCORE BOARD
            void createScoreboardDialog() {
                scoreboardDialog = new JDialog(frame, "Scoreboard", false);
                scoreboardDialog.setSize(300, 250);  // ===== CHANGED: Made taller for coins
                scoreboardDialog.setLocationRelativeTo(frame);
                scoreboardDialog.setLayout(new BorderLayout());
                
                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                panel.setBackground(new Color(50, 50, 80));
                panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                
                JLabel titleLabel = new JLabel("SCOREBOARD");
                titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
                titleLabel.setForeground(Color.YELLOW);
                titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                
                JLabel currentScoreLabel = new JLabel("Current Score: 0");
                currentScoreLabel.setFont(new Font("Arial", Font.PLAIN, 16));
                currentScoreLabel.setForeground(Color.WHITE);
                currentScoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                currentScoreLabel.setName("currentScore");
                
                // ===== ADDED: Coins in scoreboard
                JLabel currentCoinsLabel = new JLabel("Current Coins: 0");
                currentCoinsLabel.setFont(new Font("Arial", Font.PLAIN, 16));
                currentCoinsLabel.setForeground(new Color(255, 215, 0));
                currentCoinsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                currentCoinsLabel.setName("currentCoins");
                
                highScoreLabel = new JLabel("High Score: 0");
                highScoreLabel.setFont(new Font("Arial", Font.PLAIN, 16));
                highScoreLabel.setForeground(Color.GREEN);
                highScoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                highScoreLabel.setName("highScore");
                
                panel.add(titleLabel);
                panel.add(Box.createVerticalStrut(20));
                panel.add(currentScoreLabel);
                panel.add(Box.createVerticalStrut(10));
                panel.add(currentCoinsLabel);  // ===== ADDED: Add coins to scoreboard
                panel.add(Box.createVerticalStrut(10));
                panel.add(highScoreLabel);
                
                scoreboardDialog.add(panel, BorderLayout.CENTER);
            }

        void showScoreboard() {
            // Update scoreboard with current values
            for (Component comp : scoreboardDialog.getContentPane().getComponents()) {
                if (comp instanceof JPanel) {
                    for (Component c : ((JPanel)comp).getComponents()) {
                        if (c instanceof JLabel) {
                            JLabel label = (JLabel) c;
                            if (label.getName() != null) {
                                if (label.getName().equals("currentScore")) {
                                    label.setText("Current Score: " + score);
                                }
                                if (label.getName().equals("highScore")) {
                                    label.setText("High Score: " + highScore);
                                }
                            }
                        }
                    }
                }
            }
            scoreboardDialog.setVisible(true);
        }

        void loadHighScore() {
            try {
                java.io.File file = new java.io.File("highscore.txt");
                if (file.exists()) {
                    java.util.Scanner scanner = new java.util.Scanner(file);
                    highScore = scanner.nextInt();
                    scanner.close();
                }
            } catch (Exception e) {
                highScore = 0;
            }
        }

        void saveHighScore() {
            try {
                java.io.PrintWriter writer = new java.io.PrintWriter("highscore.txt");
                writer.println(highScore);
                writer.close();
            } catch (Exception e) {}
        }

        // TIMER 
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

                    int response = JOptionPane.showConfirmDialog(frame, "⏰ TIME'S UP!\n\nWould you like to try again?","Game Over", JOptionPane.YES_NO_OPTION);
                    if (response == JOptionPane.YES_OPTION) {
                        resetGame();
                    } else { 
                        
                        // GO BACK TO MAIN MENU
                        returnToDifficultyScreen();
                    }
                    
                }               
            }       
        });
            gameTimer.start();
        }

        void shuffleCards() {
                for (int i = 0; i < cardSet.size(); i++) {
                    int j = (int) (Math.random() * cardSet.size());
                    Card temp = cardSet.get(i);
                    cardSet.set(i, cardSet.get(j));
                    cardSet.set(j, temp);
                }
            }

            void resetGame() {
                if (loseClip != null && loseClip.isRunning()) {
                    loseClip.stop();
                }

                timeLeft = 60;
                score = 0;
                coins = 0;
                timerLabel.setText("Time: " + timeLeft + "s");
                scoreLabel.setText("Score: " + score);
                coinsLabel.setText("Coins: " + coins);

                // RESET BOOSTER AND HINT COUNTS 
                boosterCount = 0;
                hintCount = 0;
                updateShopButtons();

                gameActive = true;
                gameReady = false; 

                shuffleCards();

                for (int i = 0; i < board.size(); i++) {
                    board.get(i).setIcon(cardSet.get(i).cardImageIcon);
                    board.get(i).setBorder(null);
                    board.get(i).setEnabled(true);
                }

                if (hideCardTimer != null) {
                    hideCardTimer.stop();
                }

                hideCardTimer = new Timer(1500, e -> {
                    for (int i = 0; i < board.size(); i++) {
                        board.get(i).setIcon(cardBackImageIcon);
                    }
                    gameReady = true;
                });
                hideCardTimer.setRepeats(false);
                hideCardTimer.start();

                startTimer();
                playMusic("sounds/game.wav", false);
            }

            void hideCards() {
                if (card1Selected != null && card2Selected != null) {
                    card1Selected.setIcon(cardBackImageIcon);
                    card2Selected.setIcon(cardBackImageIcon);
                    card1Selected = null;
                    card2Selected = null;   
                }

                for (int i = 0; i < board.size(); i++) {
                    board.get(i).setIcon(cardBackImageIcon);
                }
                gameReady = true;
            }
           // FULL SCREEN METHOD 
           void setFullScreen() {
               frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
           } 

            // RETURN TO MAIN MENU
            void returnToDifficultyScreen() {
                // STOP ALL TIMERS
                if (gameTimer != null) gameTimer.stop();
                if (hideCardTimer != null) hideCardTimer.stop();
                if (confettiTimer != null) confettiTimer.stop();

                // STOP AND CLOSE ALL MUSIC 
                if (gameClip != null) {
                    gameClip.stop();
                    gameClip.close();
                    gameClip = null;
                }
                if (winClip != null) {
                    winClip.stop();
                    winClip.close();
                }
                if (loseClip != null) {
                    loseClip.stop();
                    loseClip.close();
                }
                if (flipClip != null) {
                    flipClip.stop();
                    flipClip.close();
                }

                // CLOSE THE GAME WINDOW 
                frame.dispose();

                // OPEN MAIN MENU
                MemeRotMemory.openDifficultyScreen();
            }
            
        }
