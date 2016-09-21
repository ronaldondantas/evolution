package br.com.nadod.evolution;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.model.Point;
import com.db.chart.view.AxisController;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.CircEase;

public class ChartsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        float[] values = new float[2];
        values[0] = (float) 101.9;
        values[1] = (float) 99.8;

        String[] labels = new String[2];
        labels[0] = "10/08";
        labels[1] = "10/09";

        LineSet lineSet = new LineSet(labels, values);

        lineSet.addPoint(new Point("10/10", (float) 108.2));
        lineSet.addPoint(new Point("10/11", (float) 91.78));

        lineSet.setSmooth(true);
        lineSet.setThickness(Tools.fromDpToPx((float) 3.0));
        lineSet.setDotsRadius(Tools.fromDpToPx((float) 6.0));
        lineSet.setColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        lineSet.setDotsColor(Color.parseColor("#FFFFFF"));
        lineSet.setDotsStrokeColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

        LineChartView lineChartView = (LineChartView) findViewById(R.id.weightChart);
        if (lineChartView != null) {
            lineChartView.addData(lineSet);
            lineChartView.setAxisBorderValues(90, 112);
            lineChartView.setYAxis(false);
            lineChartView.setXAxis(false);
            Paint gridPaint = new Paint();
            gridPaint.setColor(Color.parseColor("#BDBDBD"));
            gridPaint.setStyle(Paint.Style.STROKE);
            gridPaint.setAntiAlias(true);
            gridPaint.setStrokeWidth(Tools.fromDpToPx((float) 1.0));
            lineChartView.setGrid(ChartView.GridType.HORIZONTAL, gridPaint);

            // Animation customization
            Animation anim = new Animation();
            anim.setEasing(new CircEase());
            anim.setAlpha(1);
            anim.setStartPoint((float) 0.5, (float) 0.5);

            lineChartView.show();
        }

        lineChartView = (LineChartView) findViewById(R.id.waistChart);
        if (lineChartView != null) {
            lineChartView.addData(lineSet);
            lineChartView.setAxisBorderValues(90, 112);
            lineChartView.setYAxis(false);
            lineChartView.setXAxis(false);
            Paint gridPaint = new Paint();
            gridPaint.setColor(Color.parseColor("#BDBDBD"));
            gridPaint.setStyle(Paint.Style.STROKE);
            gridPaint.setAntiAlias(true);
            gridPaint.setStrokeWidth(Tools.fromDpToPx((float) 1.0));
            lineChartView.setGrid(ChartView.GridType.HORIZONTAL, gridPaint);

            // Animation customization
            Animation anim = new Animation();
            anim.setEasing(new CircEase());
            anim.setAlpha(1);
            anim.setStartPoint((float) 0.5, (float) 0.5);

            lineChartView.show();
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
