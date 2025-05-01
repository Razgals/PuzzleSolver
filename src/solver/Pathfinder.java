package solver; // Correct package

import java.util.List; // Standard Java import - keep this

// Remove: import net.runelite.client.plugins.puzzlesolver.solver.PuzzleState;
import solver.PuzzleState; // Correct import for PuzzleState within your project

// Remove: import net.runelite.client.plugins.puzzlesolver.solver.heuristics.Heuristic;
import solver.Heuristic; // Correct import for Heuristic within your project

public abstract class Pathfinder
{
	private final Heuristic heuristic;

	Pathfinder(Heuristic heuristic)
	{
		this.heuristic = heuristic;
	}

	Heuristic getHeuristic()
	{
		return heuristic;
	}

	// Abstract method to be implemented by concrete pathfinding algorithms (like IDAStar)
	public abstract List<PuzzleState> computePath(PuzzleState start);
}