package org.simpledrive.sodokusolver;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

public class MainActivity extends Activity
{

    private FrameLayout container;
    private DrawCanvas canvas;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        canvas = new DrawCanvas(getApplicationContext());
        container = (FrameLayout)findViewById(R.id.sc);
        container.addView(canvas);
    }

    public void clearHandler(View v)
    {
        canvas.clear();
    }

    public void solveAllHandler(View v)
    {
        canvas.solveAllHandler();
    }

    public void solveOneHandler(View v)
    {
        if(canvas.hintMode)
        {
            v.setBackgroundColor(Color.WHITE);
            canvas.hintMode = false;
        }
        else
        {
            v.setBackgroundColor(canvas.lightGrey);
            canvas.hintMode = true;
            canvas.solveOneHandler();
        }
    }
}