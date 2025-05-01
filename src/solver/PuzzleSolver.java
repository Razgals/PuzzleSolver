package solver; // Correct package

import com.google.common.base.Stopwatch; // Correct import for Guava - keep this
import java.time.Duration; // Standard Java import - keep this
import java.util.List; // Standard Java import - keep this

// Remove: import net.runelite.client.plugins.puzzlesolver.solver.pathfinding.Pathfinder;
import solver.Pathfinder; // Correct import for Pathfinder within your project

// Remove: import net.runelite.client.plugins.puzzlesolver.solver.PuzzleState;
import solver.PuzzleState; // Correct import for PuzzleState within your project

public class PuzzleSolver implements Runnable
{
	public static final int DIMENSION = 5; // Dimension of the puzzle grid
	public static final int BLANK_TILE_VALUE = DIMENSION * DIMENSION - 1; // Value of the blank tile (24 for 5x5)

	private static final Duration MAX_WAIT_DURATION = Duration.ofMillis(1500); // Max duration for solving attempt

	private final Pathfinder pathfinder; // The pathfinding algorithm to use
	private final PuzzleState startState; // The initial state to solve from

	private List<PuzzleState> solution; // Will hold the computed sequence of states forming the solution
	private int position; // Used by some external logic perhaps? (Not used in 'run')
	private Stopwatch stopwatch; // To measure solving time
	private boolean failed = false; // Flag indicating if solving failed

	// Constructor requiring a Pathfinder and the starting PuzzleState
	public PuzzleSolver(Pathfinder pathfinder, PuzzleState startState)
	{
		this.pathfinder = pathfinder;
		this.startState = startState;
	}

    // Added public getter for the computed solution steps
    public List<PuzzleState> getSolution() {
        return solution;
    }

	// Gets a specific step from the solution sequence
	public PuzzleState getStep(int stepIdx)
	{
		if (solution == null || stepIdx < 0 || stepIdx >= solution.size()) {
            return null; // Return null if solution is not available or index is out of bounds
        }
        return solution.get(stepIdx);
	}

	// Gets the total number of steps in the solution
	public int getStepCount()
	{
        if (solution == null) return 0;
		return solution.size();
	}

	// Checks if a solution was found
	public boolean hasSolution()
	{
		return solution != null && !solution.isEmpty();
	}

	// Getter for the position field (Purpose unclear from provided code snippets)
	public int getPosition()
	{
		return position;
	}

	// Setter for the position field (Purpose unclear from provided code snippets)
	public void setPosition(int position)
	{
		this.position = position;
	}

	// Checks if the solving time has exceeded the maximum allowed duration
	public boolean hasExceededWaitDuration()
	{
		return stopwatch != null && stopwatch.elapsed().compareTo(MAX_WAIT_DURATION) > 0;
	}

	// Checks if the solving process failed
	public boolean hasFailed()
	{
		return failed;
	}

	@Override
	public void run()
	{
        // Added try-catch to handle potential exceptions during the pathfinding process
        try {
            stopwatch = Stopwatch.createStarted(); // Start the timer
            solution = pathfinder.computePath(startState); // Compute the solution path
            failed = solution == null; // Mark as failed if computePath returns null
        } catch (Exception e) {
            e.printStackTrace(); // Print stack trace for debugging if an exception occurs
            failed = true; // Mark as failed if an exception occurs
            solution = null; // Ensure solution is null on failure
        } finally {
             if (stopwatch != null) {
                stopwatch.stop(); // Stop the stopwatch regardless of success or failure
            }
        }
	}
}