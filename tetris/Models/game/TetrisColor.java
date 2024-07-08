
package tetris.Models.game;

import javafx.scene.paint.Color;

/**
 * Represents the 7 different Tetris colors
 */
public enum TetrisColor {
	
	EMPTY,
	YELLOW,		// O
	LBLUE,		// I
	PURPLE,		// T
	ORANGE,		// L
	BLUE,		// J
	GREEN,		// S
	RED;		// Z

    public Color toColor() {
        switch (this) {
            case EMPTY	: return Color.BLACK;
            case YELLOW	: return Color.YELLOW;
            case LBLUE	: return Color.LIGHTBLUE;
            case PURPLE	: return Color.PURPLE;
            case ORANGE	: return Color.ORANGE;
            case BLUE	: return Color.BLUE;
            case GREEN	: return Color.GREEN;
            case RED	: return Color.RED;
        }
		return null;
    }
}
