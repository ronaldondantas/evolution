package br.com.nadod.evolution.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import br.com.nadod.evolution.R;
import br.com.nadod.evolution.activity.MeasurementListActivity;
import br.com.nadod.evolution.model.Measure;
import br.com.nadod.evolution.model.MeasurementToList;

public class MeasurementAdapter extends RecyclerView.Adapter<MeasurementAdapter.MyViewHolder> {
    private Context context;

    private List<MeasurementToList> measurementList;

    private HashMap<Integer, Measure> measureHashMap;
    public MeasurementAdapter(List<MeasurementToList> measurementList, HashMap<Integer, Measure> measureHashMap,
                              Context context) {
        this.context = context;
        this.measurementList = measurementList;
        this.measureHashMap = measureHashMap;
    }

    public void setMeasurementList(List<MeasurementToList> measurementList) {
        this.measurementList = measurementList;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{
        public TextView tvDate;
        public LinearLayout llMeasureAndResult;
        public ItemClickListener itemClickListener;
        public Context context;

        public void setItemClickListener(ItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        public MyViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);

            context = view.getContext();
            tvDate = (TextView) view.findViewById(R.id.tvDate);
            llMeasureAndResult = (LinearLayout) view.findViewById(R.id.llMeasureAndResult);
        }

        @Override
        public void onClick(View v) {
            this.itemClickListener.onItemClick(v, getLayoutPosition());
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recycler_measurement_list, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.llMeasureAndResult.removeAllViews();
        final MeasurementToList measurement = this.measurementList.get(position);
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyy");
        holder.tvDate.setText(String.valueOf(format.format(measurement.getDate())));

        TextView tvMeasureTitle, tvMeasure, tvResult;
        tvMeasureTitle = new TextView(holder.context);
        tvMeasureTitle.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, (float) 0.4));
        tvMeasureTitle.setTextSize((float) 14);
        tvMeasureTitle.setText(measureHashMap.get(measurement.getMeasure_id())
                .getDescription().toUpperCase());
        holder.llMeasureAndResult.addView(tvMeasureTitle);

        tvMeasure = new TextView(holder.context);
        tvMeasure.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, (float) 0.1));
        tvMeasure.setTextSize((float) 30);
        tvMeasure.setText(String.format("%.1f", measurement.getValue()) + " kg");
        holder.llMeasureAndResult.addView(tvMeasure);

        tvResult = new TextView(holder.context);
        tvResult.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, (float) 0.4));
        tvResult.setGravity(Gravity.END);
        tvResult.setTextSize((float) 18);
        float resultFloat = getResult(position);
        String result = "-";
        if (resultFloat > 0) result = "+" + String.format("%.1f", getResult(position));
        else if (resultFloat < 0) result = String.format("%.1f", getResult(position));
        tvResult.setText(result);
        holder.llMeasureAndResult.addView(tvResult);

        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                if (context instanceof MeasurementListActivity) {
                    ((MeasurementListActivity) context).measurementClicked(measurement);
                }
            }
        });
    }

    private float getResult(int measurementPosition) {
        if (measurementPosition+1 == measurementList.size()) return 0;
        return measurementList.get(measurementPosition).getValue() -
                measurementList.get(measurementPosition+1).getValue();
    }

    @Override
    public int getItemCount() {
        return measurementList.size();
    }

    public interface ItemClickListener {
        void onItemClick(View v, int position);
    }
}
