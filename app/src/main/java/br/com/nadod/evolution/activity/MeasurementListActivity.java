package br.com.nadod.evolution.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.nadod.evolution.R;
import br.com.nadod.evolution.adapter.MeasurementAdapter;
import br.com.nadod.evolution.model.Measure;
import br.com.nadod.evolution.model.Measurement;
import br.com.nadod.evolution.model.MeasurementDAO;
import br.com.nadod.evolution.model.MeasurementToList;
import br.com.nadod.evolution.utils.Utils;
import br.com.nadod.evolution.view.DividerItemDecoration;

public class MeasurementListActivity extends AppCompatActivity {
    private static int EDIT_MEASUREMENT = 0;

    private MeasurementAdapter measurementAdapter;

    List<String> measuresType = new ArrayList<>();

    private HashMap<Integer, Measure> measureHashMap = new HashMap<>();
    private Map<Integer, List<Measurement>> measurementList = new HashMap<>();
    private List<MeasurementToList> measurementToList = new ArrayList<>();

    private Spinner mbsMeasureType;

    int currentMeasureId = -1;
    private boolean hasChanges = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measurement_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState != null) {
            if (savedInstanceState.getSerializable(Utils.MEASURE_LIST) != null &&
                    savedInstanceState.getSerializable(Utils.MEASUREMENT_LIST) != null &&
                    savedInstanceState.getSerializable(Utils.MEASURE_TYPE) != null) {
                measureHashMap =
                        (HashMap<Integer, Measure>)
                                savedInstanceState.getSerializable(Utils.MEASURE_LIST);
                measurementList =
                        (Map<Integer, List<Measurement>>)
                                savedInstanceState.getSerializable(Utils.MEASUREMENT_LIST);
                measuresType = (List<String>) savedInstanceState.getSerializable(Utils.MEASURE_TYPE);
            }
        } else {
            measureHashMap =
                    (HashMap<Integer, Measure>) getIntent().getSerializableExtra(Utils.MEASURE_LIST);
            measurementList =
                    (Map<Integer, List<Measurement>>) getIntent().getSerializableExtra(Utils.MEASUREMENT_LIST);
            measuresType = (List<String>) getIntent().getSerializableExtra(Utils.MEASURE_TYPE);
        }

        if (measuresType != null) {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, measuresType);

            mbsMeasureType = (Spinner) findViewById(R.id.mbsMeasureType);
            if (mbsMeasureType != null) {
                mbsMeasureType.setAdapter(arrayAdapter);
                mbsMeasureType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        refreshMeasurementList(measureHashMap.get(position + 1).getId());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rvMeasurementList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        measurementAdapter = new MeasurementAdapter(measurementToList, measureHashMap, this);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(measurementAdapter);
        }
    }

    private void refreshMeasurementList(int measureId) {
        currentMeasureId = measureId;
        measurementToList = getMeasurementToList(measurementList.get(currentMeasureId));
        Collections.sort(measurementToList);
        measurementAdapter.setMeasurementList(measurementToList);
        measurementAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(Utils.MEASURE_LIST, measureHashMap);
        outState.putSerializable(Utils.MEASUREMENT_LIST, (Serializable) measurementList);
        outState.putSerializable(Utils.MEASURE_TYPE, (Serializable) measuresType);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        measureHashMap = (HashMap<Integer, Measure>) savedInstanceState.getSerializable(Utils.MEASURE_LIST);
        measurementList = (Map<Integer, List<Measurement>>) savedInstanceState.getSerializable(Utils.MEASUREMENT_LIST);
        measuresType = (List<String>) savedInstanceState.getSerializable(Utils.MEASURE_TYPE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (hasChanges) {
            Intent intent = new Intent();
            intent.putExtra(Utils.NEW_MEASUREMENT_LIST, (Serializable) measurementList);
            setResult(RESULT_OK, intent);
        } else {
            setResult(RESULT_CANCELED);
        }
        finish();
    }

    private List<MeasurementToList> getMeasurementToList(List<Measurement> measurementList) {
        List<MeasurementToList> list = new ArrayList<>();
        for (Measurement measurement : measurementList) {
            MeasurementToList measurementToList = new MeasurementToList();
            measurementToList.setId(measurement.getId());
            measurementToList.setMeasureId(measurement.getMeasure_id());
            measurementToList.setDate(measurement.getDate());
            measurementToList.setValue(measurement.getValue());
            list.add(measurementToList);
        }
        return list;
    }

    public void measurementClicked(MeasurementToList measurement) {
        Intent intent = new Intent(this, MeasurementActivity.class);
        intent.putExtra(Utils.MEASUREMENT_DATA, measurement);
        intent.putExtra(Utils.TITLE_EDIT_MEASUREMENT, "Editar medição");
        startActivityForResult(intent, EDIT_MEASUREMENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT_MEASUREMENT) {
            if (resultCode == RESULT_OK) {
                hasChanges = data.getBooleanExtra(Utils.HAS_CHANGES, false);
                if (hasChanges) {
                    measurementList.clear();
                    MeasurementDAO measurementDAO = new MeasurementDAO(this);
                    for (Integer measureId : measureHashMap.keySet())
                        measurementList.put(measureId, measurementDAO.selectAllByMeasure(measureId));
                    refreshMeasurementList(currentMeasureId);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
