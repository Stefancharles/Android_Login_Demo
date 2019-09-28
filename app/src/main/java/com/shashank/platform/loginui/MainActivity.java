package com.shashank.platform.loginui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.shashank.platform.loginui.util.CodeUtils;

import cn.com.newland.nle_sdk.requestEntity.SignIn;
import cn.com.newland.nle_sdk.responseEntity.User;
import cn.com.newland.nle_sdk.responseEntity.base.BaseResponseEntity;
import cn.com.newland.nle_sdk.util.NetWorkBusiness;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    ImageView imageView;
    TextView textView;
    TextView forget_password;
    private EditText username;   //账户
    private EditText password;  //密码
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private String _username = "";
    private String _password = "";
    private Bitmap bitmap;
    private String code;//正确的验证码
    private String codeStr;//用户输入的验证码
    private EditText et_phoneCodes;
    int count = 0;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        sp = getSharedPreferences("nlecloud",MODE_PRIVATE);
        editor = sp.edit();
        final Button login = findViewById(R.id.login);
        final Button sign_up = findViewById(R.id.sign_up);
        //获取需要展示图片验证码的ImageView
        final ImageView image = findViewById(R.id.image);
        //获取工具类生成的图片验证码对象
        bitmap = CodeUtils.getInstance().createBitmap();
        //获取当前图片验证码的对应内容用于校验
        code = CodeUtils.getInstance().getCode();

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        et_phoneCodes =findViewById(R.id.et_phoneCodes);
        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        forget_password = findViewById(R.id.forget_password);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        // TODO: 2019/9/22  判断SharedPreferences文件中，用户名、密码是否存在
        if (sp.getString("username",_username)!=null && sp.getString("password",_password)!=null){
            if (!sp.getString("username",_username).equals("") && !sp.getString("password",_password).equals("")){
                username.setText(sp.getString("username","1"));
                password.setText(sp.getString("password","2"));
            }
        }

        // TODO: 2019/9/22 若账号或密码有一项为空，则登录按钮不能点击。
        _username=username.getText().toString();
        _password=password.getText().toString();

        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(username.getText().length()==0||password.getText().length()==0){
                    login.setEnabled(false);
                }else{
                    _username=username.getText().toString();
                    _password=password.getText().toString();
                    login.setEnabled(true);
                }
            }
        };
        username.addTextChangedListener(textWatcher);
        password.addTextChangedListener(textWatcher);

        // TODO: 2019/9/22 登陆按键的点击事件
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 2019/9/22  对图片验证码进行验证，验证成功则进行登陆
                /*codeStr = et_phoneCodes.getText().toString().trim();
                Log.e("codeStr", codeStr);
                if (null == codeStr || TextUtils.isEmpty(codeStr)) {
                    Toast.makeText(MainActivity.this,"Please Enter confirmation code.",Toast.LENGTH_SHORT).show();
                    return;
                }
                String code = CodeUtils.getCode();
                Log.e("code", code);
                if (code.equalsIgnoreCase(codeStr)) {
                    Toast.makeText(MainActivity.this,"Welcome Home!.",Toast.LENGTH_SHORT).show();
                    signIn();//验证码正确后进行账号和密码的验证
                } else {
                    Toast.makeText(MainActivity.this,"Please Enter the Right confirmation code.",Toast.LENGTH_SHORT).show();
                    bitmap = CodeUtils.getInstance().createBitmap();
                    code = CodeUtils.getInstance().getCode();
                    image.setImageBitmap(bitmap);//输入错误后更换验证码
                }*/
                signIn();

            }
        });

        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"Registration function is not open yet...",Toast.LENGTH_SHORT).show();
            }
        });
        forget_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"Sometimes you have forget something so that you could move on.",Toast.LENGTH_LONG).show();
            }
        });

        image.setImageBitmap(bitmap);
        // TODO: 2019/9/22 更换图片验证码内容
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bitmap = CodeUtils.getInstance().createBitmap();
                code = CodeUtils.getInstance().getCode();
                image.setImageBitmap(bitmap);
                //Toast.makeText(MainActivity.this, code, Toast.LENGTH_SHORT).show();
            }
        });


        imageView.setOnTouchListener(new OnSwipeTouchListener(getApplicationContext()) {
            public void onSwipeTop() {
            }

            @SuppressLint("SetTextI18n")
            public void onSwipeRight() {
                if (count == 0) {
                    imageView.setImageResource(R.drawable.good_night_img);
                    textView.setText("Night");
                    count = 1;
                } else {
                    imageView.setImageResource(R.drawable.good_morning_img);
                    textView.setText("Morning");
                    count = 0;
                }
            }

            @SuppressLint("SetTextI18n")
            public void onSwipeLeft() {
                if (count == 0) {
                    imageView.setImageResource(R.drawable.good_night_img);
                    textView.setText("Night");
                    count = 1;
                } else {
                    imageView.setImageResource(R.drawable.good_morning_img);
                    textView.setText("Morning");
                    count = 0;
                }
            }

            public void onSwipeBottom() {
            }

        });
    }

    private void signIn(){
        String platformAddress = "http://api.nlecloud.com:80/";
        final NetWorkBusiness netWorkBusiness = new NetWorkBusiness("",platformAddress);
        netWorkBusiness.signIn(new SignIn(_username, _password), new Callback<BaseResponseEntity<User>>() {
            @Override
            public void onResponse(@NonNull Call<BaseResponseEntity<User>> call, @NonNull Response<BaseResponseEntity<User>> response) {
                BaseResponseEntity<User> baseResponseEntity = response.body();
                if (baseResponseEntity != null) {
                    if (baseResponseEntity.getStatus() == 0) {
                        //需要传输秘钥
                        //String accessToken = baseResponseEntity.getResultObj().getAccessToken();        //json数据返回
                        //成功.
                        editor.putString("username",_username);
                        editor.putString("password",_password);
                        editor.apply();
                        String accessToken = baseResponseEntity.getResultObj().getAccessToken();
                        Toast.makeText(MainActivity.this,"登录成功",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, ShowTemp.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("accessToken", accessToken);
                        intent.putExtras(bundle);
                        startActivity(intent);
                        //finish();
                    } else {
                        Toast.makeText(MainActivity.this, baseResponseEntity.getMsg(), Toast.LENGTH_SHORT).show();  //返回为空...
                    }
                }
            }
            @Override
            public void onFailure(@NonNull Call<BaseResponseEntity<User>> call, @NonNull Throwable t) {
                Toast.makeText(MainActivity.this,"登录失败 " + t.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

}
