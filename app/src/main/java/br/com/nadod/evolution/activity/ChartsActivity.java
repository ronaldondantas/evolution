package br.com.nadod.evolution.activity;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.LinearEase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import br.com.nadod.evolution.R;
import br.com.nadod.evolution.model.Measure;
import br.com.nadod.evolution.model.Measurement;

public class ChartsActivity extends AppCompatActivity {

    List<String> measuresType = new ArrayList<>();

    HashMap<Integer, Measure> measureHashMap = new HashMap<>();
    int currentMeasureId;
    HashMap<Integer, List<Measurement>> measurementHashMap = new HashMap<>();

    LineChartView lineChartView;
    TextView tvLastMeasurementDate;
    TextView tvMeasure;
    TextView tvResult;
    TextView tvMin;
    TextView tvMax;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        lineChartView = (LineChartView) findViewById(R.id.chart);
        tvLastMeasurementDate = (TextView) findViewById(R.id.tvDate);
        tvMeasure = (TextView) findViewById(R.id.tvMeasure);
        tvResult = (TextView) findViewById(R.id.tvResult);
        tvMin = (TextView) findViewById(R.id.tvMin);
        tvMax = (TextView) findViewById(R.id.tvMax);

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

        Calendar now = Calendar.getInstance();

        Measurement measurement = new Measurement();
        measurement.setId(1);
        measurement.setMeasureId(1);
        measurement.setValue((float) 125.5);
        now.set(2016, 4, 1);
        measurement.setDate(now.getTime().getTime());
        measurementListByMeasure.add(measurement);

        measurement = new Measurement();
        measurement.setId(1);
        measurement.setMeasureId(1);
        measurement.setValue((float) 128.5);
        now.set(2016, 6, 23);
        measurement.setDate(now.getTime().getTime());
        measurementListByMeasure.add(measurement);

        measurement = new Measurement();
        measurement.setId(1);
        measurement.setMeasureId(1);
        measurement.setValue((float) 129);
        now.set(2016, 9, 22);
        measurement.setDate(now.getTime().getTime());
        measurementListByMeasure.add(measurement);
        measurementHashMap.put(measurement.getMeasure_id(), measurementListByMeasure);

        measurementListByMeasure = new ArrayList<>();

        measurement = new Measurement();
        measurement.setId(2);
        measurement.setMeasureId(2);
        measurement.setValue((float) 130);
        now.set(2016, 4, 1);
        measurement.setDate(now.getTime().getTime());
        measurementListByMeasure.add(measurement);

        measurement = new Measurement();
        measurement.setId(2);
        measurement.setMeasureId(2);
        measurement.setValue((float) 120);
        now.set(2016, 6, 2);
        measurement.setDate(now.getTime().getTime());
        measurementListByMeasure.add(measurement);

        measurement = new Measurement();
        measurement.setId(2);
        measurement.setMeasureId(2);
        measurement.setValue((float) 110);
        now.set(2016, 9, 22);
        measurement.setDate(now.getTime().getTime());
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
        Pair<String[], float[]> labelsAndFloat = getStringAndLabels();
        LineSet lineSet = new LineSet(labelsAndFloat.first, labelsAndFloat.second);

        lineSet.setSmooth(true);
        lineSet.setThickness(Tools.fromDpToPx((float) 3.0));
        lineSet.setDotsRadius(Tools.fromDpToPx((float) 3.0));
        lineSet.setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        lineSet.setDotsColor(Color.parseColor("#FFFFFF"));
        lineSet.setDotsStrokeColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

        //using WilliamChart by @dfbernardino

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

            Animation anim = new Animation();
            anim.setEasing(new LinearEase());
            anim.setDuration(500);
            anim.setAlpha(1);
            anim.setStartPoint((float) 0.1, (float) 0.9);

            lineChartView.show(anim);
        }
    }

    private void refreshValues(double minValue, double maxValue, double lastMeasure, double result,
                               String dateLastMeasure) {
        tvLastMeasurementDate.setText(dateLastMeasure);
        String textMeasure = String.valueOf(lastMeasure) + " kg";
        tvMeasure.setText(textMeasure);

        String txtResult;
        if (result > 0) txtResult = "+" + String.valueOf(result);
        else txtResult = String.valueOf(result);
        tvResult.setText(txtResult);

        tvMin.setText(String.valueOf(minValue));
        tvMax.setText(String.valueOf(maxValue));
    }

    private Pair<String[], float[]> getStringAndLabels() {
        Pair<String[], float[]> pairLabelAndValues;
        List<Measurement> measurementList = measurementHashMap.get(currentMeasureId);
        String[] labels = new String[measurementList.size()];
        float[] values = new float[measurementList.size()];
        SimpleDateFormat format = new SimpleDateFormat("dd/MM");

        double minValue = 300;
        double maxValue = 0;

        double result;

        double firstMeasure = 0;
        long dateFirstMeasure = Long.MAX_VALUE;

        double lastMeasure = 0;
        long dateLastMeasure = 0;

        String dateTxtLastMeasure;

        int i=0;
        for (Measurement measurement : measurementList) {
            labels[i] = format.format(measurement.getDate());
            values[i] = measurement.getValue();

            if (measurement.getDate() < dateFirstMeasure) {
                dateFirstMeasure = measurement.getDate();
                firstMeasure = measurement.getValue();
            }
            if (measurement.getDate() > dateLastMeasure) {
                dateLastMeasure = measurement.getDate();
                lastMeasure = measurement.getValue();
            }
            if (measurement.getValue() < minValue) minValue = measurement.getValue();
            if (measurement.getValue() > maxValue) maxValue = measurement.getValue();

            i++;
        }
        format = new SimpleDateFormat("dd/MM/yyyy");
        dateTxtLastMeasure = format.format(dateLastMeasure);
        result = (lastMeasure - firstMeasure);

        refreshValues(minValue, maxValue, lastMeasure, result, dateTxtLastMeasure);
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
