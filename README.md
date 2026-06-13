# 🃏 Meme Rot Memory

A meme-themed memory card matching game built with Java Swing. Flip cards, find matching pairs, beat the clock, and rack up your high score!

---

## 📋 Table of Contents

- [About](#about)
- [Features](#features)
- [How to Play](#how-to-play)
- [Difficulty Levels](#difficulty-levels)
- [Scoring System](#scoring-system)
- [Shop Items](#shop-items)
- [Controls](#controls)
- [Project Structure](#project-structure)
- [Requirements](#requirements)
- [How to Run](#how-to-run)

---

## About

Meme Rot Memory is a classic memory card game with a meme twist. Match pairs of meme cards before time runs out. The game features three difficulty levels, a coin-based in-game shop, sound effects, background music, a confetti win animation, and a persistent high score system.

---

## Features

- 🎮 Three difficulty levels: Easy, Normal, Hard
- ⏱️ Countdown timer with dynamic time bonuses and penalties
- 💰 Coin system with an in-game shop
- 🔊 Background music and sound effects (flip, win, lose)
- 🎊 Confetti animation on winning
- 🏆 Persistent high score saved to file
- ⏸️ Pause functionality
- 🔇 Mute/unmute toggle
- 💡 Hint power-up (reveals all cards briefly)
- ⚡ Booster power-up (adds extra time)
- 🖥️ Full-screen support
- 🏠 Return to main menu at any time

---

## How to Play

1. Launch the game — a loading screen will appear.
2. From the main menu, click **PLAY** then choose a difficulty.
3. Cards are briefly shown face-up at the start, then flipped face-down.
4. Click any card to flip it over.
5. Click a second card to try to find a match.
   - **Match found:** Both cards stay revealed, you earn points and coins, and 3 seconds are added to the timer.
   - **No match:** Both cards flip back over and 5 seconds are deducted from the timer.
6. Match all pairs before the timer hits zero to win!

---

## Difficulty Levels

| Difficulty | Grid Size | Cards | Starting Time |
|------------|-----------|-------|---------------|
| Easy       | 3 × 4     | 6 pairs (12 cards) | 60 seconds |
| Normal     | 4 × 4     | 8 pairs (16 cards) | 60 seconds |
| Hard       | 4 × 5     | 10 pairs (20 cards) | 60 seconds |

---

## Scoring System

| Event | Effect |
|-------|--------|
| Correct match | +10 points, +5 coins, +3 seconds |
| Wrong match | -5 seconds |
| Win | Confetti celebration + Play Again prompt |
| Time runs out | Game Over + Try Again prompt |

Your **high score** is automatically saved to `highscore.txt` and persists between sessions.

---

## Shop Items

Coins earned from matches can be spent on power-ups during the game:

| Item | Cost | Effect |
|------|------|--------|
| ⚡ Booster | 10 coins | Adds +10 seconds to the timer |
| 💡 Hint | 8 coins | Reveals all unmatched cards for 1.5 seconds |

If you already own a power-up, click the button again to use it.

---

## Controls

| Button | Action |
|--------|--------|
| ⏸️ Pause | Pause / resume the game |
| 🔊 / 🔇 Sound | Toggle background music and effects |
| 🏠 Home | Return to difficulty selection |
| TRY AGAIN | Restart the current game from scratch |
| Booster button | Buy or use a booster |
| Hint button | Buy or use a hint |

---

## Project Structure

```
MemoryCardGame/
├── src/
│   ├── img/
│   │   ├── mm1.jpg – mm15.jpg   # Meme card images
│   │   ├── card_back.jpg        # Card back image
│   │   ├── bg_game.png          # In-game background
│   │   ├── bgmenu.png           # Loading screen background
│   │   └── landscape.jpg        # Menu background
│   └── fonts/
│       └── Minecraft.otf        # Custom Minecraft font
├── sounds/
│   ├── menu.wav                 # Main menu music
│   ├── game.wav                 # In-game background music
│   ├── flip.wav                 # Card flip sound
│   ├── win.wav                  # Win music
│   └── lose.wav                 # Lose sound
├── App.java                     # Entry point
├── MemeRotMemory.java           # Menu, loading screen, difficulty selection
├── MatchCards.java              # Core game logic and UI
├── highscore.txt                # Auto-generated high score file
└── README.md
```

---

## Requirements

- Java 11 or higher
- No external libraries required (uses Java Swing and `javax.sound`)

---

## How to Run

**Compile:**
```bash
javac App.java MemeRotMemory.java MatchCards.java
```

**Run:**
```bash
java App
```

> Make sure the `src/` and `sounds/` folders are in the same directory as your compiled `.class` files, or adjust the paths in the source code accordingly.

---

## Credits

Developed by **aveloso11**. Built with Java Swing.
