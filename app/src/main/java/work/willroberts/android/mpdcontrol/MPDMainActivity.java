package work.willroberts.android.mpdcontrol;

import android.content.Context;
import android.os.*;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import android.content.SharedPreferences;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import static android.widget.Toast.*;


public class MPDMainActivity extends ActionBarActivity {

    private Socket socket = null;
    private static final String PLAY_CMD = "play";
    private static final String STOP_CMD = "stop";
    private static final String NEXT_CMD = "next";
    private static final String RANON_CMD = "random 1";
    private static final String RANOFF_CMD = "random 0";
    private static final String SINGON_CMD = "single 1";
    private static final String SINGOFF_CMD = "single 0";
    private static final String VOLUP_CMD = "setvol 88";
    private static final String VOLDN_CMD = "setvol 75";
    private static final String PAUSEON_CMD = "pause 1";
    private static final String PAUSEOFF_CMD = "pause 0";
    private static final String STATUS_CMD = "status";
    private static final String TAG = "MPDControl-";
    private String serverIP;
    private String volume = "75";
    private String str = "";
    private Integer serverPort;
    private Boolean mIsConnected = false;
    private String returnData; // Response from MPD
    private String newData; // Response from MPD

    final Context context = this;
    RelativeLayout cnct;
    RelativeLayout info;
    RelativeLayout setCnct;
    LinearLayout mainLayout;
    View promptsView;
    View infoView;
    View playlistview;
    View tempview;
    TextView tvInData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mpdmain);
        connect();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mpdmain, menu);
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
            setCnct = (RelativeLayout)findViewById(R.id.relLayout);
            setCnct.removeAllViews();
            connect();
        }
        if (id == R.id.action_load) {

            try {
                String str1="";
                StringBuffer buf = new StringBuffer();
                final ArrayList<String> list = new ArrayList<String>();
                InputStream is = this.getResources().openRawResource(R.raw.playlists);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                if (is!=null) {
                    while ((str1 = reader.readLine()) != null) {
                        list.add(str1 + "\n");
                    }
                }
                is.close();
                ListAdapter adpt = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
                mainLayout = (LinearLayout)findViewById(R.id.top_main);
                mainLayout.removeAllViews();
                LayoutInflater lli = LayoutInflater.from(context);
                playlistview = lli.inflate(R.layout.play_list, null);
                mainLayout.addView(playlistview);
                ListView listofplaylists = (ListView) findViewById(R.id.playlists_list);
                listofplaylists.setAdapter(adpt);

                listofplaylists.setOnItemClickListener(
                        new AdapterView.OnItemClickListener() {

                            @Override
                            public void onItemClick(AdapterView<?> parent, final View view,
                                                    int position, long id) {
                                final String item = (String) parent.getItemAtPosition(position);
                                // Toast.makeText(MPDMainActivity.this, item, Toast.LENGTH_LONG).show();
                                mainLayout = (LinearLayout)findViewById(R.id.top_main);
                                mainLayout.removeAllViews();
                                LayoutInflater lli = LayoutInflater.from(context);
                                tempview = lli.inflate(R.layout.activity_mpdmain, null);
                                mainLayout.addView(tempview);
                                getPlaylist(item);
                            }

                        });

            } catch (IOException e) {
                e.printStackTrace();
            }

        } //if action_load

        return super.onOptionsItemSelected(item);
    }


    public void onClick(View view) throws IOException, InterruptedException {

            String strButtonTxt = "";


            switch (view.getId()) {
                case R.id.cmdButton1:
                    str = getText(R.string.PLAYCMD).toString();
                    strButtonTxt = getText(R.string.btnPlay).toString();
                    break;
                case R.id.cmdButton3:
                    str = getText(R.string.STOPCMD).toString();
                    strButtonTxt = "STOP";
                    break;
                case R.id.cmdButton2:
                    // str = getText(R.string.NEXTCMD).toString();
                    str = "mpc status";
                    strButtonTxt = "STATUS";
                    // strButtonTxt = "NEXT";
                    break;
                case R.id.btnRandomOn:
                    str = getText(R.string.RANDONCMD).toString();
                    strButtonTxt = "RANDOM ON";
                    break;
                case R.id.btnRandomOff:
                    str = getText(R.string.RANDOFFCMD).toString();
                    strButtonTxt = "RANDOM OFF";
                    break;
                case R.id.btnSingOn:
                    str = getText(R.string.SINGONCND).toString();
                    strButtonTxt = "SINGLE ON";
                    break;
                case R.id.btnSingOff:
                    str = getText(R.string.SINGOFFCND).toString();
                    strButtonTxt = "SINGLE OFF";
                    break;
                case R.id.cmdButton5:
                    volume = setvolume("DN", volume);
                    str = "setvol " + volume;
                    strButtonTxt = "VOLUME " + volume;
                    break;
                case R.id.cmdButton6:
                    volume = setvolume("UP", volume);
                    str = "setvol " + volume;
                    strButtonTxt = "VOLUME " + volume;
                    break;
                case R.id.cmdButton7:
                    str = getText(R.string.PAUSEONCMD).toString();
                    strButtonTxt = "PAUSE ON";
                    break;
                case R.id.cmdButton8:
                    str = getText(R.string.PAUSEOFFCMD).toString();
                    strButtonTxt = "PAUSE OFF";
                    break;
                case R.id.btnClear:
                    str = getText(R.string.CLEARCMD).toString();
                    strButtonTxt = "PLAYLIST CLEAR";
                    break;
                case R.id.btnLoad1:
                    str = getText(R.string.LOAD1CMD).toString();
                    strButtonTxt = "LOAD DRAGNET";
                    break;
                case R.id.btnLoad2:
                    str = getText(R.string.LOAD2CMD).toString();
                    strButtonTxt = "LOAD OMB";
                    break;
                case R.id.btnPlaylist:
                    String mplist = "";
                    getPlaylist(mplist);
                    break;
                case R.id.btnLoad:
                    EditText etPlaylist = (EditText) findViewById(R.id.strPlaylist);
                    String strPlaylist = etPlaylist.getText().toString();
                    if (!strPlaylist.isEmpty()) {
                        strButtonTxt = "LOADING " + strPlaylist;
                        str = "load " + strPlaylist;
                        setCnct = (RelativeLayout)findViewById(R.id.relLayout);
                        setCnct.removeAllViews();
                        info = (RelativeLayout)findViewById(R.id.relLayout);
                        LayoutInflater lli = LayoutInflater.from(context);
                        infoView = lli.inflate(R.layout.info_main, null);
                        info.addView(infoView);
                    } else {
                        makeText(context, "Enter a valid playlist then press Load.", LENGTH_LONG).show();
                    }
                    break;
            }
        /*
        Put call to new thread here
        String str = command to send
        */
            if (!str.equals("")){
                String newData = "";
                Button connect = (Button) findViewById(R.id.cmdConnect);
                connect.setText("Status = " + strButtonTxt);
                Log.i(TAG, "Sending command");
                new Thread(new ClientThread()).start();
                cnct = (RelativeLayout)findViewById(R.id.relLayout);
                cnct.removeAllViews();
                Log.i(TAG, "Removed view");
                LayoutInflater li = LayoutInflater.from(context);
                promptsView = li.inflate(R.layout.in_stream, null);
                cnct.addView(promptsView);
                tvInData = (TextView) findViewById(R.id.inData);
                // newData = "";
                // newData = returnData;

            } else {
                Button connect = (Button) findViewById(R.id.cmdConnect);
                connect.setText("Status = Not Connected! Restart app.");
            }

        /* End replace */
    }

    public void connect(){
        cnct = (RelativeLayout)findViewById(R.id.relLayout);
        LayoutInflater li = LayoutInflater.from(context);
        promptsView = li.inflate(R.layout.connect_params, null);
        cnct.addView(promptsView);
        SharedPreferences settings = getSharedPreferences("MPDControl", 0);
        String strOldHost = settings.getString("server-ip", "");
        Integer intOldPort = settings.getInt("server-port", 6600);
        String volume = settings.getString("volume", "75");
        EditText etAddress = (EditText) findViewById(R.id.etHostIP);
        EditText etPort = (EditText) findViewById(R.id.etHostPort);
        etAddress.setText(strOldHost);
        etPort.setText(intOldPort.toString());
    }

    public void onSave(View view) {
        EditText etAddress = (EditText) findViewById(R.id.etHostIP);
        EditText etPort = (EditText) findViewById(R.id.etHostPort);
        serverIP = etAddress.getText().toString();
        serverPort = Integer.parseInt(etPort.getText().toString());
        if (!serverIP.equals("") && !serverPort.equals(null)) {
            cnct.removeView(promptsView);
            info = (RelativeLayout) findViewById(R.id.relLayout);
            LayoutInflater lli = LayoutInflater.from(context);
            infoView = lli.inflate(R.layout.info_main, null);
            info.addView(infoView);
            SharedPreferences settings = getSharedPreferences("MPDControl", 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("server-ip", serverIP);
            editor.putInt("server-port", serverPort);
            editor.putBoolean("params-ok", true);
            editor.apply();
           // str = "mpc";
            Log.i("TcpClient", "Connection data saved");
            // new Thread(new ClientThread()).start();
        } else {
            makeText(context, "Provide values for Host IP and Port then press Save.", LENGTH_LONG).show();
        }

    }

    public String setvolume(String direction, String currvol) {
        String newvolume = currvol;
        if (direction == "UP") {
            /* Increase volume by 5 */
            Integer tempvol = Integer.parseInt(newvolume) + 5;
            newvolume = tempvol.toString();
        }
        if (direction == "DN") {
            /* Decrease volume by 5  */
            Integer tempvol = Integer.parseInt(newvolume) - 5;
            newvolume = tempvol.toString();

        }
        return newvolume;
    }

    public void getPlaylist(String plist) {
        setCnct = (RelativeLayout)findViewById(R.id.relLayout);
        setCnct.removeAllViews();
        LayoutInflater lli = LayoutInflater.from(context);
        infoView = lli.inflate(R.layout.list_entry, null);
        setCnct.addView(infoView);
        if (!plist.isEmpty()) {
            EditText etplist = (EditText) findViewById(R.id.strPlaylist);
            etplist.setText(plist);
        }
    }
    public TextView getResultsTextView() {
        return tvInData;
    }

    class ClientThread implements Runnable {

        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            StringBuilder sb = new StringBuilder();
            String line ="";
            String outMsg = str;
            try {
                // InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                if (!mIsConnected) {
                    socket = new Socket(serverIP, serverPort);
                    mIsConnected = true;
                }

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket
                        .getOutputStream())), true);
                // WHERE YOU ISSUE THE COMMANDS
                // Send command
                out.println(outMsg);
                out.flush();
                Log.i("TcpClient", "sent: " + outMsg);
                // Accept server response
                line = in.readLine();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.i(TAG, "Calling returnResult");
            mIsConnected = false;
            returnResult("\n\r Sent: " + outMsg + "    Return: " + line);
        } //run
        private void returnResult(String result) {

            final String mResult = result;
            Log.v(TAG, "reporting back from the ClientThread");

            tvInData.post(new Runnable() {

                @Override
                public void run() {
                    getResultsTextView().setText(mResult);
                }
            });

        }
    } //ClientThread


}
