package com.chenqu.toolbox.easysoney;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;

import java.io.FileOutputStream;

/**
 * Created by chenqu on 2017/2/3.
 */

public class ShowRecentDataActivity extends Activity implements View.OnClickListener{
    /**
     * Called when the activity is starting.  This is where most initialization
     * should go: calling {@link #setContentView(int)} to inflate the
     * activity's UI, using {@link #findViewById} to programmatically interact
     * with widgets in the UI, calling
     * {@link #managedQuery(Uri, String[], String, String[], String)} to retrieve
     * cursors for data being displayed, etc.
     * <p>
     * <p>You can call {@link #finish} from within this function, in
     * which case onDestroy() will be immediately called without any of the rest
     * of the activity lifecycle ({@link #onStart}, {@link #onResume},
     * {@link #onPause}, etc) executing.
     * <p>
     * <p><em>Derived classes must call through to the super class's
     * implementation of this method.  If they do not, an exception will be
     * thrown.</em></p>
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     * @see #onStart
     * @see #onSaveInstanceState
     * @see #onRestoreInstanceState
     * @see #onPostCreate
     */
    private EditText et_data;
    private Button bt_delete;
    private String filename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_recent_data_layout);
        et_data = (EditText) findViewById(R.id.et_recent_data);
        bt_delete   =(Button)findViewById(R.id.button_delete);
        bt_delete.setOnClickListener(this);

        Bundle bundle = this.getIntent().getExtras();
        et_data.setText(bundle.getString("text"));
        if(!bundle.getBoolean("isdelete",false)){
            bt_delete.setVisibility(View.INVISIBLE);
        }
        filename=bundle.getString("filename","");


    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_delete:
                try {
                    // 步骤2:创建一个FileOutputStream对象,MODE_APPEND追加模式.重写用PRIVATE
                    FileOutputStream fos = openFileOutput(filename,
                            MODE_PRIVATE);
                    byte[] a=new byte[0];
                    // 步骤3：将获取过来的值放入文件
                    fos.write(a);
                    // 步骤4：关闭数据流
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                this.finish();
                break;
        }
    }
}
