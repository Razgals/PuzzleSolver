package solver; // Correct package

import lombok.Getter; // Correct Lombok import - keep this
import lombok.RequiredArgsConstructor; // Correct Lombok import - keep this



@RequiredArgsConstructor // This annotation creates the constructor for the final fields
@Getter // This annotation generates getters for the final fields
public enum PuzzleSwapPattern
{
	// Enum constants pass values for points, modX, and modY, matching the final fields
	// The format is: ENUM_NAME(int[] points, int modX, int modY)
	ROTATE_LEFT_UP(new int[]{1, -1, 0, -1, -1, -1, -1, 0}, 1, 1), //Reference point coordinates relative to empty?
	ROTATE_LEFT_DOWN(null, 1, -1), // Assuming null or an empty array might be used where no specific points array is defined
	ROTATE_RIGHT_UP(null, -1, 1),
	ROTATE_RIGHT_DOWN(null, -1, -1),
	ROTATE_UP_LEFT(new int[]{-1, 1, -1, 0, -1, -1, 0, -1}, 1 , 1), //Reference point coordinates relative to empty?
	ROTATE_UP_RIGHT(null, -1, 1),
	ROTATE_DOWN_LEFT(null, 1, -1),
	ROTATE_DOWN_RIGHT(null, -1, -1),
	LAST_PIECE_ROW(new int[]{-1, -1, 0, -1, -1, 0, -1, 1}, 1, 1), // Coordinates relative to empty?
	LAST_PIECE_COLUMN(new int[]{-1, -1, -1, 0, 0, -1, 1, -1}, 1, 1), // Coordinates relative to empty?
	SHUFFLE_UP_RIGHT(new int[]{1, -1, 0, -1}, 1, 1), // Coordinates relative to empty?
	SHUFFLE_UP_LEFT(new int[]{-1, -1, 0, -1}, 1, 1), // Coordinates relative to empty?
	SHUFFLE_UP_BELOW(new int[]{-1, 1, -1, 0}, 1, 1), // Coordinates relative to empty?
	SHUFFLE_UP_ABOVE(new int[]{-1, -1, -1, 0}, 1, 1); // Coordinates relative to empty?


	/**
	 * Points used for swaps relative to locVal (Meaning might be specific to original context)
	 */
	private final int[] points; // Field to hold the int[] array value passed in the constant

	/**
	 * Modifier for X coordinate (Meaning might be specific to original context)
	 */
	private final int modX; // Field to hold the first int value passed in the constant

	/**
	 * Modifier for Y coordinate (Meaning might be specific to original context)
	 */
	private final int modY; // Field to hold the second int value passed in the constant

	// REMOVED THE EXTRA CONSTRUCTOR: PuzzleSwapPattern(int modX, int modY) { this(null, modX, modY); }
	// The @RequiredArgsConstructor annotation automatically generates a private constructor
	// that takes arguments for all final fields in the order they are declared:
	// private PuzzleSwapPattern(int[] points, int modX, int modY) { ... }
	// This matches how the enum constants are defined (e.g., ROTATE_LEFT_UP(new int[]{...}, 1, 1)).
}
