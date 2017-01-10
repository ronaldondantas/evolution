package br.com.nadod.evolution.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import br.com.nadod.evolution.activity.MeasurementActivity;
import br.com.nadod.evolution.adapter.MeasurementAdapter;
import br.com.nadod.evolution.model.Measure;
import br.com.nadod.evolution.model.Measurement;
import br.com.nadod.evolution.model.MeasurementDAO;
import br.com.nadod.evolution.model.MeasurementToList;
import br.com.nadod.evolution.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnMeasurementListInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MeasurementListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MeasurementListFragment extends Fragment {
    private MeasurementAdapter measurementAdapter;

    private List<String> measuresType = new ArrayList<>();
    private HashMap<Integer, Measure> measureHashMap = new HashMap<>();
    private Map<Integer, List<Measurement>> measurementList = new HashMap<>();

    private List<MeasurementToList> measurementToList = new ArrayList<>();

    private Spinner mbsMeasureType;

    int currentMeasureId = -1;
    private boolean hasChanges = false;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";


    private OnMeasurementListInteractionListener mListener;

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

            mbsMeasureType = (Spinner) view.findViewById(R.id.mbsMeasureType);
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
        this.hasChanges = hasChanges;
        if (hasChanges) {
            measurementList.clear();
            MeasurementDAO measurementDAO = new MeasurementDAO(getActivity());
            for (Integer measureId : measureHashMap.keySet()) {
                List<Measurement> measurements = measurementDAO.selectAllByMeasure(measureId);
                if (!measurements.isEmpty()) measurementList.put(measureId, measurements);
            }
            refreshMeasurementList(currentMeasureId);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMeasurementListInteractionListener) {
            mListener = (OnMeasurementListInteractionListener) context;
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
    public interface OnMeasurementListInteractionListener {
//        void onMeasurementListInteraction(MeasurementToList measurement);
    }
}
