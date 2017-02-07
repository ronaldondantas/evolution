package br.com.nadod.evolution.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;

import com.astuetz.PagerSlidingTabStrip;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.nadod.evolution.R;
import br.com.nadod.evolution.adapter.SmartFragmentStatePagerAdapter;
import br.com.nadod.evolution.fragment.ChartFragment;
import br.com.nadod.evolution.fragment.MeasurementListFragment;
import br.com.nadod.evolution.model.Measure;
import br.com.nadod.evolution.model.MeasureDAO;
import br.com.nadod.evolution.model.Measurement;
import br.com.nadod.evolution.model.MeasurementDAO;
import br.com.nadod.evolution.model.MeasurementToList;
import br.com.nadod.evolution.utils.Utils;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity
        implements ChartFragment.OnChartInteractionListener,
        MeasurementListFragment.OnMeasurementListInteractionListener {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private static int ADD_MEASUREMENT = 0;

    private SmartFragmentStatePagerAdapter adapterViewPager;

    private InterstitialAd mInterstitialAd;

    List<String> measuresType = new ArrayList<>();
    HashMap<Integer, Measure> measureHashMap = new HashMap<>();
    Map<Integer, List<Measurement>> measurementHashMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        ViewPager vpPager = (ViewPager) findViewById(R.id.vpPager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager(), measuresType,
                measureHashMap, measurementHashMap);

        // Attach the view pager to the tab strip
        if (vpPager != null) {
            vpPager.setAdapter(adapterViewPager);

            PagerSlidingTabStrip tabsStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
            if (tabsStrip != null) tabsStrip.setViewPager(vpPager);
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
    public void onChartInteraction(HashMap<Integer, Measure> measureHashMap) {
        showInterstitial();
        Intent intent = new Intent(this, MeasurementActivity.class);
        intent.putExtra(Utils.MEASURE_TYPE, measureHashMap);
        startActivityForResult(intent, ADD_MEASUREMENT);
    }

    public void measurementClicked(MeasurementToList measurement) {
        Intent intent = new Intent(this, MeasurementActivity.class);
        intent.putExtra(Utils.MEASUREMENT_DATA, measurement);
        intent.putExtra(Utils.TITLE_EDIT_MEASUREMENT, "Editar medição");
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            HashMap<Integer, List<Measurement>> measurementHashMap = new HashMap<>();
            if (requestCode == 1) {
                boolean hasChanges = data.getBooleanExtra(Utils.HAS_CHANGES, false);
                if (hasChanges) {
                    MeasurementDAO measurementDAO = new MeasurementDAO(this);
                    for (Integer measureId : measureHashMap.keySet()) {
                        List<Measurement> measurements = measurementDAO.selectAllByMeasure(measureId);
                        if (!measurements.isEmpty()) measurementHashMap.put(measureId, measurements);
                    }
                }
            } else {
                measurementHashMap =
                        (HashMap<Integer, List<Measurement>>) data.getSerializableExtra(Utils.NEW_MEASUREMENT_LIST);
            }

            boolean hasNewMeasurement = requestCode == ADD_MEASUREMENT;
            if (!hasNewMeasurement) this.measurementHashMap.clear();
            for (Integer measureId : measurementHashMap.keySet()) {
                List<Measurement> totalMeasurement  = new ArrayList<>();
                if (hasNewMeasurement && this.measurementHashMap.get(measureId) != null &&
                        !this.measurementHashMap.get(measureId).isEmpty()) {
                    totalMeasurement.addAll(this.measurementHashMap.get(measureId));
                }
                totalMeasurement.addAll(measurementHashMap.get(measureId));
                this.measurementHashMap.put(measureId, totalMeasurement);
            }

            ChartFragment chartFragment = (ChartFragment) adapterViewPager.getRegisteredFragment(0);
            chartFragment.afterCrudMeasurement();

            MeasurementListFragment measurementListFragment =
                    (MeasurementListFragment) adapterViewPager.getRegisteredFragment(1);
            measurementListFragment.afterCrudMeasurement(hasNewMeasurement ||
                    data.getBooleanExtra(Utils.HAS_CHANGES, false));

            adapterViewPager.notifyDataSetChanged();
        }
    }

    // Extend from SmartFragmentStatePagerAdapter now instead for more dynamic ViewPager items
    public static class MyPagerAdapter extends SmartFragmentStatePagerAdapter {
        private static int NUM_ITEMS = 2;

        List<String> measuresType = new ArrayList<>();
        HashMap<Integer, Measure> measureHashMap = new HashMap<>();
        Map<Integer, List<Measurement>> measurementHashMap = new HashMap<>();

        MyPagerAdapter(FragmentManager fragmentManager, List<String> measuresType,
                       HashMap<Integer, Measure> measureHashMap,
                       Map<Integer, List<Measurement>> measurementHashMap) {
            super(fragmentManager);
            this.measuresType = measuresType;
            this.measureHashMap = measureHashMap;
            this.measurementHashMap = measurementHashMap;
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return ChartFragment.newInstance(measuresType, measureHashMap,
                            measurementHashMap);
                case 1:
                    return MeasurementListFragment.newInstance(measuresType, measureHashMap,
                            measurementHashMap);
                default:
                    return null;
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return title(position);
        }

        private String title (int position) {
            return position == 0 ? "GRÁFICO" : "MEDIÇÕES";
        }

    }
}
