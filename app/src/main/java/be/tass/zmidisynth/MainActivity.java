package be.tass.zmidisynth;

import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;

import android.os.StrictMode;
import android.widget.TextView;
import be.tass.mididriver.MidiDriver;
import be.tass.mididriver.MidiDriver.OnMidiStartListener;

public class MainActivity extends ActionBarActivity implements View.OnClickListener , OnMidiStartListener {
    private Button btnCConnect;
    private EditText txtCAddress;
    private TextView txtCIncoming;
    protected MidiDriver midi;

    // our handler
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //display each item in a single line
            Bundle b = msg.getData();
            txtCIncoming.append(".");
            if (midi != null)
            {
                midi.write(b.getByteArray("MIDIkey"));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCConnect=(Button)findViewById(R.id.btnConnect);
        btnCConnect.setOnClickListener(this);

        txtCAddress=(EditText)findViewById(R.id.txtAddress);
        txtCIncoming=(TextView)findViewById(R.id.txtIncoming);

        // Create midi driver
        midi = new MidiDriver();

        boolean DEVELOPER_MODE = true;

        if (DEVELOPER_MODE)
        {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .permitNetwork()   // or .detectAll() for all detectable problems
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }

        // Set on midi start listener
        if (midi != null)
            midi.setOnMidiStartListener(this);
    }

    @Override
    public void onClick(View v) {
        btnCConnect.setText("Connecting...");

        String address = txtCAddress.getText().toString();

        // Launch thread to receive
        new Thread(new ZeroMQSubscriber(this.handler, address)).start();

        btnCConnect.setText("Subscribed!");

        return;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Listener for sending initial midi messages when the Sonivox
    // synthesizer has been started, such as program change. Runs on
    // the MidiDriver thread, so should only be used for sending midi
    // messages.
    @Override
    public void onMidiStart()
    {
        // TODO
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // Start midi

        if (midi != null)
            midi.start();
    }

    // On pause

    @Override
    protected void onPause()
    {
        super.onPause();

        // Stop midi

        if (midi != null)
            midi.stop();
    }

}
