package solver; // Correct package

// Remove: import net.runelite.client.plugins.puzzlesolver.solver.PuzzleState;
import solver.PuzzleState; // Correct import

public interface Heuristic
{
	int computeValue(PuzzleState state);
}