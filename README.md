# MEME ROT MEMORY - Card Matching Game

A fun memory card game where you match pairs of meme images. Built with Java Swing. Earn coins, and beat your high score!

## 🎮 Features

- **3 Difficulty Levels**: Easy (3x4), Normal (4x4), Hard (4x5)
- **Coin System**: Earn coins by matching cards
- **Shop**: Buy boosters (+10 seconds) and hints (reveal cards for 1.5 seconds)
- **Sound Effects**: Card flip, win, lose sounds
- **Confetti Animation**: Celebration when you win
- **High Score**: Saves your best score (100)
- **Pause/Resume**: Pause the game anytime
- **Full Screen Mode**: Toggle full screen

## 🕹️ How to Play

1. Click on cards to flip them
2. Match pairs of identical memes
3. Wrong match = -5 seconds
4. Correct match = +10 score, +3 seconds, +5 coins
5. Complete all pairs before time runs out!

## 🛒 Shop Items

|  Item   |   Cost   |             Effect              |
|---------|----------|---------------------------------|
| Booster | 10 coins | +10 seconds                     |
| Hint    | 8 coins  | Shows all cards for 1.5 seconds |

## 🚀 How to Run

```bash
cd newcode.java
javac src/*.java
java src.MemeRotMemory