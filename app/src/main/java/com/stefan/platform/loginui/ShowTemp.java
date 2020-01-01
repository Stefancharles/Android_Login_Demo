package com.stefan.platform.loginui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
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
import cn.com.newland.nle_sdk.responseEntity.User;
import cn.com.newland.nle_sdk.responseEntity.base.BaseResponseEntity;
import cn.com.newland.nle_sdk.util.NCallBack;
import cn.com.newland.nle_sdk.util.NetWorkBusiness;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShowTemp extends AppCompatActivity {

    private TempView mDeviceTempHum;
    private int temperature = 21;//当前温度
    private double temperature_d = 21.0;
    private int alarmTemp = 25;//预警温度限值
    private Button GetPastTempdata; //历史数据
    private Button GetPastHumdata;
    private int humidity = 21;//当前温度
    private double humidity_d = 21.0;
    private int alarmHum = 25;
    private boolean alarmFlag = true;//true则可以弹窗，false则不再弹窗
    private boolean allowCount = false;
    private String deviceID = "41209";
    private NetWorkBusiness netWorkBusiness;
    private String accessToken;
    private int FLAG_MSG = 0x001; //定义发送的消息代码
    private Message message; //声明消息对象
    private Message countMessage;
    private int FLAG_MSG_COUNT = 0x005;
    private int count = 0;
    private  Button btn_light, btn_fan;
    boolean light_state = false;
    boolean fan_state = false;

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
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // TODO: 2019-12-30 对风扇和灯进行控制
        btn_light.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!light_state){

                        control(deviceID,"bool_work",1);  // 开灯.


                    btn_light.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.power_on);
                }
                else{

                        control(deviceID,"bool_work",0);  //关灯.

                    btn_light.setCompoundDrawablesWithIntrinsicBounds(0,0,0,R.drawable.power_off);
                }
                light_state = !light_state;
            }
        });

        btn_fan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!fan_state){

                        control(deviceID,"fan",1);   //open fan

                    btn_fan.setCompoundDrawablesWithIntrinsicBounds(0,0,0,R.drawable.power_on);
                }
                else{

                        control(deviceID,"fan",0);   //close fan

                    btn_fan.setCompoundDrawablesWithIntrinsicBounds(0,0,0,R.drawable.power_off);
                }
                fan_state = !fan_state;
            }
        });

        GetPastTempdata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ShowTemp.this,PastdataActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("accessToken",accessToken);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });


        GetPastHumdata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ShowTemp.this,getPastHum.class);
                Bundle bundle = new Bundle();
                bundle.putString("accessToken",accessToken);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });


    }

    private void initView() {
        mDeviceTempHum = findViewById(R.id.device_temp_hum);
        btn_light = findViewById(R.id.btn_light);
        btn_fan = findViewById(R.id.btn_fan);
        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        accessToken = bundle.getString("accessToken");   //获得传输秘钥
        netWorkBusiness = new NetWorkBusiness(accessToken, "http://api.nlecloud.com:80/");   //进行登录连接
        mDeviceTempHum.setTemp(10);
        mDeviceTempHum.setHum(10);
        GetPastTempdata = findViewById(R.id.get_past_temp);
        GetPastHumdata = findViewById(R.id.get_past_hum);
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
                getHumidity();
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
        netWorkBusiness.getSensor(deviceID, "temperature", new NCallBack<BaseResponseEntity<SensorInfo>>() {
            @Override
            public void onResponse(final Call<BaseResponseEntity<SensorInfo>> call, final Response<BaseResponseEntity<SensorInfo>> response) {
                BaseResponseEntity baseResponseEntity = response.body();
                if (baseResponseEntity != null) {
                    final Gson gson = new Gson();
                    JSONObject jsonObject;
                    String msg = gson.toJson(baseResponseEntity);
                    try {
                        jsonObject = new JSONObject(msg);   //解析数据.

                        JSONObject resultObj = (JSONObject) jsonObject.get("ResultObj");
                        String TempValue = resultObj.getString("Value");
                        temperature_d = Double.valueOf(TempValue).intValue();
                        temperature = (int) temperature_d;
                        //if ((temperature >= alarmTemp) && (alarmFlag)) {
                        //    dialog();
                        //}
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
                Toast.makeText(ShowTemp.this, "Can not get temperature data for now.", Toast.LENGTH_SHORT).show();
            }
        });
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

    public void control(String id,String apiTag,Object value){
        //设备id,标识符,值.
        netWorkBusiness.control(id, apiTag, value, new Callback<BaseResponseEntity>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponseEntity> call, @NonNull Response<BaseResponseEntity> response) {
                BaseResponseEntity<User> baseResponseEntity = response.body();  //获得返回体
                if (baseResponseEntity==null){
                    Toast.makeText(ShowTemp.this,"请求内容为空",Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<BaseResponseEntity> call, Throwable t) {
                Toast.makeText(ShowTemp.this,"请求出错 " + t.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getHumidity() {
        netWorkBusiness.getSensor(deviceID, "humidity", new NCallBack<BaseResponseEntity<SensorInfo>>() {
            @Override
            public void onResponse(final Call<BaseResponseEntity<SensorInfo>> call, final Response<BaseResponseEntity<SensorInfo>> response) {
                BaseResponseEntity baseResponseEntity = response.body();
                if (baseResponseEntity != null) {
                    final Gson gson = new Gson();
                    JSONObject jsonObject;
                    String msg = gson.toJson(baseResponseEntity);
                    try {
                        jsonObject = new JSONObject(msg);
                        JSONObject resultObj = (JSONObject) jsonObject.get("ResultObj");
                        String HumValue = resultObj.getString("Value");
                        humidity_d = Double.valueOf(HumValue).intValue();
                        humidity = (int) humidity_d;

                        mDeviceTempHum.setHum(humidity);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            protected void onResponse(BaseResponseEntity<SensorInfo> response) {

            }

            public void onFailure(final Call<BaseResponseEntity<SensorInfo>> call, final Throwable t) {
                Toast.makeText(ShowTemp.this, "Can not get humidity data for now.", Toast.LENGTH_SHORT).show();
            }
        });

        }

    }
