package br.com.nadod.evolution.model;

import android.support.annotation.NonNull;

import java.io.Serializable;

public class Measurement implements Serializable, Comparable<Measurement> {
    private int id;
    private int measure_id;
    private float value;
    private long date;

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public int getMeasure_id() {
        return measure_id;
    }

    public void setMeasureId(int type) {
        this.measure_id = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int compareTo(@NonNull Measurement another) {
        if (this.date < another.getDate()) return -1;
        if (this.date > another.getDate()) return 1;
        return 0;
    }
}
