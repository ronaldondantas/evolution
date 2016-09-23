package br.com.nadod.evolution.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.nadod.evolution.R;
import br.com.nadod.evolution.model.Measure;
import br.com.nadod.evolution.model.Measurement;

public class MeasurementActivity extends AppCompatActivity
        implements DatePickerDialog.OnDateSetListener {

    List<MaterialEditText> materialEditTexts;
    MaterialEditText metDate;

    HashMap<Integer, Measure> measureHashMap = null;

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
            measureHashMap = (HashMap<Integer, Measure>) savedInstanceState.get("MEASURES_TYPE");
        } else {
            measureHashMap = (HashMap<Integer, Measure>) getIntent().getSerializableExtra("MEASURES_TYPE");
        }

        LinearLayout llMeasureType = (LinearLayout) findViewById(R.id.llMeasureType);
        if (llMeasureType != null) {
            addMetDate(llMeasureType);
            materialEditTexts = new ArrayList<>();
            TextView tvMeasureType;
            MaterialEditText materialEditText;
            for (Measure measure : measureHashMap.values()) {
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

                llMeasureType.addView(materialEditText);
                materialEditTexts.add(materialEditText);
            }
        }

        final DatePickerDialog.OnDateSetListener measurementActivity = this;

        Calendar now = Calendar.getInstance();
        String dateNow = now.get(Calendar.DAY_OF_MONTH) +"/"+ (now.get(Calendar.MONTH)+1) + "/" +
                now.get(Calendar.YEAR);
        if (metDate != null) {
            metDate.setText(dateNow);
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

    private void addMetDate(LinearLayout llMeasureType) {
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

        llMeasureType.addView(metDate);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_measurement, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.finish) {
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
                List<Measurement> measurementList = new ArrayList<>();
                measurement = new Measurement();
                measurement.setDate(chosenDate.getTime().getTime());
                measurement.setMeasureId((int) metMeasure.getTag());
                measurement.setValue(Float.valueOf(metMeasure.getText().toString()));
                measurementList.add(measurement);
                measurementHashMap.put(measurement.getMeasure_id(), measurementList);
            }
            if (!measurementHashMap.isEmpty()) {
                Intent intent = new Intent();
                intent.putExtra("NEW_MEASUREMENTS", (Serializable) measurementHashMap);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                Toast.makeText(this, "Nenhuma medição foi incluída", Toast.LENGTH_LONG).show();
            }
            return true;
        } else if (id == android.R.id.home) {
            setResult(RESULT_CANCELED);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        finish();
        super.onBackPressed();
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        String chosenDate = dayOfMonth+"/"+(monthOfYear+1)+"/"+year;
        if (metDate != null) metDate.setText(chosenDate);
    }
}
