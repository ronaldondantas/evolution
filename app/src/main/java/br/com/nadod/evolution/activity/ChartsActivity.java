package br.com.nadod.evolution.activity;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.model.Point;
import com.db.chart.view.AxisController;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.CircEase;
import com.db.chart.view.animation.easing.LinearEase;
import com.db.chart.view.animation.easing.SineEase;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.com.nadod.evolution.R;
import br.com.nadod.evolution.model.Measure;
import br.com.nadod.evolution.model.Measurement;

public class ChartsActivity extends AppCompatActivity {

    List<String> measuresType = new ArrayList<>();
//    String[] measuresType = {"Peso", "Cintura"};

    HashMap<Integer, Measure> measureHashMap = new HashMap<>();
    int currentMeasureId;
    HashMap<Integer, List<Measurement>> measurementHashMap = new HashMap<>();

    LineChartView lineChartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        lineChartView = (LineChartView) findViewById(R.id.chart);

        //TODO: MOCK RETIRAR

//        measureList = MeasureDAO.selectAll();
        Measure measure = new Measure();
        measure.setId(1);
        measure.setName("weight");
        measure.setDescription("Peso");
        measureHashMap.put(measure.getId(), measure);
        measuresType.add(measure.getDescription());

        measure = new Measure();
        measure.setId(2);
        measure.setName("waist");
        measure.setDescription("Cintura");
        measureHashMap.put(measure.getId(), measure);
        measuresType.add(measure.getDescription());

        List<Measurement> measurementListByMeasure = new ArrayList<>();

        Measurement measurement = new Measurement();
        measurement.setId(1);
        measurement.setMeasureId(1);
        measurement.setValue((float) 125.5);
        measurement.setDate(SystemClock.currentThreadTimeMillis()/1000);
        measurementListByMeasure.add(measurement);

        measurement = new Measurement();
        measurement.setId(1);
        measurement.setMeasureId(1);
        measurement.setValue((float) 128.5);
        measurement.setDate(SystemClock.currentThreadTimeMillis()/1000);
        measurementListByMeasure.add(measurement);

        measurement = new Measurement();
        measurement.setId(1);
        measurement.setMeasureId(1);
        measurement.setValue((float) 129);
        measurement.setDate(SystemClock.currentThreadTimeMillis()/1000);
        measurementListByMeasure.add(measurement);
        measurementHashMap.put(measurement.getMeasure_id(), measurementListByMeasure);

        measurementListByMeasure = new ArrayList<>();

        measurement = new Measurement();
        measurement.setId(2);
        measurement.setMeasureId(2);
        measurement.setValue((float) 130);
        measurement.setDate(SystemClock.currentThreadTimeMillis()/1000);
        measurementListByMeasure.add(measurement);

        measurement = new Measurement();
        measurement.setId(2);
        measurement.setMeasureId(2);
        measurement.setValue((float) 120);
        measurement.setDate(SystemClock.currentThreadTimeMillis()/1000);
        measurementListByMeasure.add(measurement);

        measurement = new Measurement();
        measurement.setId(2);
        measurement.setMeasureId(2);
        measurement.setValue((float) 110);
        measurement.setDate(SystemClock.currentThreadTimeMillis()/1000);
        measurementListByMeasure.add(measurement);
        measurementHashMap.put(measurement.getMeasure_id(), measurementListByMeasure);

        //TODO: FIM DO MOCK

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, measuresType);

        Spinner mbsMeasureType =
                (Spinner) findViewById(R.id.mbsMeasureType);
        if (mbsMeasureType != null) {
            mbsMeasureType.setAdapter(arrayAdapter);
            mbsMeasureType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    currentMeasureId = measureHashMap.get(position+1).getId();
                    plotChart();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void plotChart() {
        Pair<String[], float[]> labelsAndFloat = getStringAndLabels(currentMeasureId);
        LineSet lineSet = new LineSet(labelsAndFloat.first, labelsAndFloat.second);

        lineSet.setSmooth(true);
        lineSet.setThickness(Tools.fromDpToPx((float) 3.0));
        lineSet.setDotsRadius(Tools.fromDpToPx((float) 3.0));
        lineSet.setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        lineSet.setDotsColor(Color.parseColor("#FFFFFF"));
        lineSet.setDotsStrokeColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

        if (lineChartView != null) {
            lineChartView.reset();
            lineChartView.addData(lineSet);
            lineChartView.setAxisBorderValues(110, 130);
            lineChartView.setYAxis(false);
            lineChartView.setXAxis(false);
//            lineChartView.setXLabels(AxisController.LabelPosition.NONE);
//            lineChartView.setYLabels(AxisController.LabelPosition.NONE);
            Paint gridPaint = new Paint();
            gridPaint.setColor(Color.parseColor("#BDBDBD"));
            gridPaint.setStyle(Paint.Style.STROKE);
            gridPaint.setAntiAlias(true);
            gridPaint.setStrokeWidth(Tools.fromDpToPx((float) 1.0));
            lineChartView.setGrid(ChartView.GridType.HORIZONTAL, gridPaint);

            // Animation customization
            Animation anim = new Animation();
            anim.setEasing(new LinearEase());
            anim.setDuration(500);
            anim.setAlpha(1);
            anim.setStartPoint((float) 0.1, (float) 0.9);

            lineChartView.show(anim);
        }
    }

    private Pair<String[], float[]> getStringAndLabels(int measure_id) {
        Pair<String[], float[]> pairLabelAndValues;
        List<Measurement> measurementList = measurementHashMap.get(measure_id);
        String[] labels = new String[measurementList.size()];
        float[] values = new float[measurementList.size()];
        int i=0;
        for (Measurement date : measurementList) {
            labels[i] = String.valueOf(date.getDate()); //TODO: Converter para Date
            values[i] = date.getValue();
            i++;
        }
        pairLabelAndValues = new Pair<>(labels, values);
        return pairLabelAndValues;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_charts, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_view_list) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
