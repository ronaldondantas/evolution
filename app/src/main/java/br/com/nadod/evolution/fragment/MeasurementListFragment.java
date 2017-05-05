package br.com.nadod.evolution.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.nadod.evolution.R;
import br.com.nadod.evolution.activity.OnDataChanged;
import br.com.nadod.evolution.adapter.MeasurementAdapter;
import br.com.nadod.evolution.model.Measure;
import br.com.nadod.evolution.model.Measurement;
import br.com.nadod.evolution.model.MeasurementDAO;
import br.com.nadod.evolution.model.MeasurementToList;
import br.com.nadod.evolution.singleton.UserSingleton;
import br.com.nadod.evolution.utils.Utils;

public class MeasurementListFragment extends Fragment implements OnDataChanged {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private MeasurementAdapter measurementAdapter;

    private List<String> measuresType = new ArrayList<>();
    private HashMap<Integer, Measure> measureHashMap = new HashMap<>();
    private Map<Integer, List<Measurement>> measurementList = new HashMap<>();

    private List<MeasurementToList> measurementToList = new ArrayList<>();

    int currentMeasureId = -1;

    public MeasurementListFragment() {
        // Required empty public constructor
    }

    public static MeasurementListFragment newInstance(List<String> measuresType,
                                                      HashMap<Integer, Measure> measureHashMap,
                                                      Map<Integer, List<Measurement>> measurementList) {
        MeasurementListFragment fragment = new MeasurementListFragment();
        Bundle args = new Bundle();
        args.putSerializable(Utils.MEASURE_TYPE, (Serializable) measuresType);
        args.putSerializable(Utils.MEASURE_HASHMAP, measureHashMap);
        args.putSerializable(Utils.MEASUREMENT_LIST, (Serializable) measurementList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            measuresType = (List<String>) getArguments().getSerializable(Utils.MEASURE_TYPE);
            measureHashMap = (HashMap<Integer, Measure>) getArguments().getSerializable(Utils.MEASURE_HASHMAP);
            measurementList = (Map<Integer, List<Measurement>>) getArguments().getSerializable(Utils.MEASUREMENT_LIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_measurement_list, container, false);

        if (measuresType != null) {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_dropdown_item_1line, measuresType);

            Spinner mbsMeasureType = (Spinner) view.findViewById(R.id.mbsMeasureType);
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

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.rvMeasurementList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        measurementAdapter = new MeasurementAdapter(measurementToList, measureHashMap, getActivity());
        if (recyclerView != null) {
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(measurementAdapter);
        }

        return view;
    }

    private void refreshMeasurementList(int measureId) {
        currentMeasureId = measureId;
        List<Measurement> measurementsByMeasure = measurementList.get(currentMeasureId);
        if (measurementsByMeasure != null) {
            measurementToList = getMeasurementToList(measurementsByMeasure);
            Collections.sort(measurementToList);
        } else {
            measurementToList.clear();
        }
        measurementAdapter.setMeasurementList(measurementToList);
        measurementAdapter.notifyDataSetChanged();
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

    public void afterCrudMeasurement(boolean hasChanges) {
        if (hasChanges) {
            measurementList.clear();
            MeasurementDAO measurementDAO = new MeasurementDAO(getActivity());
            for (Integer measureId : measureHashMap.keySet()) {
                List<Measurement> measurements = measurementDAO.selectAllByMeasure(measureId,
                        UserSingleton.getInstance(getActivity()).getUid());
                if (!measurements.isEmpty()) measurementList.put(measureId, measurements);
            }
            refreshMeasurementList(currentMeasureId);
        }
    }

    @Override
    public void refreshData() {
        refreshMeasurementList(currentMeasureId);
    }
}
