package com.slk.androidaudio;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.slk.androidaudio.fsk.ErrorDetection;
import com.slk.androidaudio.fsk.FSKModule;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class SenderActivity extends Activity {

    EditText msgEditText;
    Button btnOK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sender);

        msgEditText = (EditText) findViewById(R.id.msgEditText);
        btnOK = (Button) findViewById(R.id.buttonSend);

        btnOK.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                write(Integer.parseInt(msgEditText.getText().toString()));
            }
        });
    }

    public void write(int message) {

        encodeMessage(message);

        // TODO Alternative sending data
        //byte[] data = encodeData(message);
        //send(data);
    }

    private static final int AUDIO_SAMPLE_FREQ = 44100;

    public void send(byte[] bytes_pkg) {
        int bufsize = AudioTrack.getMinBufferSize(AUDIO_SAMPLE_FREQ,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        AudioTrack trackplayer = new AudioTrack(AudioManager.STREAM_MUSIC,
                AUDIO_SAMPLE_FREQ, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufsize,
                AudioTrack.MODE_STREAM);
        trackplayer.play();
        trackplayer.write(bytes_pkg, 0, bytes_pkg.length);
    }

    private byte[] encodeData(int data) {
        // error detection encoding
        Log.i("TAG", "encodeMessage() value=" + data);
        data = ErrorDetection.createMessage(data);
        Log.i("TAG", "encodeMessage() message=" + data);
        // sound encoding
        double[] sound = FSKModule.encode(data);

        ByteBuffer buf = ByteBuffer.allocate(4 * sound.length);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < sound.length; i++) {
            int yInt = (int) sound[i];
            buf.putInt(yInt);
            if (yInt != 0) {
                Log.d("sender", "index = " + i + " value = " + yInt);
            }
        }
        byte[] tone = buf.array();

        return tone;
    }

    private void encodeMessage(int value) {
        // audio initialization
        int AUDIO_BUFFER_SIZE = 4096;
        int minBufferSize = AudioTrack.getMinBufferSize(AUDIO_SAMPLE_FREQ,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        if (AUDIO_BUFFER_SIZE < minBufferSize)
            AUDIO_BUFFER_SIZE = minBufferSize;
        AudioTrack aT = new AudioTrack(AudioManager.STREAM_MUSIC,
                AUDIO_SAMPLE_FREQ, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, AUDIO_BUFFER_SIZE,
                AudioTrack.MODE_STREAM);
        aT.play();

        byte[] tone = encodeData(value);

        // play message
        int nBytes = aT.write(tone, 0, tone.length);
        aT.stop();
        aT.release();
    }

}
