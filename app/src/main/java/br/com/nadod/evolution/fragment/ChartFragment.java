package br.com.nadod.evolution.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.LinearEase;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.nadod.evolution.R;
import br.com.nadod.evolution.activity.MeasurementActivity;
import br.com.nadod.evolution.model.Measure;
import br.com.nadod.evolution.model.MeasureDAO;
import br.com.nadod.evolution.model.Measurement;
import br.com.nadod.evolution.model.MeasurementDAO;
import br.com.nadod.evolution.utils.Utils;

public class ChartFragment extends Fragment {
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

    private OnChartInteractionListener mListener;

    public ChartFragment() {
        // Required empty public constructor
    }

    public static ChartFragment newInstance(List<String> measuresType, HashMap<Integer, Measure> measureHashMap,
                                            Map<Integer, List<Measurement>> measurementHashMap) {
        ChartFragment fragment = new ChartFragment();
        Bundle args = new Bundle();
        args.putSerializable(Utils.MEASURE_TYPE, (Serializable) measuresType);
        args.putSerializable(Utils.MEASURE_HASHMAP, measureHashMap);
        args.putSerializable(Utils.MEASUREMENT_HASHMAP, (Serializable) measurementHashMap);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            measuresType = (List<String>) getArguments().getSerializable(Utils.MEASURE_TYPE);
            measureHashMap = (HashMap<Integer, Measure>) getArguments().getSerializable(Utils.MEASURE_HASHMAP);
            measurementHashMap = (Map<Integer, List<Measurement>>) getArguments().getSerializable(Utils.MEASUREMENT_HASHMAP);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chart, container, false);

        lineChartView = (LineChartView) view.findViewById(R.id.chart);
        tvLastMeasurementDate = (TextView) view.findViewById(R.id.tvDate);
        tvMeasure = (TextView) view.findViewById(R.id.tvMeasure);
        tvResult = (TextView) view.findViewById(R.id.tvResult);
        tvMin = (TextView) view.findViewById(R.id.tvMin);
        tvMax = (TextView) view.findViewById(R.id.tvMax);
        llDate = (LinearLayout) view.findViewById(R.id.llDate);
        tvTitleMin = (TextView) view.findViewById(R.id.tvMinTitle);
        tvTitleMax = (TextView) view.findViewById(R.id.tvMaxTitle);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_dropdown_item_1line, measuresType);

        mbsMeasureType = (Spinner) view.findViewById(R.id.mbsMeasureType);
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

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) mListener.onChartInteraction(measureHashMap);
                }
            });
        }

        return view;
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

    private Pair<String[], float[]> getStringAndLabels() {
        Pair<String[], float[]> pairLabelAndValues;
        List<Measurement> measurementList = measurementHashMap.get(currentMeasureId);

        if (measurementList == null || measurementList.isEmpty()) return null;

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

    private void refreshValues(double minValue, double maxValue, double lastMeasure, double result,
                               String dateLastMeasure) {
        tvLastMeasurementDate.setText(dateLastMeasure);
        String unit = measureHashMap.get(currentMeasureId).getName().compareTo("weight") == 0 ?
                "kg" : "cm";
        String textMeasure = String.format("%.1f", lastMeasure) + " " + unit;
        tvMeasure.setText(textMeasure);

        String txtResult;
        if (result > 0) txtResult = "+" + String.format("%.1f", result);
        else txtResult = String.format("%.1f", result);
        tvResult.setText(txtResult);

        tvMin.setText(String.format("%.1f", minValue));
        tvMax.setText(String.format("%.1f", maxValue));
    }

    private void plotChart() {
        Pair<String[], float[]> labelsAndFloat = getStringAndLabels();
        if (labelsAndFloat != null) {
            enableComponents(true);
            LineSet lineSet = new LineSet(labelsAndFloat.first, labelsAndFloat.second);

            lineSet.setSmooth(true);
            lineSet.setThickness(Tools.fromDpToPx((float) 3.0));
            lineSet.setDotsRadius(Tools.fromDpToPx((float) 3.0));
            lineSet.setColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
            lineSet.setDotsColor(Color.parseColor("#FFFFFF"));
            lineSet.setDotsStrokeColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));

            //using WilliamChart by @dfbernardino

            if (lineChartView != null) {
                lineChartView.reset();
                lineChartView.addData(lineSet);
                String minValue = tvMin.getText().toString().replace(",",".");
                String maxValue = tvMax.getText().toString().replace(",",".");
                lineChartView.setAxisBorderValues(Math.round(Float.parseFloat(minValue) - 1),
                        Math.round(Float.parseFloat(maxValue) - 1) + 1);
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

    public void afterCrudMeasurement() {
        plotChart();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnChartInteractionListener) {
            mListener = (OnChartInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnChartInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnChartInteractionListener {
        // TODO: Update argument type and name
        void onChartInteraction(HashMap<Integer, Measure> measureHashMap);
    }
}
