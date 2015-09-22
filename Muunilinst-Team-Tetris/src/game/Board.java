package game;

import java.awt.*;

/**
 * Represents a Tetris board in the form of a 2-d grid of booleans. Supports tetris pieces and row clearing.
 * Has an "undo" feature that allows clients to add and remove pieces efficiently.
 * Does not do any drawing or have any idea of pixels. Instead, just represents the abstract 2-d board.
 * @author Nick Parlante
 */

public final class Board {
    private int width;
    private int height;

    private int[] widths;
    private int[] heights;
    private boolean[][] grid;

    private int[] xwidths;
    private int[] xheights;
    private boolean[][] xgrid;

    private boolean committed;

    /**
     * Creates an empty board of the given width and height  measured in blocks.
     */
    public Board(int aWidth, int aHeight) {
        width = aWidth;
        height = aHeight;

        grid = new boolean[width][height];
        widths = new int[height];
        heights = new int[width];

        xgrid = new boolean[width][height];
        xwidths = new int[height];
        xheights = new int[width];

        committed = true;
    }

    /**
     * Returns the width of the board in blocks.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of the board in blocks.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the max column height present in the board. For an empty board this is 0.
     */
    public int getMaxHeight() {
        int max = 0;
        for (int i = 0; i < width; i++) {
            if (heights[i] > max) max = heights[i];
        }
        return max;
    }

    /**
     * Given a piece and an x, returns the y value where the piece would come to rest
     * if it were dropped straight down at that x.
     * Implementation: use the skirt and the col heights to compute this fast -- O (skirt length).
     */
    public int dropHeight(Piece piece, int x) {
        int high = 10000;
        int result = 0;
        int temp;
        int i;
        int[] skirt = piece.getSkirt();

        for (i = x; i < x + piece.getWidth(); i++) {
            temp = skirt[i - x] - heights[i];
            if (temp < high) {
                high = temp;
                result = i;
            }
        }

        return heights[result] - skirt[result - x];
    }

    /**
     * Returns the height of the given column -- i.e. the y value of the highest block + 1.
     * The height is 0 if the column contains no blocks.
     */
    public int getColumnHeight(int x) {
        return heights[x];
    }

    /**
     * Returns the number of filled blocks in the given row.
     */
    public int getRowWidth(int y) {
        return widths[y];
    }

    /**
     * Returns true if the given block is filled in the board.
     * Blocks outside of the valid width/height area always return true.
     */
    public final boolean getGrid(int x, int y) {
        if (x < 0 || x > width) {
            return true;
        }

        if (y < 0 || y > height) {
            return true;
        }

        return grid[x][y];
    }

    public static final int PLACE_OK = 0;
    public static final int PLACE_ROW_FILLED = 1;
    public static final int PLACE_OUT_BOUNDS = 2;
    public static final int PLACE_BAD = 3;

    /**
     * Attempts to add the body of a piece to the board.
     * Copies the piece blocks into the board grid.
     * Returns PLACE_OK for a regular placement, or PLACE_ROW_FILLED for a regular placement that causes at least one row to be filled.
     *
     * Error cases:
     * If part of the piece would fall out of bounds, the placement does not change the board at all, and PLACE_OUT_BOUNDS is returned.
     * If the placement is "bad" --interfering with existing blocks in the grid --
     * then the placement is halted partially complete and PLACE_BAD is returned.
     * An undo() will remove the bad placement.
     */
    public int place(Piece piece, int x, int y) {
        Point[] body = piece.getBody();
        int putx;
        int puty;
        boolean row_filled = false;

        if (!committed) {
            throw new RuntimeException("Trying to place on uncommitted board!!!");
        }

        committed = false;
        backup();

        // might as well do all the error checking at once, since we must leave the board unchanged if we've got
        // an out of bounds error - this has the added bonus of not altering the board on a bad place
        for (int i = 0; i < body.length; i++) {
            putx = x + body[i].x;
            puty = y + body[i].y;
            if (putx < 0 || putx >= width) {
                return PLACE_OUT_BOUNDS;
            }

            if (puty < 0 || puty >= height) {
                return PLACE_OUT_BOUNDS;
            }

            if (grid[putx][puty]) {
                return PLACE_BAD;
            }
        }

        for (int i = 0; i < body.length; i++) {
            putx = x + body[i].x;
            puty = y + body[i].y;

            grid[putx][puty] = true;
            widths[puty]++;
            if (puty + 1 > heights[putx]) {
                heights[putx] = puty + 1;
            }

            if (widths[puty] >= width) {
                row_filled = true;
            }

        }

        if (row_filled) {
            return PLACE_ROW_FILLED;
        }

        return PLACE_OK;
    }

    /**
     * Deletes rows that are filled all the way across, moving things above down. Returns true if any row clearing happened.
     * Implementation:
     * Ideally, you want to copy each row down to its correct location in one pass.
     * Note that more than one row may be filled.
     */
    public boolean clearRows() {
        boolean cleared = false;

        if (committed) {
            backup(); // not calling immediately after place
        }

        committed = false;

        for (int i = 0; i < height; i++) {
            if (widths[i] >= width) {
                cleared = true;

                for (int j = 0; j < width; j++) {
                    System.arraycopy(grid[j], i + 1, grid[j], i, height - 1 - i);
                    grid[j][height - 1] = false;
                }
                System.arraycopy(widths, i + 1, widths, i, height - 1 - i);
                widths[height - 1] = 0;
                i--;
            }
        }

        if (cleared) {
            recalculateHeights();
        }

        return cleared;
    }

    private void recalculateHeights() {
        int j;
        boolean isSent = false;

        for (int i = 0; i < width; i++) {
            isSent = false;
            for (j = height - 1; j >= 0; j--) {
                if (grid[i][j] && !isSent) {
                    heights[i] = j + 1;
                    isSent = true;
                }
            }

            if (!isSent) {
                heights[i] = 0;
            }
        }
    }

    /**
     * If a place() happens, optionally followed by a clearRows(), a subsequent undo() reverts the board to its state before
     * the place(). If the conditions for undo() are not met, such as calling undo() twice in a row,
     * then the second undo() does nothing.
     */
    public void undo() {
        if (!committed) {
            int[] temp;
            boolean[][] temp2;

            temp = widths;
            widths = xwidths;
            xwidths = temp;

            temp = heights;
            heights = xheights;
            xheights = temp;

            temp2 = grid;
            grid = xgrid;
            xgrid = temp2;

            committed = true;
        }
    }

    private void backup() {
        for (int i = 0; i < width; i++) {
            System.arraycopy(grid[i], 0, xgrid[i], 0, height);
        }

        System.arraycopy(widths, 0, xwidths, 0, height);
        System.arraycopy(heights, 0, xheights, 0, width);
    }

    /**
     * Puts the board in the committed state.
     */
    public void commit() {
        if (!committed) {
            committed = true;
        }
    }
}