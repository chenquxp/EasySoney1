package com.chenqu.toolbox.easysoney;

import android.support.v7.app.AppCompatActivity;
import android.os.*;
import android.app.*;
import android.view.*;
import android.widget.*;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.content.*;

import java.util.*;
import java.text.*;

import com.chenqu.toolbox.easysoney.PriceMonitorService.*;

public class EasySoneyActivity extends AppCompatActivity implements View.OnClickListener {

    private Intent mIntentService;
    private Button mBGetCurrentData;
    private Button mBSave;
    private Button mBPredict;
    private Button mBSaveTarget;
    private Button mBLoadTarget;
    private Button mBCalcMargin;
    private EditText mETCurrentPrice;
    private EditText mETCurrentTime;
    private EditText mETLastNet;
    private EditText mETLastNetDate;
    private EditText mETLastTargetPrice;
    private EditText mETTargetCurrentTime;
    private EditText mETTargetCurrentPrice;
    private EditText mETMargin;
    private EditText mETUSDCNYIncrease;
    private ToggleButton mTBAuto;
    private TextView mTVIntivalSeconds;
    private TextView mTVCount;
    private SeekBar mSBIntivalSeconds;
    private Integer intivalseconds = 60;
    private SharedPreferences read;
    private SharedPreferences.Editor editor;
    private boolean isBind = false;
    private PriceMonitorService mPriceMonitorService;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle data = intent.getExtras();
            String exval = data.getString("exvalue");
            String netval = data.getString("netvalue");
            String tarval = data.getString("tarvalue");
            String feval = data.getString("fevalue");
            Integer count = data.getInt("timercount");
            PriceMonitorService.ESData esdata = mPriceMonitorService.new ESData(exval, netval, tarval, feval, count);
            mPriceMonitorService.WriteFile("records.txt", "Activity Write Test\n");
            //  LogESData(esdata, "A");
            UpdateUI(esdata, false);
            //  SendESNotify(esdata);
        }
    };
    private DialogInterface.OnClickListener exitdialoglistener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序
                    finish();
                    break;
                case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框
                    moveTaskToBack(true);
                    break;
                default:
                    break;
            }
        }
    };

    private ServiceConnection sconn = new ServiceConnection() {
        public void onServiceDisconnected(ComponentName name) {
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            mPriceMonitorService = ((MyBinder) service).getMyService(); //获取Myservice对象
            mPriceMonitorService.setMainActivity(EasySoneyActivity.this); //把当前对象传递给myservice
            isBind = true;
        }
    };
    private OnSeekBarChangeListener seekListener = new OnSeekBarChangeListener() {
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (progress == 100) {
                intivalseconds = 3600;
            } else {
                intivalseconds = 30 + 30 * progress;
            }

            mTVIntivalSeconds.setText("AutoGet Intival:" + intivalseconds.toString() + "Seconds");

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_easy_soney);
        mBGetCurrentData = (Button) findViewById(R.id.bt_getcurrdata);
        mBGetCurrentData.setOnClickListener(this);
        mBSave = (Button) findViewById(R.id.bt_savenet);
        mBSave.setOnClickListener(this);
        mBPredict = (Button) findViewById(R.id.bt_predict);
        mBPredict.setOnClickListener(this);
        mBLoadTarget = (Button) findViewById(R.id.bt_load_target);
        mBLoadTarget.setOnClickListener(this);
        mBSaveTarget = (Button) findViewById(R.id.bt_save_target);
        mBSaveTarget.setOnClickListener(this);
        mBCalcMargin = (Button) findViewById(R.id.bt_calc_margin);
        mBCalcMargin.setOnClickListener(this);
        mTBAuto = (ToggleButton) findViewById(R.id.tb_auto);
        mTBAuto.setOnClickListener(this);

        mTVIntivalSeconds = (TextView) findViewById(R.id.tv_intival_seconds);
        mTVCount = (TextView) findViewById(R.id.tv_count);
        mSBIntivalSeconds = (SeekBar) findViewById(R.id.sb_intival_seconds);
        mTVIntivalSeconds.setText("AutoGet Intival:" + intivalseconds.toString() + "Seconds");
        mSBIntivalSeconds.setOnSeekBarChangeListener(seekListener);

        mETCurrentPrice = (EditText) findViewById(R.id.et_current_price);
        mETCurrentTime = (EditText) findViewById(R.id.et_current_time);
        mETLastNet = (EditText) findViewById(R.id.et_lastday_net);
        mETLastNetDate = (EditText) findViewById(R.id.et_last_net_date);
        mETLastTargetPrice = (EditText) findViewById(R.id.et_last_target_price);
        mETTargetCurrentTime = (EditText) findViewById(R.id.et_target_current_time);
        mETTargetCurrentPrice = (EditText) findViewById(R.id.et_target_current_price);
        mETMargin = (EditText) findViewById(R.id.et_margin);
        mETUSDCNYIncrease = (EditText) findViewById(R.id.et_usdcny_increase);
        read = getSharedPreferences("pxmlfile", MODE_PRIVATE);
        editor = read.edit();
     /*  editor.putString("net2017-01-25",true "1.2609");
        editor.putString("target2017-01-25", "23049.12");
        editor.commit(); */
        mBGetCurrentData.setFocusable(true);
        mBGetCurrentData.setFocusableInTouchMode(true);
        mBGetCurrentData.requestFocus();
        mBGetCurrentData.requestFocusFromTouch();
        try {
            registerReceiver();
            mIntentService = new Intent(EasySoneyActivity.this, PriceMonitorService.class);
            bindService(mIntentService, sconn, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBind) {
            unbindService(sconn);
        }
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.easy_soney_mainmenu, menu);
        menu.findItem(R.id.autosave_item).setChecked(read.getBoolean("isautosave", false));
        menu.findItem(R.id.autoweekend_item).setChecked(read.getBoolean("isautoweekend", false));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(this, ShowRecentDataActivity.class);
        Bundle bundle = new Bundle();
        switch (item.getItemId()) {
            case R.id.autosave_item:
                item.setChecked(!item.isChecked());
                editor.putBoolean("isautosave", item.isChecked());
                editor.commit();
                break;
            case R.id.autoweekend_item:
                item.setChecked(!item.isChecked());
                editor.putBoolean("isautoweekend", item.isChecked());
                editor.commit();
                break;
            case R.id.show_recent_data_item:
                //携带数据
                String starget = "";
                String snet = "";
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date dt = format.parse(mETLastNetDate.getText().toString());
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(dt);
                    String t;
                    t = format.format(dt);
                    starget += "target" + t + ":" + read.getString("target" + t, "") + "\n";
                    snet += "predict" + t + "+1:" + read.getString("predict" + t + "+1", "") + "\n";
                    snet += "net" + t + ":" + read.getString("net" + t, "") + "\n";

                    for (int i = 8; i >= 0; i--) {
                        calendar.add(Calendar.DATE, -1);
                        dt = calendar.getTime();
                        t = format.format(dt);
                        starget += "target" + t + ":" + read.getString("target" + t, "") + "\n";
                        snet += "predict" + t + "+1:" + read.getString("predict" + t + "+1", "") + "\n";
                        snet += "net" + t + ":" + read.getString("net" + t, "") + "\n";
                    }
                    bundle.putString("text", starget + "\n" + snet);
                    bundle.putBoolean("isdelete", false);
                    //把附加的数据放到意图当中
                    intent.putExtras(bundle);
                    //执行意图
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    OutputMessage(e.toString());
                }
                break;
            case R.id.show_records_item:
                bundle.putString("text", mPriceMonitorService.ReadFile("records.txt"));
                bundle.putBoolean("isdelete", true);
                bundle.putString("filename", "records.txt");

                //把附加的数据放到意图当中
                intent.putExtras(bundle);
                //执行意图
                startActivity(intent);
                break;
            case R.id.exit_item:
                finish();
                break;
            default:
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog isExit = new AlertDialog.Builder(this).create();
            isExit.setTitle("系统提示");
            isExit.setMessage("确定要退出吗,点击取消转后台运行");
            isExit.setButton("确定", exitdialoglistener);
            isExit.setButton2("取消", exitdialoglistener);
            // 显示对话框
            isExit.show();
        }
        return false;
    }

    public void OutputMessage(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View v) {
        String date = mETLastNetDate.getText().toString().trim();
        String net = mETLastNet.getText().toString().trim();
        String target = mETLastTargetPrice.getText().toString().trim();
        String value = "";
        switch (v.getId()) {
            case R.id.bt_getcurrdata:
                ClearData();
                if (isBind) {
                    mPriceMonitorService.RefreshOnce();
                }
                //     mIntentService = new Intent(EasySoneyActivity.this, PriceMonitorService.class);
                //     mIntentService.putExtra("intival", intivalseconds);
                //     mIntentService.putExtra("once", true);
                //     boolean b = bindService(mIntentService, sconn, Context.BIND_AUTO_CREATE);
                //    isBind = true;
                break;
            case R.id.bt_savenet:
                editor.putString("net" + date, net);
                editor.commit();
                OutputMessage("save complete net" + date + " " + net);
                break;
            case R.id.bt_predict:
                SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Shanghai"));
                String retStrFormatNowDate = sdFormatter.format(cal.getTime());
                Double dnewnet = mPriceMonitorService.PredictNewNet(mETLastNet.getText().toString(), mETLastTargetPrice.getText().toString(),
                        mETTargetCurrentPrice.getText().toString(), mETUSDCNYIncrease.getText().toString());
                int len = dnewnet.toString().length();
                if (len > 6) len = 6;
                editor.putString("predict" + date + "+1", dnewnet.toString().substring(0, len) + " (" + retStrFormatNowDate + ")");
                editor.commit();
                OutputMessage("save predict net" + date + "+1:" + dnewnet.toString().substring(0, len));
                break;
            case R.id.bt_save_target:
                editor.putString("target" + date, target);
                editor.commit();
                OutputMessage("save complete target" + date + " " + target);
                break;
            case R.id.bt_load_target:
                // value = read.getString("target" + date, "");
                // OutputMessage("saved data loaded：" + value);
                // mETLastTargetPrice.setText(value);
                break;
            case R.id.bt_calc_margin:
                Double dmargin = mPriceMonitorService.CalcMarginPercent(mETLastNet.getText().toString(), mETLastTargetPrice.getText().toString(),
                        mETTargetCurrentPrice.getText().toString(), mETCurrentPrice.getText().toString());
                mETMargin.setText(dmargin.toString().substring(0, 5) + "%");
                break;
            case R.id.tb_auto:
                if (mTBAuto.isChecked()) {
                    mSBIntivalSeconds.setEnabled(false);
                    if (isBind) {
                        mPriceMonitorService.RefreshContinous(intivalseconds);
                    }
                    //    mIntentService = new Intent(this, PriceMonitorService.class);
                    //    mIntentService.putExtra("intival", intivalseconds);
                    //    mIntentService.putExtra("once", false);
                    //    mIntentService.putExtra("stop", false);
                    //bindService....
                } else {
                    mSBIntivalSeconds.setEnabled(true);
                    if (isBind) {
                        mPriceMonitorService.StopContinous();
                    }
                }
                break;
        }
    }


    void ClearData() {
        mETCurrentPrice.setText("");
        mETCurrentTime.setText("");
        mETLastNet.setText("");
        mETLastNetDate.setText("");
        mETLastTargetPrice.setText("");
        mETTargetCurrentTime.setText("");
        mETTargetCurrentPrice.setText("");
        mETMargin.setText("");
        mETUSDCNYIncrease.setText("");
    }

    public void UpdateUI(ESData d, boolean ismute) {
        try {
            String smargin = (d.mDMarginPercent.toString().substring(0, 5) + "%");
            String sfeincrease = (d.mDForeignExchangeIncreasePercent.toString().substring(0, 5) + "%");
            mTVCount.setText(d.msCount);
            mETCurrentPrice.setText(d.msCurrentPrice);
            mETCurrentTime.setText(d.msCurrentTime);
            mETLastNet.setText(d.msLastNet);
            mETLastNetDate.setText(d.msLastNetDate);
            mETLastTargetPrice.setText(d.msLastTargetPrice);
            mETTargetCurrentTime.setText(d.msTargetCurrentTime);
            mETTargetCurrentPrice.setText(d.msTargetCurrentPrice);
            mETMargin.setText(smargin);
            mETUSDCNYIncrease.setText(sfeincrease);
            mETLastTargetPrice.setTextColor(d.miLastTargetPriceTextColor);
            mETLastNet.setTextColor(d.miLastdayNetTextColor);
            if (!ismute) {
                OutputMessage(d.errmsg);
            }
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    private void registerReceiver() {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.chenqu.toolbox.easysoney.PriceMonitorService");
        EasySoneyActivity.this.registerReceiver(mReceiver, intentFilter);
    }


}
