package org.simpledrive.sodokusolver;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class DrawCanvas extends View implements View.OnTouchListener
{
    private boolean isInitialized = false;
    private int gridSize = 9;
    private Pixel[][] fields = new Pixel[gridSize][gridSize];
    private Pixel[][] tempFields =new Pixel[gridSize][gridSize];
    private Pixel[][] solution = new Pixel[gridSize][gridSize];
    private int pixelSize;
    private int top;
    private int selX = -1;
    private int selY = -1;
    private int currentNumber = 0;
    private Context ctx;
    private boolean clearPending = false; // Need to double tap clear to prevent accidental clearing
    private boolean solved = false; // To prevent multiple solvings of the same grid
    private boolean showAll = false; // Set true when asked to solve all
    public boolean hintMode = false; // When true, user can fill an empty cell from solution
    public int lightGrey = Color.parseColor("#eeeeee");

    public DrawCanvas(Context context)
    {
        super(context);
        this.setOnTouchListener(this);
        ctx = context.getApplicationContext();
    }

    private void init()
    {
        pixelSize = getWidth() / gridSize;
        top = (int) Math.floor((getHeight() - gridSize * pixelSize) / 1.3);

        for(int i = 0; i < gridSize; i++)
        {
            for(int j = 0; j < gridSize; j++)
            {
                fields[i][j] = new Pixel(0, false);
                tempFields[i][j] = new Pixel(0, false);
                solution[i][j] = new Pixel(0, false);
            }
        }
        isInitialized = true;
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        if(!isInitialized)
        {
            init();
        }

        // Draw numbers-row
        Paint pNumber = new Paint();
        pNumber.setTextAlign(Paint.Align.CENTER);

        int numbersTop = (int) Math.floor((top - pixelSize) / 1.5);
        for(int n = 0; n < 10; n++)
        {
            Paint.Style style = (currentNumber == n + 1) ? Paint.Style.FILL : Paint.Style.STROKE;
            pNumber.setStyle(style);
            int color = (currentNumber == n + 1) ? lightGrey : Color.LTGRAY;
            pNumber.setColor(color);
            canvas.drawRect(n * pixelSize, numbersTop, n * pixelSize + pixelSize, numbersTop + pixelSize, pNumber);
        }

        Paint pText = new Paint();
        pText.setStyle(Paint.Style.FILL);
        pText.setColor(Color.BLACK);
        pText.setTextSize(36);
        pText.setTextAlign(Paint.Align.CENTER);
        for(int m = 0; m < 10; m++)
        {
            canvas.drawText("" + (m + 1), m * pixelSize + pixelSize / 2, numbersTop + pixelSize / 2, pText);
        }

        // Draw big grid
        for(int i = 0; i < 3; i++)
        {
            for(int j = 0; j < 3; j++)
            {
                Paint paint = new Paint();
                paint.setColor(Color.LTGRAY);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(3);

                int left = i * pixelSize * 3;
                int thistop = j * pixelSize * 3 + top;
                int right = left + pixelSize * 3;
                int bottom = thistop + pixelSize * 3;

                canvas.drawRect(left, thistop, right, bottom, paint);
            }
        }

        // Draw grid
        Paint paint2 = new Paint();
        paint2.setTextSize(36);
        paint2.setTextAlign(Paint.Align.CENTER);
        for(int i = 0; i < fields.length; i++)
        {
            for(int j = 0; j < fields[i].length; j++)
            {
                int topLeftX = i * pixelSize;
                int topLeftY = j * pixelSize + top;
                int bottomRightX = i * pixelSize + pixelSize;
                int bottomRightY = j * pixelSize + pixelSize + top;

                if ((i == selX && j == selY) || (showAll && fields[i][j].userset))
                {
                    paint2.setStyle(Paint.Style.FILL);
                    paint2.setColor(lightGrey);
                    canvas.drawRect(topLeftX, topLeftY, bottomRightX, bottomRightY, paint2);

                }

                paint2.setStyle(Paint.Style.STROKE);
                paint2.setColor(Color.LTGRAY);
                canvas.drawRect(topLeftX, topLeftY, bottomRightX, bottomRightY, paint2);

                if(fields[i][j].value != 0)
                {
                    paint2.setColor(Color.BLACK);
                    canvas.drawText(fields[i][j].value + "", topLeftX + pixelSize / 2, topLeftY + pixelSize / 2, paint2);
                }
            }
        }

        // Draw title
        Paint pTitle = new Paint();
        pTitle.setStyle(Paint.Style.STROKE);
        pTitle.setColor(Color.GRAY);
        pTitle.setTextSize(64);
        float width = pTitle.measureText("Sudoku Solver");
        int titleTop = (((top - pixelSize) / 2) - 64) / 2 + 64;
        canvas.drawText("Sudoku Solver", (getWidth() - width) / 2, titleTop, pTitle);
    }

    public void clear()
    {
        if(clearPending)
        {
            solved = false;
            clearPending = false;
            init();
            invalidate();
        }
        else
        {
            Toast.makeText(ctx, "Click again to clear", Toast.LENGTH_SHORT).show();
            clearPending = true;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        clearPending = false;
        solved = (!hintMode) ? false : solved; // If not in hint-mode, grid is altered, so solve() needs to be called again

        // Touched numbers row
        if(event.getY() < top)
        {
            int number = (int) event.getX() / pixelSize + 1;
            currentNumber = (number == currentNumber) ? 0 : number;
        }
        // Touched grid
        else if(event.getY() < top + gridSize * pixelSize)
        {
            selX = (int) event.getX() / pixelSize;
            selY = (int) (event.getY() - top) / pixelSize;

            if(hintMode && fields[selX][selY].value == 0)
            {
                fields[selX][selY].userset = true;
                fields[selX][selY].value = solution[selX][selY].value;
                solution[selX][selY].userset = true; // So it's filled grey on "solve all"
            }
            else if(hintMode)
            {
                Toast.makeText(ctx, "Already set!", Toast.LENGTH_SHORT).show();
            }
            else if(currentNumber != 0 && isInColl(selX, selY, currentNumber))
            {
                Toast.makeText(ctx, currentNumber + " is already in the column!", Toast.LENGTH_SHORT).show();
            }
            else if(currentNumber != 0 && isInRow(selX, selY, currentNumber))
            {
                Toast.makeText(ctx, currentNumber + " is already in the row!", Toast.LENGTH_SHORT).show();
            }
            else if(currentNumber != 0 && isInBox(selX, selY, currentNumber))
            {
                Toast.makeText(ctx, currentNumber + " is already in the box!", Toast.LENGTH_SHORT).show();
            }
            else
            {
                fields[selX][selY].userset = (currentNumber != 0);
                fields[selX][selY].value = currentNumber;
            }
        }

        invalidate();
        return false;
    }

    /**
     * Solves the puzzle to reveal one number at a time
     */

    public void solveOneHandler()
    {
        // Backup current state
        copyArray(fields, tempFields);

        if(solve())
        {
            // Write solution
            copyArray(fields, solution);

            // Restore original state
            copyArray(tempFields, fields);

            hintMode = true;
            Toast.makeText(ctx, "Click on the field you want to be solved", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(ctx, "No solution found", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean copyArray(Pixel[][] src, Pixel[][] dest)
    {
        for(int i = 0; i < dest.length; i++)
        {
            for(int j = 0; j < dest.length; j++)
            {
                dest[i][j].value = src[i][j].value;
                dest[i][j].userset = src[i][j].userset;
            }
        }
        return true;
    }

    /**
     * Tries to solve the puzzle if it hasn't already solved (e.g. for a hint)
     */

    public void solveAllHandler()
    {
        // If there is a current solution, use it!
        boolean solved = (this.solved) ? copyArray(solution, fields) : solve();
        if(solved)
        {
            showAll = true;
            invalidate();
        }
        else
        {
            Toast.makeText(ctx, "No solution found", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean solve()
    {
        int direction = 1;
        int coll = 0;
        int row = 0;

        while(!solved)
        {
            // If not set by user
            if(!fields[coll][row].userset && fields[coll][row].value < 9)
            {
                fields[coll][row].value++;
                if(!collision(coll, row, fields[coll][row].value))
                {
                    // Reached last cell
                    if (coll == 8 && row == 8)
                    {
                        solved = true;
                    }

                    // Go to next cell
                    row = (coll == 8 && row < 8) ? row + 1 : row;
                    coll = (coll == 8) ? 0 : coll + 1;
                    direction = 1;
                }
                else {
                    if (fields[coll][row].value == 9){
                        if(coll == 8 && row == 8) {
                            return false;
                        }
                        // Reset and go to previous cell
                        fields[coll][row].value = 0;
                        row = (coll == 0 && row > 0) ? row - 1 : row;
                        coll = (coll == 0) ? 8 : coll - 1;
                        direction = -1;
                    }
                }
            }
            else {
                if (direction == 1) {
                    // Go to next cell
                    row = (coll == 8 && row < 8) ? row + 1 : row;
                    coll = (coll == 8) ? 0 : coll + 1;
                }
                else {
                    // Reset and go to previous cell
                    row = (coll == 0 && row > 0) ? row - 1 : row;
                    coll = (coll == 0) ? 8 : coll - 1;
                }
            }
        }
        return true;
    }

    public boolean collision(int coll, int row, int value)
    {
        return isInColl(coll, row, value) || isInRow(coll, row, value) || isInBox(coll, row, value);
    }

    public boolean isInColl(int c, int r, int value)
    {
        for (int i = 0; i < gridSize; i++)
        {
            if (i != r && fields[c][i].value == value)
            {
                return true;
            }
        }
        return false;
    }

    public boolean isInRow(int c, int r, int value)
    {
        for (int j = 0; j < gridSize; j++)
        {
            if (j != c && fields[j][r].value == value)
            {
                return true;
            }
        }
        return false;
    }

    public boolean isInBox(int c, int r, int value)
    {
        // Get top left of box
        int column = (c / 3) * 3;
        int row = (r / 3) * 3;

        for(int i = column; i < column + 3; i++)
        {
            for(int j = row; j < row + 3; j++)
            {
                if(i != c && j != r && fields[i][j].value == value)
                {
                    return true;
                }
            }
        }
        return false;
    }

    class Pixel
    {
        public int value;
        public boolean userset;

        public Pixel(int value, boolean user)
        {
            this.value = value;
            this.userset = user;
        }
    }
}
