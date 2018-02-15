package edu.cnm.deepdive.life;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TextView;
import edu.cnm.deepdive.life.ca.Model;

public class MainActivity extends AppCompatActivity {

  final private Object lock = new Object();
  private Model model;
  private boolean running = false;
  private Runner runner = null;
  private TextView generationCounter;
  private SeekBar populationDensity;
  private String generationFormat;
  private boolean updatePending = false;
  private TerrainView terrainView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main); //inflater
    terrainView = findViewById(R.id.terrain_view);// inflate terrain view
    generationCounter = findViewById(R.id.generation_counter); //wire field to XML
    populationDensity = findViewById(R.id.population_density); // wire
    generationFormat = generationCounter.getText().toString();
    model = new Model(200, 200);
    model.populate(populationDensity.getProgress() / 100.0);
    updateView(model.getTerrain(), model.getGeneration());
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.options, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_step:
        model.advance();
        updateView(model.getTerrain(), model.getGeneration());
        break;
      case R.id.action_run:
        running = true;
        runner = new Runner();
        runner.start();
        break;
      case R.id.action_stop:
        running = false;
        runner = null;
        break;
      case R.id.action_reset:
        model.populate(populationDensity.getProgress() / 100.0);
        updateView(model.getTerrain(), model.getGeneration());
        break;
      default:
        return super.onOptionsItemSelected(item);
    }
    invalidateOptionsMenu();
    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    menu.findItem(R.id.action_step).setEnabled(!running);
    menu.findItem(R.id.action_run).setVisible(!running);
    menu.findItem(R.id.action_stop).setVisible(running);
    menu.findItem(R.id.action_reset).setEnabled(!running);
    return true;
  }

  private void updateView(byte[][] terrain, int generation) {
    terrainView.setTerrain(terrain);
    terrainView.invalidate(); //whatever drawn before is no longer good.
    generationCounter.setText(String.format(generationFormat, generation));
  }

  private class Runner extends Thread {

    @Override
    public void run() {
     while (running) {
       synchronized (lock) {
         model.advance();
         if (!terrainView.isUpdatePending()) {
           update();
         }
       }
      }
      update();  //final update after hitting stop button
    }

    private void update() {
      final byte[][] terrain = model.getTerrain();
      final int generation = model.getGeneration();
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          updateView(terrain, generation);
        }
      });
    }
  }
}
