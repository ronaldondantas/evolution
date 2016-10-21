package br.com.nadod.evolution.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.nadod.evolution.R;
import br.com.nadod.evolution.model.Measure;
import br.com.nadod.evolution.model.MeasureDAO;
import br.com.nadod.evolution.model.Measurement;
import br.com.nadod.evolution.model.MeasurementDAO;
import br.com.nadod.evolution.model.MeasurementToList;
import br.com.nadod.evolution.utils.Utils;

public class MeasurementActivity extends AppCompatActivity
        implements DatePickerDialog.OnDateSetListener {

    private static String MEASUREMENT_BY_DATE = "MEASUREMENT_BY_DATE";

    private List<MaterialEditText> materialEditTexts;
    private MaterialEditText metDate;

    private HashMap<Integer, Measure> measureHashMap = new HashMap<>();

    private HashMap<Integer, Measurement> measurementListByDate = new HashMap<>();
    private MeasurementToList currentMeasurement = null;

    private String title = "Cadastrar medição";
    private HashMap<Integer, String> metText = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measurement);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState != null) {
            measureHashMap = (HashMap<Integer, Measure>) savedInstanceState.get(Utils.MEASURE_TYPE);
            currentMeasurement = (MeasurementToList) savedInstanceState.getSerializable(Utils.MEASUREMENT_DATA);
            measurementListByDate = (HashMap<Integer, Measurement>) savedInstanceState.get(MEASUREMENT_BY_DATE);
            measurementListByDate = (HashMap<Integer, Measurement>) savedInstanceState.get(MEASUREMENT_BY_DATE);
            if (savedInstanceState.getString(Utils.TITLE_EDIT_MEASUREMENT) != null &&
                    !savedInstanceState.getString(Utils.TITLE_EDIT_MEASUREMENT).isEmpty()) {
                title = savedInstanceState.getString(Utils.TITLE_EDIT_MEASUREMENT);
            }
            if (savedInstanceState.getSerializable(Utils.MET_TEXT) != null) {
                metText = (HashMap<Integer, String>) savedInstanceState.getSerializable(Utils.MET_TEXT);
            }
        } else {
            measureHashMap = (HashMap<Integer, Measure>) getIntent().getSerializableExtra(Utils.MEASURE_TYPE);
            currentMeasurement = (MeasurementToList) getIntent().getSerializableExtra(Utils.MEASUREMENT_DATA);
            if (getIntent().getStringExtra(Utils.TITLE_EDIT_MEASUREMENT) != null &&
                    !getIntent().getStringExtra(Utils.TITLE_EDIT_MEASUREMENT).isEmpty()) {
                title = getIntent().getStringExtra(Utils.TITLE_EDIT_MEASUREMENT);
            }
            if (getIntent().getStringExtra(Utils.MET_TEXT) != null) {
                metText = (HashMap<Integer, String>) getIntent().getSerializableExtra(Utils.MET_TEXT);
            }
        }

        setTitle(title);

        if (measureHashMap == null || measureHashMap.isEmpty()) {
            measureHashMap = new HashMap<>();
            MeasureDAO measureDAO = new MeasureDAO(this);
            List<Measure> measureList = measureDAO.selectAll();
            for (Measure measure : measureList) measureHashMap.put(measure.getId(), measure);
        }

        long date = -1;
        if (currentMeasurement != null && measurementListByDate.isEmpty()) {
            MeasurementDAO measurementDAO = new MeasurementDAO(this);
            date = currentMeasurement.getDate();
            measurementListByDate = measurementDAO.selectAllByDate(date);
        }

        LinearLayout llMeasureType = (LinearLayout) findViewById(R.id.llMeasureType);
        if (llMeasureType != null) {
            addMetDate(llMeasureType, date);
            materialEditTexts = new ArrayList<>();
            TextView tvMeasureType;
            MaterialEditText materialEditText;
            for (final Measure measure : measureHashMap.values()) {
                if (currentMeasurement == null || currentMeasurement.getMeasure_id() == measure.getId()) {
                    tvMeasureType = new TextView(this);
                    tvMeasureType.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
                    tvMeasureType.setTextSize((float) 18);
                    tvMeasureType.setText(measure.getDescription());
                    llMeasureType.addView(tvMeasureType);

                    materialEditText = new MaterialEditText(this);
                    materialEditText.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
                    materialEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    materialEditText.setTextSize((float) 18);
                    materialEditText.setTag(measure.getId());
                    if (!measurementListByDate.isEmpty()) {
                        Measurement measurement = measurementListByDate.get(measure.getId());
                        if (measurement != null)
                            metText.put(measure.getId(), String.valueOf(measurement.getValue()));
                    }
                    materialEditText.setText(metText.get(measure.getId()));
                    materialEditText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            metText.put(measure.getId(), s.toString());
                        }
                    });

                    llMeasureType.addView(materialEditText);
                    materialEditTexts.add(materialEditText);
                }
            }
        }

        final DatePickerDialog.OnDateSetListener measurementActivity = this;

        Calendar now = Calendar.getInstance();
        String dateNow = now.get(Calendar.DAY_OF_MONTH) +"/"+ (now.get(Calendar.MONTH)+1) + "/" +
                now.get(Calendar.YEAR);
        if (metDate != null) {
            if (date == -1) metDate.setText(dateNow);
            metDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Calendar now = Calendar.getInstance();
                    DatePickerDialog dpd = DatePickerDialog.newInstance(measurementActivity,
                            now.get(Calendar.YEAR), now.get(Calendar.MONTH),
                            now.get(Calendar.DAY_OF_MONTH)
                    );
                    dpd.show(getFragmentManager(), "DatePickerDialog");
                }
            });
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(MEASUREMENT_BY_DATE, measurementListByDate);
        outState.putSerializable(Utils.MEASURE_TYPE, measureHashMap);
        outState.putSerializable(Utils.MEASUREMENT_DATA, currentMeasurement);
        outState.putString(Utils.TITLE_EDIT_MEASUREMENT, title);
        outState.putSerializable(Utils.MET_TEXT, metText);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        measurementListByDate = (HashMap<Integer, Measurement>) savedInstanceState.get(MEASUREMENT_BY_DATE);
        measureHashMap = (HashMap<Integer, Measure>) savedInstanceState.get(Utils.MEASURE_TYPE);
        currentMeasurement = (MeasurementToList) savedInstanceState.getSerializable(Utils.MEASUREMENT_DATA);
        title = savedInstanceState.getString(Utils.TITLE_EDIT_MEASUREMENT);
        metText = (HashMap<Integer, String>) savedInstanceState.getSerializable(Utils.MET_TEXT);
    }

    private void addMetDate(LinearLayout llMeasureType, long date) {
        TextView tvMeasureType;
        tvMeasureType = new TextView(this);
        tvMeasureType.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        tvMeasureType.setTextSize((float) 18);
        tvMeasureType.setText("Data");
        llMeasureType.addView(tvMeasureType);

        metDate = new MaterialEditText(this);
        metDate.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        metDate.setTextSize((float) 18);

        if (date != -1) {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            metDate.setText(format.format(date));
        }

        llMeasureType.addView(metDate);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (!measurementListByDate.isEmpty()) {
            getMenuInflater().inflate(R.menu.menu_delete_measurement, menu);
            return true;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.delete) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Deseja realmente excluir essa medição?")
                    .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            MeasurementDAO measurementDAO = new MeasurementDAO(getApplicationContext());
                            measurementDAO.delete(currentMeasurement.getId());
                            onBackPressed();
                        }
                    })
                    .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    }).create().show();
        } else if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Map<Integer, List<Measurement>> measurementHashMap = new HashMap<>();

        Calendar chosenDate = Calendar.getInstance();
        String txtDate = metDate.getText().toString();
        String[] dateSplit = txtDate.split("/");
        String day = dateSplit[0];
        String month = dateSplit[1];
        String year = dateSplit[2];
        chosenDate.set(Integer.valueOf(year), Integer.valueOf(month)-1, Integer.valueOf(day));

        Measurement measurement;
        for (MaterialEditText metMeasure : materialEditTexts) {
            if (metMeasure.getText().toString().isEmpty()) continue;
            List<Measurement> measurementList = new ArrayList<>();
            measurement = new Measurement();
            measurement.setDate(chosenDate.getTime().getTime());
            measurement.setMeasureId((int) metMeasure.getTag());
            measurement.setValue(Float.valueOf(metMeasure.getText().toString()));

            if (!measurementListByDate.isEmpty()) {
                measurement.setId(measurementListByDate.get(measurement.getMeasure_id()).getId());
            }

            measurementList.add(measurement);
            measurementHashMap.put(measurement.getMeasure_id(), measurementList);
        }
        if (!measurementHashMap.isEmpty()) {
            MeasurementDAO measurementDAO = new MeasurementDAO(this);
            boolean hasChanges = false;
            if (measurementListByDate.isEmpty()) {
                measurementDAO.insertAll(measurementHashMap);
            } else {
                hasChanges = true;
                measurementDAO.updateAll(measurementHashMap);
            }
            Intent intent = new Intent();
            intent.putExtra(Utils.NEW_MEASUREMENT_LIST, (Serializable) measurementHashMap);
            intent.putExtra(Utils.HAS_CHANGES, hasChanges);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String chosenDate = dayOfMonth+"/"+(monthOfYear+1)+"/"+year;
        if (metDate != null) metDate.setText(chosenDate);
    }
}
