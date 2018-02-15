package edu.cnm.deepdive.life;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class TerrainView extends View {

  private byte[][] terrain = null;
  private Paint paint = new Paint();
  private boolean updatePending = false;

  {
    paint.setColor(Color.CYAN);
    setWillNotDraw(false);
  }

  public TerrainView(Context context) {
    super(context);
  }

  public TerrainView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public TerrainView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public TerrainView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) { //find out the size
    int width = getSuggestedMinimumWidth();
    int height = getSuggestedMinimumHeight();
    width = resolveSizeAndState(getPaddingRight() + getPaddingRight() + width, widthMeasureSpec, 0); //this adapts the drawable width
    height = resolveSizeAndState(getPaddingTop() + getPaddingBottom() + height, heightMeasureSpec, 0);
    width = Math.max(width, height);
    height = width;
    setMeasuredDimension(width, height);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    //TODO Explore off-screen bitmap
    if (terrain != null) {
      updatePending = true;
      byte[][] safeTerrain = terrain;
      float cellSize = Math.min((float) getHeight() / safeTerrain.length,
          (float) getWidth() / safeTerrain[0].length);
      canvas.drawColor(Color.BLACK);
      for (int i = 0; i < safeTerrain.length; i++) {
        for (int j = 0; j < safeTerrain[i].length; j++) {
          if (terrain[i][j] !=0){
            canvas.drawCircle((j + 0.5f) * cellSize, (i + 0.5f) * cellSize, cellSize / 2, paint);
          }
        }
      }
      updatePending = false;
    }

  }

  public void setTerrain(byte[][] terrain) {
    this.terrain = terrain;
  }

  public boolean isUpdatePending() {
    return updatePending;
  }
}
