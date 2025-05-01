package solver; // Correct package

import java.util.ArrayList; // Standard Java import - keep this
import java.util.Arrays; // Standard Java import - keep this
import java.util.List; // Standard Java import - keep this

// Remove: import net.runelite.client.plugins.puzzlesolver.solver.heuristics.Heuristic;
import solver.Heuristic; // Correct import for Heuristic

// Remove: import static net.runelite.client.plugins.puzzlesolver.solver.PuzzleSolver.DIMENSION;
import static solver.PuzzleSolver.DIMENSION; // Correct static import

// Remove: import static net.runelite.client.plugins.puzzlesolver.solver.PuzzleSolver.BLANK_TILE_VALUE;
import static solver.PuzzleSolver.BLANK_TILE_VALUE; // Correct static import


// Add standard Java imports if needed directly in PuzzleState (e.g., Point)
// import java.awt.Point; // If Point is used directly in PuzzleState methods


public class PuzzleState
{
	private PuzzleState parent; // Reference to the parent state in the solution path

	private final int[] pieces; // The puzzle grid represented as a 1D array
	private int emptyPiece = -1; // Index of the empty piece in the 'pieces' array

	private int h = -1; // Cached heuristic value

    // Constructor that takes a 1D int array representing the puzzle state
	public PuzzleState(int[] pieces)
	{
		if (pieces == null)
		{
			throw new IllegalStateException("Pieces cannot be null");
		}

		// Ensure the piece array size matches the expected dimensions
		if (DIMENSION * DIMENSION != pieces.length)
		{
			throw new IllegalStateException("Piece array does not have the right dimensions");
		}

		this.pieces = pieces;
		findEmptyPiece(); // Find the index of the blank tile
	}

    // Private copy constructor - used internally to create new states
	private PuzzleState(PuzzleState state)
	{
		// Create a copy of the pieces array
		this.pieces = Arrays.copyOf(state.pieces, state.pieces.length);
		this.emptyPiece = state.emptyPiece; // Copy the empty piece index
	}

	// Finds the index of the blank tile in the pieces array
	private void findEmptyPiece()
	{
		for (int i = 0; i < pieces.length; i++)
		{
			if (pieces[i] == BLANK_TILE_VALUE)
			{
				this.emptyPiece = i;
				return;
			}
		}
		// Should not happen in a valid puzzle state
		throw new IllegalStateException("Incorrect empty piece passed in!");
	}

	// Computes all possible next states (moves) from the current state
	public List<PuzzleState> computeMoves()
	{
		List<PuzzleState> moves = new ArrayList<>();

		int emptyPieceX = emptyPiece % DIMENSION; // X coordinate of the empty piece
		int emptyPieceY = emptyPiece / DIMENSION; // Y coordinate of the empty piece

		// Check and add move Left (if space exists and it's not moving back to parent)
		if (emptyPieceX > 0) // Can move left
		{
			if (parent == null || parent.emptyPiece != emptyPiece - 1) // Not moving back to parent's empty spot
			{
				PuzzleState state = new PuzzleState(this); // Create a copy of the current state
				state.parent = this; // Set the current state as the parent of the new state

				// Perform the swap: move the piece to the left of empty into the empty spot
				state.pieces[emptyPiece - 1] = BLANK_TILE_VALUE;
				state.pieces[emptyPiece] = pieces[emptyPiece - 1];
				state.emptyPiece--; // Update empty piece index

				moves.add(state); // Add the new state to the list of possible moves
			}
		}

		// Check and add move Right (if space exists and it's not moving back to parent)
		if (emptyPieceX < DIMENSION - 1) // Can move right
		{
			if (parent == null || parent.emptyPiece != emptyPiece + 1) // Not moving back to parent's empty spot
			{
				PuzzleState state = new PuzzleState(this); // Create a copy
				state.parent = this; // Set parent

				// Perform the swap: move the piece to the right of empty into the empty spot
				state.pieces[emptyPiece + 1] = BLANK_TILE_VALUE;
				state.pieces[emptyPiece] = pieces[emptyPiece + 1];
				state.emptyPiece++; // Update empty piece index

				moves.add(state); // Add the new state
			}
		}

		// Check and add move Up (if space exists and it's not moving back to parent)
		if (emptyPieceY > 0) // Can move up
		{
			if (parent == null || parent.emptyPiece != emptyPiece - DIMENSION) // Not moving back to parent's empty spot
			{
				PuzzleState state = new PuzzleState(this); // Create a copy
				state.parent = this; // Set parent

				// Perform the swap: move the piece above empty into the empty spot
				state.pieces[emptyPiece - DIMENSION] = BLANK_TILE_VALUE;
				state.pieces[emptyPiece] = pieces[emptyPiece - DIMENSION];
				state.emptyPiece -= DIMENSION; // Update empty piece index

				moves.add(state); // Add the new state
			}
		}

		// Check and add move Down (if space exists and it's not moving back to parent)
		if (emptyPieceY < DIMENSION - 1) // Can move down
		{
			if (parent == null || parent.emptyPiece != emptyPiece + DIMENSION) // Not moving back to parent's empty spot
			{
				PuzzleState state = new PuzzleState(this); // Create a copy
				state.parent = this; // Set parent

				// Perform the swap: move the piece below empty into the empty spot
				state.pieces[emptyPiece + DIMENSION] = BLANK_TILE_VALUE;
				state.pieces[emptyPiece] = pieces[emptyPiece + DIMENSION];
				state.emptyPiece += DIMENSION; // Update empty piece index

				moves.add(state); // Add the new state
			}
		}

		return moves; // Return the list of all possible next states
	}

	// Gets the parent state in the solution path
	public PuzzleState getParent()
	{
		return parent;
	}

	// Checks if the pieces array of this state matches another pieces array
	public boolean hasPieces(int[] pieces)
	{
		return Arrays.equals(pieces, this.pieces);
	}

    // Added method to get the grid data as a 2D array (used by UI)
    public int[][] getGrid() {
        int[][] grid = new int[DIMENSION][DIMENSION];
        for (int i = 0; i < pieces.length; i++) {
            grid[i / DIMENSION][i % DIMENSION] = pieces[i];
        }
        return grid;
    }

	// Gets the piece value at a specific (x, y) coordinate
	public int getPiece(int x, int y)
	{
		// Convert 2D coordinates to 1D index
		return pieces[y * DIMENSION + x];
	}

	// Gets the index of the empty piece in the 1D pieces array
	public int getEmptyPiece()
	{
		return emptyPiece;
	}

	// Gets the cached heuristic value or computes it if not cached
	public int getHeuristicValue(Heuristic heuristic)
	{
		if (h == -1) // If heuristic value is not cached
		{
			// Compute and cache the value
			h = heuristic.computeValue(this);
		}

		return h; // Return the cached or newly computed value
	}

    // Swaps the piece values at two locations (used internally for creating new states)
	public PuzzleState swap(int x1, int y1, int x2, int y2)
	{
		int val1 = getPiece(x1, y1);
		int val2 = getPiece(x2, y2);

		if (!isValidSwap(x1, y1, x2, y2))
		{
			throw new IllegalStateException(String.format("Invalid swap: (%1$d, %2$d), (%3$d, %4$d)", x1, y1, x2, y2));
		}

		PuzzleState newState = new PuzzleState(this); // Create a copy to perform the swap on

		// Perform the swap in the new state's pieces array
		newState.pieces[y1 * DIMENSION + x1] = val2;
		newState.pieces[y2 * DIMENSION + x2] = val1;
		newState.findEmptyPiece(); // Update empty piece index in the new state

		return newState; // Return the new state after the swap
	}

    // Checks if a swap between two coordinates is valid (must be adjacent and one must be the blank tile)
	private boolean isValidSwap(int x1, int y1, int x2, int y2)
	{
		int absX = Math.abs(x1 - x2);
		int absY = Math.abs(y1 - y2);

		// One of the tiles must be the blank tile
		if (getPiece(x1, y1) != BLANK_TILE_VALUE && getPiece(x2, y2) != BLANK_TILE_VALUE)
		{
			return false;
		}

		// Must be adjacent either horizontally or vertically
		if (x1 == x2 && absY == 1) // Adjacent vertically
		{
			return true;
		}

		return y1 == y2 && absX == 1; // Adjacent horizontally
	}
}