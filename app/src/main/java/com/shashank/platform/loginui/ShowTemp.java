package com.shashank.platform.loginui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import cn.com.newland.nle_sdk.responseEntity.SensorInfo;
import cn.com.newland.nle_sdk.responseEntity.base.BaseResponseEntity;
import cn.com.newland.nle_sdk.util.NCallBack;
import cn.com.newland.nle_sdk.util.NetWorkBusiness;
import retrofit2.Call;
import retrofit2.Response;

public class ShowTemp extends AppCompatActivity {

    private TempView mDeviceTempHum;
    private int temperature = 21;//当前温度
    private double temperature_d = 21.0;
    private int alarmTemp = 25;//预警温度限值
    private boolean alarmFlag = true;//true则可以弹窗，false则不再弹窗
    private boolean allowCount = false;
    private String deviceID = "41210";
    private NetWorkBusiness netWorkBusiness;
    private String accessToken;
    private int FLAG_MSG = 0x001; //定义发送的消息代码
    private Message message; //声明消息对象
    private Message countMessage;
    private int FLAG_MSG_COUNT = 0x005;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_temp);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Enjoy Your Life.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        // TODO: 2019/9/23 温度显示
        initView();
        refreshData();

        // TODO: 2019/9/23 状态栏设置
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    private void initView() {
        mDeviceTempHum = findViewById(R.id.device_temp_hum);
        Bundle bundle = getIntent().getExtras();
        accessToken = bundle.getString("accessToken");   //获得传输秘钥
        netWorkBusiness = new NetWorkBusiness(accessToken, "http://api.nlecloud.com:80/");   //进行登录连接


    }

    // TODO: 2019/9/24 每隔2分钟刷新温度数据并且显示出来
    private void refreshData() {
        message = Message.obtain();//从消息池获取空消息对象
        message.what = FLAG_MSG;//标识信息，以便用不同的方式处理Message
        handler.sendMessage(message);//立刻发送消息
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == FLAG_MSG) {
                getTemperature();
            }
            message = handler.obtainMessage(FLAG_MSG);//从消息池获取空消息对象，标识为FLAG_MSG
            handler.sendMessageDelayed(message, 2000); // 延时2s发送
        }
    };

    // TODO: 2019/9/24 当点击“稍后提醒”时，开始计时两分钟
    private void refreshCount() {
        countMessage = Message.obtain();//从消息池获取空消息对象
        countMessage.what = FLAG_MSG_COUNT;//标识信息，以便用不同的方式处理Message
        count_handler.sendMessage(countMessage);//立刻发送消息
    }

    @SuppressLint("HandlerLeak")
    private Handler count_handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == FLAG_MSG_COUNT) {
                count = count + 1;
            }
            if((count==12)&&allowCount){//定时2min
                alarmFlag = true;
            }
            countMessage = count_handler.obtainMessage(FLAG_MSG_COUNT);//从消息池获取空消息对象，标识为FLAG_MSG
            count_handler.sendMessageDelayed(countMessage, 10000); // 延时10s发送
        }
    };

    // TODO: 2019/9/24 从云平台获取温度数据，显示在仪表盘上。注意apitTag要和云平台标识名一致
    public void getTemperature() {
/*        netWorkBusiness.getSensor(deviceID, "currentTemp", new NCallBack<BaseResponseEntity<SensorInfo>>() {
            @Override
            public void onResponse(final Call<BaseResponseEntity<SensorInfo>> call, final Response<BaseResponseEntity<SensorInfo>> response) {
                BaseResponseEntity baseResponseEntity = response.body();
                if (baseResponseEntity != null) {
                    //获取到了内容,使用json解析.
                    //JSON 是一种文本形式的数据交换格式，它比XML更轻量、比二进制容易阅读和编写，调式也更加方便;解析和生成的方式很多，Java中最常用的类库有：JSON-Java、Gson、Jackson、FastJson等
                    final Gson gson = new Gson();
                    JSONObject jsonObject;
                    String msg = gson.toJson(baseResponseEntity);
                    try {
                        jsonObject = new JSONObject(msg);   //解析数据.

                        JSONObject resultObj = (JSONObject) jsonObject.get("ResultObj");
                        String TempValue = resultObj.getString("Value");
                        temperature_d = Double.valueOf(TempValue).intValue();
                        temperature = (int) temperature_d;
                        if ((temperature >= alarmTemp) && (alarmFlag)) {
                            dialog();
                        }
                        mDeviceTempHum.setTemp(temperature);//显示温度数据到仪表盘
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            protected void onResponse(BaseResponseEntity<SensorInfo> response) {

            }

            public void onFailure(final Call<BaseResponseEntity<SensorInfo>> call, final Throwable t) {
                Toast.makeText(ShowTemp.this, "Can not get data for now.", Toast.LENGTH_SHORT).show();
            }
        });*/
                mDeviceTempHum.setTemp(21);
                mDeviceTempHum.setHum(25);
    }

    @SuppressLint("SetTextI18n")
    public void dialog() {
        //标题居中
        TextView title = new TextView(this);
        title.setText("Warning!");
        title.setPadding(0, 25, 0, 0);
        title.setGravity(Gravity.CENTER);
        //创建对话框对象
        AlertDialog alertDialog = new AlertDialog.Builder(ShowTemp.this).create();
        alertDialog.setIcon(R.drawable.advise); //设置对话框的图标
        alertDialog.setCustomTitle(title);//设置标题
        //设置要显示的内容
        alertDialog.setMessage("Temperature exceeds limit!");
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel reminder", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alarmFlag = false;
                allowCount = false;
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Remind later", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alarmFlag = false;
                allowCount = true;
                count = 0;//每点击一次稍后提醒就把count置0，重新计数2分钟
                refreshCount();
            }
        });

        alertDialog.show();//显示对话框
        Button mNegativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        Button mPositiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

        LinearLayout.LayoutParams mNegativeButtonLL = (LinearLayout.LayoutParams) mNegativeButton.getLayoutParams();
        mNegativeButtonLL.weight = 1;
        mNegativeButton.setLayoutParams(mNegativeButtonLL);

        LinearLayout.LayoutParams mPositiveButtonLL = (LinearLayout.LayoutParams) mPositiveButton.getLayoutParams();
        mPositiveButtonLL.weight = 1;
        mPositiveButton.setLayoutParams(mPositiveButtonLL);
    }

}
