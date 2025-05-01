import javax.swing.*;
import java.awt.*;
import java.awt.event.*; // Import event classes
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;
import java.awt.Point;
// Correct imports for classes in your solver package
import solver.PuzzleState;
import solver.PuzzleSolver;
// Imports needed for creating the solver chain
import solver.Heuristic;
import solver.Pathfinder;
import solver.IDAStar; // Assuming IDAStar is your chosen Pathfinder
import solver.ManhattanDistance; // Assuming ManhattanDistance is your chosen Heuristic

// No package declaration needed if this file is directly in the src directory

public class StandalonePuzzleSolver {
    private static final int PUZZLE_SIZE = 5; // For a 5x5 puzzle
    // Changed the order of puzzle types
    private static final String[] PUZZLE_TYPES = {"castle", "tree", "troll"};
    // Adjusted HIGHLIGHT_COLOR to be less opaque (alpha 80 instead of 128)
    private static final Color HIGHLIGHT_COLOR = new Color(255, 255, 0, 80); // Semi-transparent yellow
    private static final Color TEXT_COLOR = new Color(255, 255, 0); // Bright yellow
    private static final Color BORDER_COLOR = new Color(139, 69, 19); // Brown border
    private static final Color ARRANGE_BORDER_COLOR = new Color(0, 100, 255); // Blue border for arrange mode
    // Define the background color used for styled buttons and controls
    private static final Color CONTROL_BACKGROUND_COLOR = new Color(60, 60, 60); // Slightly different dark gray for control panel

    private String currentPuzzleType;
    // Changed to 1D array to match piece indices 0-24
    private BufferedImage[] puzzlePieces;
    private JFrame frame;
    private JPanel puzzlePanel;
    private int[][] currentState; // The current state displayed on the UI
    private List<PuzzleState> solutionSteps; // List of PuzzleState objects representing the solution
    private int currentStep = 0;
    private Point highlightedTile; // The tile to highlight for the next move
    private Font runescapeFont; // Custom font for the UI

    // UI Components for new features
    private JCheckBox showNumbersCheckbox;
    private JCheckBox arrangeCheckbox;
    private JButton solveButton; // Made solveButton a class member to enable/disable
    private JComboBox<String> puzzleTypeSelector; // Added back the puzzle type selector

    // State variables for new features
    private boolean showNumbers = true; // Whether to show tile numbers
    private boolean arrangeMode = false; // Whether the user is in arrange mode

    // Drag and drop variables
    private Point draggedTileLocation; // Grid coordinates of the tile being dragged
    private Point dragStartMouseLocation; // Mouse coordinates when drag started
    private Point dragCurrentMouseLocation; // Current mouse coordinates during drag

    // Padding for the puzzle grid within the panel (increased for smaller puzzle display)
    private static final int PUZZLE_PADDING = 60; // Increased Padding around the puzzle grid for even smaller tiles
    // Gap size between tiles (increased for more visible gaps)
    private static final int TILE_GAP = 4; // Increased Gap size in pixels

    public StandalonePuzzleSolver() {
        loadCustomFont();
        initializeUI();
        loadPuzzle(PUZZLE_TYPES[0]); // Load the first puzzle type by default
    }

    // Loads a custom TrueType font from a file
    private void loadCustomFont() {
        try {
            // Load the custom RuneScape font - MAKE SURE runescape_uf.ttf IS IN PROJECT ROOT
            File fontFile = new File("runescape_uf.ttf");
            runescapeFont = Font.createFont(Font.TRUETYPE_FONT, fontFile).deriveFont(18f); // Slightly larger font for UI elements

            // Register the font with the graphics environment
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(runescapeFont);
        } catch (IOException | FontFormatException e) {
            System.err.println("Could not load custom font, using default: " + e.getMessage());
            runescapeFont = new Font("Arial", Font.BOLD, 18); // Fallback to Arial, slightly larger
        }
    }

    // Sets up the main Swing UI components
    private void initializeUI() {
        frame = new JFrame("Standalone Puzzle Solver");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 800); // Slightly increased frame size to accommodate padding and bigger buttons
        frame.setLayout(new BorderLayout());

        // Load and set the application icon
        loadAndSetIcon();

        // Puzzle type selector dropdown - ADDED BACK and STYLED
        puzzleTypeSelector = new JComboBox<>(PUZZLE_TYPES);
        puzzleTypeSelector.setFont(runescapeFont); // Set font
        puzzleTypeSelector.setForeground(TEXT_COLOR); // Set foreground color
        puzzleTypeSelector.setBackground(CONTROL_BACKGROUND_COLOR); // Set background color
        puzzleTypeSelector.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 2)); // Add border
        puzzleTypeSelector.addActionListener(e -> {
            // Load the selected puzzle when the dropdown value changes
            loadPuzzle((String) puzzleTypeSelector.getSelectedItem());
        });
        // Ensure the JComboBox's renderer uses the correct colors
         puzzleTypeSelector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setFont(runescapeFont);
                if (isSelected) {
                    label.setBackground(BORDER_COLOR); // Highlight color when selected
                    label.setForeground(Color.WHITE); // White text when selected
                } else {
                    label.setBackground(CONTROL_BACKGROUND_COLOR); // Background color
                    label.setForeground(TEXT_COLOR); // Text color
                }
                return label;
            }
        });


        // Control buttons - STYLED via createStyledButton
        solveButton = createStyledButton("Solve"); // Made class member
        solveButton.addActionListener(e -> solvePuzzle());

        // Checkbox to toggle tile numbers visibility - STYLED
        showNumbersCheckbox = new JCheckBox("Show Tile Numbers", showNumbers);
        showNumbersCheckbox.setFont(runescapeFont); // Set font
        showNumbersCheckbox.setForeground(TEXT_COLOR); // Set foreground color
        showNumbersCheckbox.setBackground(CONTROL_BACKGROUND_COLOR); // Set background color (though opaque is false)
        showNumbersCheckbox.setOpaque(false); // Make background transparent
        showNumbersCheckbox.addActionListener(e -> {
            showNumbers = showNumbersCheckbox.isSelected();
            puzzlePanel.repaint(); // Repaint to show/hide numbers
        });

        // Checkbox for manual arrangement mode - STYLED
        arrangeCheckbox = new JCheckBox("Arrange Manually", arrangeMode);
        arrangeCheckbox.setFont(runescapeFont); // Set font
        arrangeCheckbox.setForeground(TEXT_COLOR); // Set foreground color
        arrangeCheckbox.setBackground(CONTROL_BACKGROUND_COLOR); // Set background color (though opaque is false)
        arrangeCheckbox.setOpaque(false); // Make background transparent
        arrangeCheckbox.addActionListener(e -> toggleArrangeMode());


        // Panel to hold control buttons and dropdown
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10)); // Increased horizontal and vertical gap
        controlPanel.setBackground(CONTROL_BACKGROUND_COLOR); // Use the defined background color
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding around the control panel
        controlPanel.add(puzzleTypeSelector); // ADDED BACK
        controlPanel.add(solveButton);
        controlPanel.add(showNumbersCheckbox);
        controlPanel.add(arrangeCheckbox);


        // Custom JPanel for drawing the puzzle
        puzzlePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g; // Corrected typo here (Graphics2d -> Graphics2D)
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                drawPuzzle(g2d); // Call the drawing method

                // Draw the dragged tile on top if in arrange mode and dragging
                if (arrangeMode && draggedTileLocation != null && dragCurrentMouseLocation != null) {
                    drawDraggedTile(g2d);
                }
            }
        };
        puzzlePanel.setPreferredSize(new Dimension(600, 600)); // Can keep this size, padding will make puzzle smaller
        puzzlePanel.setBackground(new Color(30, 30, 30)); // Very dark gray background
        // Add a border to the puzzle panel
        puzzlePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10), // Add padding around the border
            BorderFactory.createLineBorder(BORDER_COLOR, 5) // Thicker brown border
        ));


        // Add Mouse Listeners for drag and drop in arrange mode and click-to-step in solve mode
        puzzlePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (arrangeMode) {
                    // In arrange mode, start drag if a tile is clicked
                    Point clickLocation = getTileLocationFromMouse(e.getPoint());
                    if (clickLocation != null) {
                        draggedTileLocation = clickLocation;
                        dragStartMouseLocation = e.getPoint();
                        dragCurrentMouseLocation = e.getPoint();
                         // Repaint to start drawing the dragged tile
                        puzzlePanel.repaint();
                    }
                } else {
                    // In solve mode, handle clicks for stepping through the solution
                    if (solutionSteps != null && currentStep < solutionSteps.size() - 1) {
                        Point clickLocation = getTileLocationFromMouse(e.getPoint());
                        // Check if the clicked tile is the highlighted tile
                        if (highlightedTile != null && clickLocation != null && clickLocation.equals(highlightedTile)) {
                            currentStep++; // Move to the next step
                            showCurrentStep(); // Display the new current step
                        }
                    } else if (solutionSteps != null && currentStep == solutionSteps.size() - 1) {
                        // User clicked on the last step, maybe show a message or reset
                        JOptionPane.showMessageDialog(frame, "Puzzle is solved!");
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (arrangeMode && draggedTileLocation != null) {
                    // In arrange mode, end drag and perform swap if valid
                    Point dropLocation = getTileLocationFromMouse(e.getPoint());
                    if (dropLocation != null && !dropLocation.equals(draggedTileLocation)) {
                        // Perform the swap
                        swapTiles(draggedTileLocation, dropLocation);
                        // Clear any previous solution as the arrangement changed
                        solutionSteps = null;
                        currentStep = 0;
                        highlightedTile = null;
                         frame.setTitle("Standalone Puzzle Solver - Arrangement Updated");
                    }

                    // Reset drag state
                    draggedTileLocation = null;
                    dragStartMouseLocation = null;
                    dragCurrentMouseLocation = null;
                    // Repaint to stop drawing the dragged tile and show the swapped state
                    puzzlePanel.repaint();
                }
            }
        });

         // Add MouseMotionListener for tracking drag movement
        puzzlePanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (arrangeMode && draggedTileLocation != null) {
                    // Update current mouse location during drag
                    dragCurrentMouseLocation = e.getPoint();
                    // Repaint to show the dragged tile at the new position
                    puzzlePanel.repaint();
                }
            }
        });


        // Add panels to the frame
        frame.add(controlPanel, BorderLayout.NORTH);
        frame.add(puzzlePanel, BorderLayout.CENTER);

        // Make the frame visible
        frame.setVisible(true);
    }

    // Loads and sets the application window icon
    private void loadAndSetIcon() {
        try {
            // Load the icon image - MAKE SURE app_icon.png IS IN PROJECT ROOT
            File iconFile = new File("app_icon.png");
            if (iconFile.exists()) {
                BufferedImage iconImage = ImageIO.read(iconFile);
                frame.setIconImage(iconImage); // Set the frame's icon
            } else {
                System.err.println("Application icon file not found: app_icon.png");
            }
        } catch (IOException e) {
            System.err.println("Error loading application icon: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // Helper method to get the grid coordinates (col, row) from mouse coordinates
    private Point getTileLocationFromMouse(Point mousePoint) {
        if (currentState == null) return null;

        int panelWidth = puzzlePanel.getWidth();
        int panelHeight = puzzlePanel.getHeight();
        int gap = TILE_GAP; // Use the defined gap size
        int totalGaps = PUZZLE_SIZE - 1;

        // Calculate available drawing area for tiles (excluding padding)
        int drawableWidth = panelWidth - 2 * PUZZLE_PADDING;
        int drawableHeight = panelHeight - 2 * PUZZLE_PADDING;

        // Recalculate piece size based on the drawable area
        int pieceWidth = (drawableWidth - totalGaps * gap) / PUZZLE_SIZE;
        int pieceHeight = (drawableHeight - totalGaps * gap) / PUZZLE_SIZE;

        // Calculate the starting coordinates for drawing the puzzle grid to center it
        int startX = (panelWidth - (PUZZLE_SIZE * pieceWidth + totalGaps * gap)) / 2;
        int startY = (panelHeight - (PUZZLE_SIZE * pieceHeight + totalGaps * gap)) / 2;


        // Adjust mouse coordinates relative to the start of the puzzle grid area
        int adjustedX = mousePoint.x - startX;
        int adjustedY = mousePoint.y - startY;

        // Check if click is within the padded puzzle area
        if (adjustedX < 0 || adjustedY < 0 || adjustedX >= (PUZZLE_SIZE * pieceWidth + totalGaps * gap) || adjustedY >= (PUZZLE_SIZE * pieceHeight + totalGaps * gap)) {
             return null; // Click was outside the padded puzzle area
        }


        int col = adjustedX / (pieceWidth + gap);
        int row = adjustedY / (pieceHeight + gap);

        // Check if the click was within a tile and not in a gap within the calculated tile area
        if (adjustedX % (pieceWidth + gap) > pieceWidth || adjustedY % (pieceHeight + gap) > pieceHeight) {
            return null; // Click was in a gap
        }


        // Check if the calculated coordinates are within the puzzle bounds
        if (col >= 0 && col < PUZZLE_SIZE && row >= 0 && row < PUZZLE_SIZE) { // Corrected typo here (PUzzle_SIZE -> PUZZLE_SIZE)
            return new Point(col, row);
        }

        return null; // Click was outside the puzzle area (should be caught by the padding check, but good safeguard)
    }


    // Toggles between normal puzzle mode and manual arrangement mode
    private void toggleArrangeMode() {
        arrangeMode = arrangeCheckbox.isSelected();
        if (arrangeMode) {
            // In arrange mode, disable solve button and clear solution steps
            solveButton.setEnabled(false);
            solutionSteps = null;
            currentStep = 0;
            highlightedTile = null;
            draggedTileLocation = null; // Ensure drag state is reset
            dragStartMouseLocation = null;
            dragCurrentMouseLocation = null;
            frame.setTitle("Standalone Puzzle Solver - Arrange Mode");
            // Disable puzzle type selection in arrange mode
            puzzleTypeSelector.setEnabled(false);
        } else {
            // Exiting arrange mode, re-enable solve button
            solveButton.setEnabled(true);
             frame.setTitle("Standalone Puzzle Solver"); // Reset title
             // Re-enable puzzle type selection
            puzzleTypeSelector.setEnabled(true);
        }
        puzzlePanel.repaint(); // Repaint to apply arrange mode visual changes
    }


    // Helper method to create styled buttons
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(runescapeFont);
        button.setForeground(TEXT_COLOR);
        button.setBackground(CONTROL_BACKGROUND_COLOR); // Use the defined background color
        button.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 2)); // Brown border
        button.setFocusPainted(false); // Remove focus border
        // Set preferred size for the button to make it bigger
        button.setPreferredSize(new Dimension(120, 35)); // Adjust size as needed
        return button;
    }

    // Loads the puzzle images for the given puzzle type
    private void loadPuzzle(String puzzleType) {
        currentPuzzleType = puzzleType;
        // Change to a 1D array to match piece indices 0-24
        puzzlePieces = new BufferedImage[PUZZLE_SIZE * PUZZLE_SIZE];

        try {
            // Construct the path to the puzzle image directory - MAKE SURE tiles FOLDER IS IN PROJECT ROOT
            File puzzleDir = new File("tiles/" + puzzleType);

            // Loop through piece indices 0 to 24 (for a 5x5 puzzle)
            for (int pieceIndex = 0; pieceIndex < PUZZLE_SIZE * PUZZLE_SIZE; pieceIndex++) {
                // Construct the filename using the 1-based index (pieceIndex + 1), matching tile_X.png
                String filename = "tile_" + (pieceIndex + 1) + ".png";
                File imgFile = new File(puzzleDir, filename);

                // Load the image and store it in the 1D array at the index matching the piece value (0-24)
                // Assumes tile_1.png corresponds to piece value 0, tile_2.png to piece value 1, etc.
                // The blank tile (value 24 for 5x5) should be tile_25.png
                // CORRECTED: Assign the loaded image to the array
                puzzlePieces[pieceIndex] = ImageIO.read(imgFile);
            }

            // Initialize the currentState to the solved state (pieces 0-24 in order)
            currentState = new int[PUZZLE_SIZE][PUZZLE_SIZE];
            for (int i = 0; i < PUZZLE_SIZE * PUZZLE_SIZE; i++) {
                currentState[i / PUZZLE_SIZE][i % PUZZLE_SIZE] = i; // currentState uses 0-24 piece values
            }

            // Reset solution state
            solutionSteps = null;
            currentStep = 0;
            highlightedTile = null;

            // Repaint the puzzle panel to show the loaded puzzle
            puzzlePanel.repaint();
            frame.setTitle("Standalone Puzzle Solver"); // Reset window title
        } catch (Exception e) {
            // Handle errors during loading (e.g., file not found)
            e.printStackTrace(); // Print stack trace for debugging
            JOptionPane.showMessageDialog(frame, "Error loading puzzle pieces: " + e.getMessage());

            // Reset state and clear display on error
            currentState = null; // Ensure currentState is null if loading fails
            puzzlePieces = null; // Ensure puzzlePieces is null if loading fails
            solutionSteps = null;
            currentStep = 0;
            highlightedTile = null;

            // Repaint to show a blank state
            puzzlePanel.repaint();
            frame.setTitle("Standalone Puzzle Solver - Error Loading Puzzle"); // Update title to show error
        }
    }

    // Randomizes the puzzle state (simple shuffle) - REMOVED as per request
    /*
    private void randomizePuzzle() {
        // Simple shuffle - for a real implementation, ensure solvability
        if (currentState == null) {
             JOptionPane.showMessageDialog(frame, "Cannot randomize: Puzzle not loaded.");
             return;
        }

        for (int i = 0; i < 100; i++) { // Perform 100 random swaps
            int direction = (int) (Math.random() * 4); // 0:Up, 1:Right, 2:Down, 3:Left
            Point empty = findEmptyTile(); // Find the location of the empty tile

            switch (direction) {
                case 0: // Up
                    if (empty.y > 0) swapTiles(empty, new Point(empty.x, empty.y - 1));
                    break;
                case 1: // Right
                    if (empty.x < PUZZLE_SIZE - 1) swapTiles(empty, new Point(empty.x + 1, empty.y));
                    break;
                case 2: // Down
                    if (empty.y < PUZZLE_SIZE - 1) swapTiles(empty, new Point(empty.x, empty.y + 1));
                    break;
                case 3: // Left
                    if (empty.x > 0) swapTiles(empty, new Point(empty.x - 1, empty.y));
                    break;
            }
        }

        // Reset solution state after randomizing
        solutionSteps = null;
        currentStep = 0;
        highlightedTile = null;

        // Repaint to show the randomized puzzle
        puzzlePanel.repaint();
         frame.setTitle("Standalone Puzzle Solver - Randomized"); // Update title
    }
    */

    // Finds the current location (row, col) of the empty tile in currentState
    private Point findEmptyTile() {
         if (currentState == null) return null;

        for (int y = 0; y < PUZZLE_SIZE; y++) {
            for (int x = 0; x < PUZZLE_SIZE; x++) {
                // Assuming the empty tile has a value of PUZZLE_SIZE * PUZZLE_SIZE - 1 (e.g., 24 for 5x5)
                if (currentState[y][x] == PUZZLE_SIZE * PUZZLE_SIZE - 1) {
                    return new Point(x, y);
                }
            }
        }
        return null; // Should not happen if the puzzle is valid
    }

    // Swaps two tiles in the currentState array
    private void swapTiles(Point p1, Point p2) {
         if (currentState == null || p1 == null || p2 == null) return;

        int temp = currentState[p1.y][p1.x];
        currentState[p1.y][p1.x] = currentState[p2.y][p2.x];
        currentState[p2.y][p2.x] = temp;
    }


    // Initiates the puzzle solving process using SwingWorker
    private void solvePuzzle() {
         if (currentState == null) {
             JOptionPane.showMessageDialog(frame, "Cannot solve: Puzzle not loaded or arranged.");
             return;
         }
         if (solutionSteps != null) {
              JOptionPane.showMessageDialog(frame, "Puzzle is already solved. Click tiles to step through.");
              return;
         }
         if (arrangeMode) {
             JOptionPane.showMessageDialog(frame, "Please exit arrange mode before solving.");
             return;
         }

        // Disable solve button and other controls while solving
        solveButton.setEnabled(false);
        puzzleTypeSelector.setEnabled(false);
        arrangeCheckbox.setEnabled(false);
        showNumbersCheckbox.setEnabled(false);

        frame.setTitle("Standalone Puzzle Solver - Solving..."); // Update title

        // Create and execute the SwingWorker
        SwingWorker<List<PuzzleState>, Void> solverWorker = new SwingWorker<List<PuzzleState>, Void>() {
            @Override
            protected List<PuzzleState> doInBackground() throws Exception {
                // This code runs in a background thread
                int[] initialPieces = new int[PUZZLE_SIZE * PUZZLE_SIZE];
                for (int i = 0; i < PUZZLE_SIZE * PUZZLE_SIZE; i++) {
                    initialPieces[i] = currentState[i / PUZZLE_SIZE][i % PUZZLE_SIZE];
                }
                PuzzleState initialState = new PuzzleState(initialPieces);

                Heuristic heuristic = new ManhattanDistance();
                Pathfinder pathfinder = new IDAStar(heuristic);
                PuzzleSolver solver = new PuzzleSolver(pathfinder, initialState);

                solver.run(); // Run the solver's logic

                return solver.getSolution(); // Return the computed solution
            }

            @Override
            protected void done() {
                // This code runs back on the Event Dispatch Thread (EDT)
                // Re-enable controls
                solveButton.setEnabled(true);
                puzzleTypeSelector.setEnabled(true);
                arrangeCheckbox.setEnabled(true);
                showNumbersCheckbox.setEnabled(true);

                try {
                    solutionSteps = get(); // Get the result from doInBackground()
                    currentStep = 0;

                    if (solutionSteps == null || solutionSteps.isEmpty()) {
                        JOptionPane.showMessageDialog(frame, "No solution found or solver failed!");
                        frame.setTitle("Standalone Puzzle Solver - No Solution");
                    } else {
                        showCurrentStep(); // Show the first step of the solution
                    }
                } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error during solving: " + e.getMessage());
                    frame.setTitle("Standalone Puzzle Solver - Solver Error");
                    solutionSteps = null; // Ensure solution is null on error
                    currentStep = 0;
                    highlightedTile = null;
                    puzzlePanel.repaint(); // Repaint to clear highlight if any
                }
            }
        };

        solverWorker.execute(); // Start the SwingWorker
    }

    // Displays the puzzle state for the currentStep in the solution
    private void showCurrentStep() {
        if (solutionSteps != null && currentStep >= 0 && currentStep < solutionSteps.size()) {
            // Get the PuzzleState for the current step and convert its internal 1D array to the 2D currentState
            currentState = solutionSteps.get(currentStep).getGrid(); // Now should be found

            // Determine which tile to highlight (the one that needs to move in the next step)
            if (currentStep < solutionSteps.size() - 1) {
                // Find the tile that needs to be clicked in the *current* state to reach the *next* state
                highlightedTile = findTileToClick(
                    solutionSteps.get(currentStep), // Current state (at currentStep)
                    solutionSteps.get(currentStep + 1)); // Next state (at currentStep + 1)
            } else {
                highlightedTile = null; // No tile to highlight on the last step (puzzle is solved)
                frame.setTitle("Standalone Puzzle Solver - Solved!"); // Update title when solved
            }

            // Repaint the puzzle panel to show the updated state and highlight
            puzzlePanel.repaint();

            // Update window title to show current step (if not solved)
            if (highlightedTile != null) {
                 frame.setTitle(String.format("Standalone Puzzle Solver - Step %d/%d",
                    currentStep + 1, solutionSteps.size()));
            }
        }
    }

    // Removed showNextStep and showPreviousStep methods as per request
    /*
    private void showNextStep() {
        if (solutionSteps != null && currentStep < solutionSteps.size() - 1) {
            currentStep++; // Move to the next step
            showCurrentStep(); // Display the new current step
        }
    }

    private void showPreviousStep() {
        if (solutionSteps != null && currentStep > 0) {
            currentStep--; // Move to the previous step
            showCurrentStep(); // Display the new current step
        }
    }
    */

    // Finds the tile that the user should click in the 'current' state to reach the 'next' state.
    // This is the tile that moves *into* the empty square.
    private Point findTileToClick(PuzzleState current, PuzzleState next) {
        int[][] currentGrid = current.getGrid();
        int[][] nextGrid = next.getGrid();

        // Find the location of the empty tile in the *current* state
        Point emptyInCurrent = null;
        for (int y = 0; y < PUZZLE_SIZE; y++) {
            for (int x = 0; x < PUZZLE_SIZE; x++) {
                if (currentGrid[y][x] == PUZZLE_SIZE * PUZZLE_SIZE - 1) {
                    emptyInCurrent = new Point(x, y);
                    break;
                }
            }
            if (emptyInCurrent != null) break;
        }

        if (emptyInCurrent == null) return null; // Should not happen in a valid state

        // Find the tile that is now at the location of the empty tile in the *next* state
        // This tile is the one that moved into the empty spot.
        int valueOfTileThatMoved = nextGrid[emptyInCurrent.y][emptyInCurrent.x];

        // Find the location of this tile (valueOfTileThatMoved) in the *current* state
        Point tileToClickLocation = null;
         for (int y = 0; y < PUZZLE_SIZE; y++) {
            for (int x = 0; x < PUZZLE_SIZE; x++) {
                if (currentGrid[y][x] == valueOfTileThatMoved) {
                    tileToClickLocation = new Point(x, y);
                    break;
                }
            }
            if (tileToClickLocation != null) break;
        }

        return tileToClickLocation; // This is the location of the tile the user should click
    }


    // Draws the current state of the puzzle on the puzzlePanel
    private void drawPuzzle(Graphics2D g) {
        // If currentState is null (e.g., loading failed), draw a blank panel
        if (currentState == null || puzzlePieces == null) {
            g.setColor(new Color(30, 30, 30)); // Match panel background
            g.fillRect(0, 0, puzzlePanel.getWidth(), puzzlePanel.getHeight());
            return; // Stop drawing if state is invalid
        }

        int panelWidth = puzzlePanel.getWidth();
        int panelHeight = puzzlePanel.getHeight();
        int gap = TILE_GAP; // Use the defined gap size
        int totalGaps = PUZZLE_SIZE - 1; // Number of gaps in one dimension

        // Calculate available drawing area for tiles (excluding padding)
        int drawableWidth = panelWidth - 2 * PUZZLE_PADDING;
        int drawableHeight = panelHeight - 2 * PUZZLE_PADDING;

        // Recalculate piece size based on the drawable area
        int pieceWidth = (drawableWidth - totalGaps * gap) / PUZZLE_SIZE;
        int pieceHeight = (drawableHeight - totalGaps * gap) / PUZZLE_SIZE;

        // Calculate the total size needed for the grid including gaps
        int totalGridWidth = PUZZLE_SIZE * pieceWidth + totalGaps * gap;
        int totalGridHeight = PUZZLE_SIZE * pieceHeight + totalGaps * gap;


        // Calculate the starting coordinates for drawing the puzzle grid to center it
        int startX = (panelWidth - totalGridWidth) / 2;
        int startY = (panelHeight - totalGridHeight) / 2;


        // Draw the grid background
        g.setColor(new Color(60, 60, 60)); // Darker gray grid background
        // Draw background for the actual puzzle area, not the whole panel
        g.fillRect(startX, startY, totalGridWidth, totalGridHeight);


        // Draw each puzzle piece
        for (int row = 0; row < PUZZLE_SIZE; row++) {
            for (int col = 0; col < PUZZLE_SIZE; col++) {
                int pieceIndex = currentState[row][col]; // Get the piece value (0-24) at this grid position

                // Calculate drawing coordinates with gaps and padding
                int x = startX + col * (pieceWidth + gap); // X coordinate for drawing this piece
                int y = startY + row * (pieceHeight + gap); // Y coordinate for drawing this piece


                // Skip drawing the empty tile (assuming its value is 24 for 5x5) unless we are in arrange mode
                // In arrange mode, we draw all tiles, including the blank one.
                if (pieceIndex == PUZZLE_SIZE * PUZZLE_SIZE - 1 && !arrangeMode && draggedTileLocation == null) {
                     // Optionally draw a different background for the empty spot in non-arrange mode
                     g.setColor(new Color(40, 40, 40)); // Slightly lighter dark gray for empty spot
                     g.fillRect(x, y, pieceWidth, pieceHeight);
                    continue;
                }

                 // If currently dragging this tile, draw its original position translucent
                 if (arrangeMode && draggedTileLocation != null && draggedTileLocation.equals(new Point(col, row))) {
                     g.setColor(new Color(128, 128, 128, 100)); // Semi-transparent gray
                     g.fillRect(x, y, pieceWidth, pieceHeight);
                     g.setColor(Color.GRAY);
                     g.drawRect(x, y, pieceWidth, pieceHeight);
                     continue; // Skip drawing the actual tile here, it's drawn in drawDraggedTile
                 }


                // Use the pieceIndex directly to get the BufferedImage from the 1D array
                // Assumes tile_1.png corresponds to piece value 0, tile_2.png to piece value 1, etc.
                // The blank tile (value 24 for 5x5) should be tile_25.png
                BufferedImage piece = puzzlePieces[pieceIndex]; // FIX: Use 1D array index

                // Draw the image piece
                g.drawImage(piece, x, y, pieceWidth, pieceHeight, null);

                // Draw tile number if checkbox is selected and not in arrange mode
                if (showNumbers && !arrangeMode) {
                    g.setFont(runescapeFont.deriveFont(14f)); // Slightly smaller font for numbers
                    g.setColor(TEXT_COLOR); // Bright yellow text
                    String num = String.valueOf(pieceIndex + 1); // Display 1-based number
                    FontMetrics fm = g.getFontMetrics();
                    int textX = x + (pieceWidth - fm.stringWidth(num)) / 2; // Center text horizontally
                    int textY = y + ((pieceHeight - fm.getHeight()) / 2) + fm.getAscent(); // Center text vertically
                    g.drawString(num, textX, textY);
                }

                // Draw grid lines/borders
                g.setColor(arrangeMode ? ARRANGE_BORDER_COLOR : BORDER_COLOR); // Blue border in arrange mode
                g.drawRect(x, y, pieceWidth, pieceHeight);
            }
        }

        // Highlight the tile location determined by findTileToClick (the tile to click next)
        if (highlightedTile != null && !arrangeMode) { // Only highlight in normal mode
            // Calculate highlight coordinates with gaps and padding
            int x = startX + highlightedTile.x * (pieceWidth + gap); // X coordinate for the highlight
            int y = startY + highlightedTile.y * (pieceHeight + gap); // Y coordinate for the highlight

            // Draw highlight overlay (less opaque)
            g.setColor(HIGHLIGHT_COLOR);
            g.fillRect(x, y, pieceWidth, pieceHeight);

            // Draw highlight border (yellow border)
            g.setColor(Color.YELLOW);
            g.setStroke(new BasicStroke(3)); // Thicker border
            g.drawRect(x, y, pieceWidth, pieceHeight);

            // Draw instruction text on the highlighted tile
            g.setFont(runescapeFont.deriveFont(16f)); // Standard font size
            g.setColor(Color.WHITE); // White text
            String text = "Click Me"; // Instruction text
            FontMetrics fm = g.getFontMetrics();
            int textX = x + (pieceWidth - fm.stringWidth(text)) / 2; // Center text horizontally
            int textY = y + pieceHeight - 10; // Position text near bottom of tile
            g.drawString(text, textX, textY);
        }
    }

    // Draws the tile currently being dragged in arrange mode
    private void drawDraggedTile(Graphics2D g) {
        if (draggedTileLocation == null || dragStartMouseLocation == null || dragCurrentMouseLocation == null || currentState == null || puzzlePieces == null) {
            return; // Nothing to drag
        }

        int panelWidth = puzzlePanel.getWidth();
        int panelHeight = puzzlePanel.getHeight();
        int gap = TILE_GAP; // Use the defined gap size
        int totalGaps = PUZZLE_SIZE - 1;

        // Calculate available drawing area for tiles (excluding padding)
        int drawableWidth = panelWidth - 2 * PUZZLE_PADDING;
        int drawableHeight = panelHeight - 2 * PUZZLE_PADDING;

        // Recalculate piece size based on the drawable area
        int pieceWidth = (drawableWidth - totalGaps * gap) / PUZZLE_SIZE;
        int pieceHeight = (drawableHeight - totalGaps * gap) / PUZZLE_SIZE;

        // Calculate the starting coordinates for drawing the puzzle grid to center it
        int startX = (panelWidth - (PUZZLE_SIZE * pieceWidth + totalGaps * gap)) / 2;
        int startY = (panelHeight - (PUZZLE_SIZE * pieceHeight + totalGaps * gap)) / 2;


        // Get the value of the tile being dragged
        int pieceIndex = currentState[draggedTileLocation.y][draggedTileLocation.x];
        BufferedImage piece = puzzlePieces[pieceIndex];

        // Calculate the top-left corner for drawing the dragged tile based on current mouse position
        // and the offset from where the drag started within the tile.
        int offsetX = dragCurrentMouseLocation.x - dragStartMouseLocation.x;
        int offsetY = dragCurrentMouseLocation.y - dragStartMouseLocation.y;

        // Original tile position coordinates (with padding and gaps)
        int initialTileX = startX + draggedTileLocation.x * (pieceWidth + gap);
        int initialTileY = startY + draggedTileLocation.y * (pieceHeight + gap);

        int drawX = initialTileX + offsetX;
        int drawY = initialTileY + offsetY;

        // Draw the dragged tile image
        g.drawImage(piece, drawX, drawY, pieceWidth, pieceHeight, null);

        // Optionally draw a translucent version of the tile's original position
        // This is now handled in drawPuzzle by skipping the original tile draw
        // g.setColor(new Color(128, 128, 128, 100)); // Semi-transparent gray
        // g.fillRect(initialTileX, initialTileY, pieceWidth, pieceHeight);
        // g.setColor(Color.GRAY);
        // g.drawRect(initialTileX, initialTileY, pieceWidth, pieceHeight);
    }


    // Main method to start the application
    public static void main(String[] args) {
        // Run the UI creation on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(StandalonePuzzleSolver::new);
    }
}
