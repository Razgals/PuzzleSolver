package solver; // Correct package

import solver.PuzzleState; // Correct import for PuzzleState
import static solver.PuzzleSolver.DIMENSION; // Correct static import
import static solver.PuzzleSolver.BLANK_TILE_VALUE; // Correct static import
import solver.Heuristic; // Correct import for Heuristic


/**
 * An implementation of the manhattan distance heuristic function.
 *
 * https://heuristicswiki.wikispaces.com/Manhattan+Distance
 *
 * Revised incremental calculation logic.
 */
public class ManhattanDistance implements Heuristic
{
	@Override
	public int computeValue(PuzzleState state)
	{
		int value = 0;

		PuzzleState parent = state.getParent();

		if (parent == null) // If this is the root state, calculate from scratch
		{
			for (int x = 0; x < DIMENSION; x++)
			{
				for (int y = 0; y < DIMENSION; y++)
				{
					int piece = state.getPiece(x, y); // Get piece value at (x, y)

					if (piece == BLANK_TILE_VALUE) // Skip blank tile
					{
						continue;
					}

					int goalX = piece % DIMENSION; // Correct goal X for piece 'piece' (0-based)
					int goalY = piece / DIMENSION; // Correct goal Y for piece 'piece' (0-based)

					// Add Manhattan distance for this piece to the total
					value += Math.abs(x - goalX) + Math.abs(y - goalY);
				}
			}
		}
		else // Calculate incrementally
		{
			// Start with the parent's heuristic value (which is cached)
			value = parent.getHeuristicValue(this);

			// Coordinates of the blank in the parent state (where the moved piece CAME FROM)
			int movedPieceOriginalX = state.getEmptyPiece() % DIMENSION;
			int movedPieceOriginalY = state.getEmptyPiece() / DIMENSION;

			// Coordinates of the blank in the current state (where the moved piece WENT TO)
			// This is the location of the piece that moved *into* the blank spot.
			int movedPieceCurrentX = parent.getEmptyPiece() % DIMENSION;
			int movedPieceCurrentY = parent.getEmptyPiece() / DIMENSION;


			// The value of the piece that moved is the piece currently at the parent's blank location
			int movedPieceValue = state.getPiece(movedPieceCurrentX, movedPieceCurrentY);

            // The blank tile does not contribute to the Manhattan distance, so we only care
            // about the piece that moved into the blank's previous spot.
            // The piece that moved is the one that was at (movedPieceOriginalX, movedPieceOriginalY)
            // in the parent state and is now at (movedPieceCurrentX, movedPieceCurrentY) in the current state.

            int movedPieceGoalX = movedPieceValue % DIMENSION;
            int movedPieceGoalY = movedPieceValue / DIMENSION;

            // Distance of the moved piece BEFORE the move (at movedPieceOriginalX, movedPieceOriginalY)
            int distanceBefore = Math.abs(movedPieceOriginalX - movedPieceGoalX) + Math.abs(movedPieceOriginalY - movedPieceGoalY);

            // Distance of the moved piece AFTER the move (at movedPieceCurrentX, movedPieceCurrentY)
            int distanceAfter = Math.abs(movedPieceCurrentX - movedPieceGoalX) + Math.abs(movedPieceCurrentY - movedPieceGoalY);

            // The change in heuristic is the difference in distance for the moved piece
            // Add this change to the parent's heuristic value.
            value += (distanceAfter - distanceBefore);

		}

		return value;
	}
}
