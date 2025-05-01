package solver; // Correct package

import java.util.ArrayList;
import java.util.List;

// Remove: import net.runelite.client.plugins.puzzlesolver.solver.PuzzleState;
import solver.PuzzleState; // Correct import

// Remove: import net.runelite.client.plugins.puzzlesolver.solver.heuristics.Heuristic;
import solver.Heuristic; // Correct import

// Assuming Pathfinder is in the same package, no import needed

/**
 * An implementation of the IDA* algorithm.
 *
 * https://en.wikipedia.org/wiki/Iterative_deepening_A*
 */
public class IDAStar extends Pathfinder
{
	public IDAStar(Heuristic heuristic)
	{
		super(heuristic);
	}

	@Override
	public List<PuzzleState> computePath(PuzzleState root)
	{
		PuzzleState goalNode = path(root);

		List<PuzzleState> path = new ArrayList<>();

		PuzzleState parent = goalNode;
		while (parent != null)
		{
			path.add(0, parent);
			parent = parent.getParent();
		}

		return path;
	}

	private PuzzleState path(PuzzleState root)
	{
		int bound = root.getHeuristicValue(getHeuristic());

		while (true)
		{
			PuzzleState t = search(root, 0, bound);

			if (t != null)
			{
				return t;
			}

			bound += 1; // Increase the bound if no solution found within current bound
		}
	}

	private PuzzleState search(PuzzleState node, int g, int bound)
	{
		int h = node.getHeuristicValue(getHeuristic());
		int f = g + h; // f = cost to reach node (g) + estimated cost from node to goal (h)

		if (f > bound)
		{
			return null; // Prune this branch if f exceeds the current bound
		}

		if (h == 0)
		{
			return node; // Goal reached (heuristic value is 0)
		}

		// Explore neighbors
		for (PuzzleState successor : node.computeMoves())
		{
			PuzzleState t = search(successor, g + 1, bound); // Recursively search successor

			if (t != null)
			{
				return t; // Solution found in a recursive call
			}
		}

		return null; // No solution found in this branch within the current bound
	}
}