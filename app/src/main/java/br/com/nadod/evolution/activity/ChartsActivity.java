package br.com.nadod.evolution.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.LinearEase;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import io.fabric.sdk.android.Fabric;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.nadod.evolution.R;
import br.com.nadod.evolution.model.Measure;
import br.com.nadod.evolution.model.MeasureDAO;
import br.com.nadod.evolution.model.Measurement;
import br.com.nadod.evolution.model.MeasurementDAO;
import br.com.nadod.evolution.utils.Utils;

public class ChartsActivity extends AppCompatActivity {
    private static int ADD_MEASUREMENT = 0;
    private static int MEASUREMENT_LIST = 1;

    private static String MEASURES_TYPE = "MEASURES_TYPE";
    private static String MEASURE_HASHMAP = "MEASURE_HASHMAP";
    private static String MEASUREMENT_HASHMAP = "MEASUREMENT_HASHMAP";
    private static String CURRENT_MEASURE_ID = "CURRENT_MEASURE_ID";

    List<String> measuresType = new ArrayList<>();
    HashMap<Integer, Measure> measureHashMap = new HashMap<>();
    Map<Integer, List<Measurement>> measurementHashMap = new HashMap<>();
    int currentMeasureId = -1;

    LineChartView lineChartView;
    LinearLayout llDate;
    TextView tvLastMeasurementDate;
    TextView tvMeasure;
    TextView tvResult;
    TextView tvMin;
    TextView tvMax;
    TextView tvTitleMin;
    TextView tvTitleMax;
    Spinner mbsMeasureType;

    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_charts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MobileAds.initialize(this, getString(R.string.interstitial_ad_unit_id));

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
            }
        });

        requestNewInterstitial();

        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }

        lineChartView = (LineChartView) findViewById(R.id.chart);
        tvLastMeasurementDate = (TextView) findViewById(R.id.tvDate);
        tvMeasure = (TextView) findViewById(R.id.tvMeasure);
        tvResult = (TextView) findViewById(R.id.tvResult);
        tvMin = (TextView) findViewById(R.id.tvMin);
        tvMax = (TextView) findViewById(R.id.tvMax);
        llDate = (LinearLayout) findViewById(R.id.llDate);
        tvTitleMin = (TextView) findViewById(R.id.tvMinTitle);
        tvTitleMax = (TextView) findViewById(R.id.tvMaxTitle);

        if (savedInstanceState != null) {
            if (savedInstanceState.getSerializable(MEASURES_TYPE) != null &&
                    savedInstanceState.getSerializable(MEASURE_HASHMAP) != null &&
                    savedInstanceState.getSerializable(MEASUREMENT_HASHMAP) != null) {
                measuresType = (List<String>) savedInstanceState.getSerializable(MEASURES_TYPE);
                measureHashMap = (HashMap<Integer, Measure>) savedInstanceState.getSerializable(MEASURE_HASHMAP);
                measurementHashMap = (Map<Integer, List<Measurement>>) savedInstanceState.getSerializable(MEASUREMENT_HASHMAP);
            }
            currentMeasureId = savedInstanceState.getInt(CURRENT_MEASURE_ID);
        }

        if (measurementHashMap.isEmpty()) {
            MeasurementDAO measurementDAO = new MeasurementDAO(this);
            MeasureDAO measureDAO = new MeasureDAO(this);
            List<Measure> measureList = measureDAO.selectAll();
            for (Measure measure : measureList) {
                measureHashMap.put(measure.getId(), measure);
                measuresType.add(measure.getDescription());

                measurementHashMap.put(measure.getId(),
                        measurementDAO.selectAllByMeasure(measure.getId()));
            }
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, measuresType);

        mbsMeasureType = (Spinner) findViewById(R.id.mbsMeasureType);
        if (mbsMeasureType != null) {
            mbsMeasureType.setAdapter(arrayAdapter);
            mbsMeasureType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    currentMeasureId = measureHashMap.get(position + 1).getId();
                    plotChart();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showInterstitial();
                    Intent intent = new Intent(view.getContext(), MeasurementActivity.class);
                    intent.putExtra(Utils.MEASURE_TYPE, measureHashMap);
                    startActivityForResult(intent, ADD_MEASUREMENT);
                }
            });
        }

        loadInterstitial();
    }

    private void showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and restart the game.
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            loadInterstitial();
        }
    }

    private void loadInterstitial() {
        // Request a new ad if one isn't already loaded, hide the button, and kick off the timer.
        if (!mInterstitialAd.isLoading() && !mInterstitialAd.isLoaded()) {
            AdRequest adRequest = new AdRequest.Builder().build();
            mInterstitialAd.loadAd(adRequest);
        }
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(getString(R.string.banner_ad_unit_id_test))
                .build();
        mInterstitialAd.loadAd(adRequest);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(MEASURES_TYPE, (Serializable) measuresType);
        outState.putSerializable(MEASURE_HASHMAP, measureHashMap);
        outState.putSerializable(MEASUREMENT_HASHMAP, (Serializable) measurementHashMap);
        outState.putInt(CURRENT_MEASURE_ID, currentMeasureId);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        measuresType = (List<String>) savedInstanceState.getSerializable(MEASURES_TYPE);
        measureHashMap = (HashMap<Integer, Measure>) savedInstanceState.getSerializable(MEASURE_HASHMAP);
        measurementHashMap = (Map<Integer, List<Measurement>>) savedInstanceState.getSerializable(MEASUREMENT_HASHMAP);
        currentMeasureId = savedInstanceState.getInt(CURRENT_MEASURE_ID);
    }

    private void plotChart() {
        Pair<String[], float[]> labelsAndFloat = getStringAndLabels();
        if (labelsAndFloat != null) {
            enableComponents(true);
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
                lineChartView.setAxisBorderValues((Math.round(Float.valueOf(tvMin.getText().toString()) - 1)),
                        (Math.round(Float.valueOf(tvMax.getText().toString()) - 1) + 1));
                lineChartView.setYAxis(false);
                lineChartView.setXAxis(false);
//            lineChartView.setXLabels(AxisController.LabelPosition.NONE);
                lineChartView.setYLabels(AxisController.LabelPosition.NONE);
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
        } else {
            enableComponents(false);
        }
    }

    private void enableComponents(boolean enable) {
        if (enable) {
            tvLastMeasurementDate.setVisibility(View.VISIBLE);
            tvMeasure.setVisibility(View.VISIBLE);
            tvMin.setVisibility(View.VISIBLE);
            tvMax.setVisibility(View.VISIBLE);
            tvTitleMin.setVisibility(View.VISIBLE);
            tvTitleMax.setVisibility(View.VISIBLE);
            llDate.setVisibility(View.VISIBLE);
            mbsMeasureType.setVisibility(View.VISIBLE);
        } else {
            tvLastMeasurementDate.setVisibility(View.INVISIBLE);
            tvMeasure.setVisibility(View.INVISIBLE);
            tvMin.setVisibility(View.INVISIBLE);
            tvMax.setVisibility(View.INVISIBLE);
            tvTitleMin.setVisibility(View.INVISIBLE);
            tvTitleMax.setVisibility(View.INVISIBLE);
            llDate.setVisibility(View.INVISIBLE);
            mbsMeasureType.setVisibility(View.INVISIBLE);
            tvResult.setText("Insira sua primeira medição");
        }
    }

    private void refreshValues(double minValue, double maxValue, double lastMeasure, double result,
                               String dateLastMeasure) {
        tvLastMeasurementDate.setText(dateLastMeasure);
        String textMeasure = String.format("%.1f", lastMeasure) + " kg";
        tvMeasure.setText(textMeasure);

        String txtResult;
        if (result > 0) txtResult = "+" + String.format("%.1f", result);
        else txtResult = String.format("%.1f", result);
        tvResult.setText(txtResult);

        tvMin.setText(String.format("%.1f", minValue));
        tvMax.setText(String.format("%.1f", maxValue));
    }

    private Pair<String[], float[]> getStringAndLabels() {
        Pair<String[], float[]> pairLabelAndValues;
        List<Measurement> measurementList = measurementHashMap.get(currentMeasureId);

        if (measurementList.isEmpty()) return null;

        Collections.sort(measurementList);
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
            showInterstitial();
            Intent intent = new Intent(this, MeasurementListActivity.class);
            intent.putExtra(Utils.MEASURE_LIST, measureHashMap);
            intent.putExtra(Utils.MEASUREMENT_LIST, (Serializable) measurementHashMap);
            intent.putExtra(Utils.MEASURE_TYPE, (Serializable) measuresType);
            startActivityForResult(intent, MEASUREMENT_LIST);
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            HashMap<Integer, List<Measurement>> measurementHashMap =
                    (HashMap<Integer, List<Measurement>>) data.getSerializableExtra(Utils.NEW_MEASUREMENT_LIST);
            boolean hasNewMeasurement = (requestCode == ADD_MEASUREMENT);
            if (!hasNewMeasurement) this.measurementHashMap.clear();
            List<Measurement> totalMeasurement;
            for (Integer measureId : measurementHashMap.keySet()) {
                totalMeasurement = new ArrayList<>();
                if (hasNewMeasurement)
                    totalMeasurement.addAll(this.measurementHashMap.get(measureId));
                totalMeasurement.addAll(measurementHashMap.get(measureId));
                this.measurementHashMap.put(measureId, totalMeasurement);
            }
            plotChart();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
