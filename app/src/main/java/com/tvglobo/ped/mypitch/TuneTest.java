package com.tvglobo.ped.mypitch;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.Arrays;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

public class TuneTest extends AppCompatActivity {
    AudioDispatcher dispatcher = null;

    String[] notes = {"C2",   "C#2",  "D2",   "D#2",  "E2",   "F2",   "F#2",  "G2",  "G#2",    "A2",    "A#2",   "B2",    "C3",    "C#3",   "D3",    "D#3",   "E3",    "F3",    "F#3",   "G3",    "G#3",   "A3",    "A#3",   "B3",    "C4",    "C#4",   "D4","D#4","E4","F4","F#4","G4","G#4","A4","A#4","B4","C5","C#5","D5","D#5","E5","F5","F#5","G5","G#5","A5","A#5","B5"};
    Float[] freqs = { 65.41f, 69.30f, 73.42f, 77.78f, 82.41f, 87.31f, 92.50f, 98.00f, 103.83f, 110.00f, 116.54f, 123.47f, 130.81f, 138.59f, 146.83f, 155.56f, 164.81f, 174.61f, 185.00f, 196.00f, 207.65f, 220.00f, 233.08f, 246.94f, 261.63f, 277.18f, 293.66f, 311.13f, 329.63f, 349.23f, 369.99f, 392.00f, 415.30f, 440.00f, 466.16f, 493.88f, 523.25f, 554.37f, 587.33f, 622.25f, 659.25f, 698.46f, 739.99f, 783.99f, 830.61f, 880.00f, 932.33f, 987.77f};
    TextView pitchText;
    TextView noteText;

    private static final String TAG = "TuneTest";
    private LineChart mChart;
    private Thread thread;
    private boolean plotData = true;

    private String noteToTest;
    private int noteIndex;
    private float noteFreq;
    private float noteMaxFreq;
    private float noteMinFreq;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tune_test);


        noteText = (TextView) findViewById(R.id.text_view_note);
        pitchText = (TextView) findViewById(R.id.text_view_pitch);

        Intent intent = getIntent();
        noteToTest = intent.getStringExtra(MainActivity.NOTE_PITCH);
        //noteIndex = Arrays.binarySearch(notes, noteToTest);
        noteIndex = -1;
        for (int i = 0; i < notes.length; ++i) {
            if (noteToTest.equals(notes[i])) {
                noteIndex = i;
                break;
            }
        }
        noteFreq = freqs[noteIndex];
        int range = 4;
        int maxIndex = (noteIndex + range < freqs.length ? noteIndex + range : freqs.length - 1);
        noteMaxFreq = freqs[maxIndex];
        int minIndex = (noteIndex - range >= 0 ? noteIndex - range : 0);
        noteMinFreq = freqs[minIndex];
        noteText.setText(noteToTest + " " + freqs[noteIndex] + " " + noteMinFreq + " " + noteMaxFreq);

        onCreateChart();
        getPitch();
    }
    protected void addLimitLine(float freq, String name, int color) {
        LimitLine ll = new LimitLine(freq, name);
        ll.setLineWidth(1f);
        //ll.enableDashedLine(10f, 10f, 0f);
        ll.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        ll.setTextSize(10f);
        ll.setLineColor(getApplicationContext().getResources().getColor(color));
        mChart.getAxisLeft().addLimitLine(ll);
    }
    protected void onCreateChart() {

        mChart = (LineChart) findViewById(R.id.chart1);

        // enable description text
        mChart.getDescription().setEnabled(true);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        mChart.setBackgroundColor(Color.WHITE);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        mChart.setData(data);

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        XAxis xl = mChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setDrawGridLines(false);
        leftAxis.setAxisMaximum(noteMaxFreq);
        leftAxis.setAxisMinimum(noteMinFreq);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

        mChart.getAxisLeft().setDrawGridLines(false);
        mChart.getXAxis().setDrawGridLines(false);
        mChart.setDrawBorders(false);


        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
        addLimitLine(freqs[noteIndex-1], notes[noteIndex-1],R.color.colorBad );
        //addLimitLine((noteFreq+freqs[noteIndex-1])/2, "limit",R.color.colorBad );
        addLimitLine(noteFreq, noteToTest, R.color.colorGood);
        //addLimitLine((noteFreq+freqs[noteIndex+1])/2, "limit", R.color.colorBad);
        addLimitLine(freqs[noteIndex+1], notes[noteIndex+1], R.color.colorBad);

        feedMultiple();
    }

    private void addValue(float value) {

        LineData data = mChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

//            data.addEntry(new Entry(set.getEntryCount(), (float) (Math.random() * 80) + 10f), 0);
            data.addEntry(new Entry(set.getEntryCount(), value), 0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(150);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            mChart.moveViewToX(data.getEntryCount());

            YAxis leftAxis = mChart.getAxisLeft();
            leftAxis.setAxisMaximum(noteMaxFreq >  value ? noteMaxFreq : value);
            leftAxis.setAxisMinimum(noteMinFreq < value ? noteMinFreq : value);

        }
    }


    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Dynamic Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setLineWidth(3f);
        set.setColor(Color.MAGENTA);
        set.setHighlightEnabled(false);
        set.setDrawValues(false);
        set.setDrawCircles(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        return set;
    }

    private void feedMultiple() {

        if (thread != null){
            thread.interrupt();
        }

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (true){
                    plotData = true;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (thread != null) {
            thread.interrupt();
        }
    }

    public final void onNoteChanged(float value) {
        if(plotData){
            addValue(value);
            plotData = false;
        }
    }


    @Override
    protected void onDestroy() {
        thread.interrupt();
        super.onDestroy();
    }

    private void getPitch() {
        dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050,1024,0);

        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult res, AudioEvent e){
                final float pitchInHz = res.getPitch();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        processPitch(pitchInHz);
                    }
                });
            }
        };
        AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
        dispatcher.addAudioProcessor(pitchProcessor);

        Thread audioThread = new Thread(dispatcher, "Audio Thread");
        audioThread.start();
    }
    float mFreq = 0;
    private String searchNote(float freq) {
        if (freq < 63.575 || freq > 1017.135)
            return "";

        mFreq = freq;
        for (int i = 1; i < freqs.length-1; ++i) {
            if(freq >= (freqs[i-1] + freqs[i])/2 && freq <= (freqs[i] + freqs[i+1])/2) {
                return notes[i];
            }
        }

        int index = 1;
        if (freq >= 63.575 && freq <= (freqs[index] + freqs[index+1])/2)
            return notes[0];
        index = freqs.length-1;
        if (freq >= (freqs[index-1] + freqs[index])/2 && freq <= 1017.135)
            return notes[index];
        return "";
    }
    public void processPitch(float pitchInHz) {

        pitchText.setText("" + pitchInHz);

        String note = searchNote(pitchInHz);
        if (!note.isEmpty()) {
            noteText.setText(note);
            addValue(mFreq);
        }
    }
}
