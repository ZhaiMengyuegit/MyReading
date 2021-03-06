package com.example.myreading;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myreading.Utils.CodeUtils;
import com.example.myreading.Utils.MessageEvent;
import com.example.myreading.Utils.UserDataManager;

import org.greenrobot.eventbus.Subscribe;

public class LoginActivity extends BaseActivity {                 //登录界面活动

    public int pwdresetFlag=0;
    private EditText mAccount;                        //用户名编辑
    private EditText mPwd;                            //密码编辑
    private Button mRegisterButton;                   //注册按钮
    private Button mLoginButton;                      //登录按钮
   // private Button mCancleButton;                     //注销按钮
  // private CheckBox mRememberCheck;                  //记住密码
    private EditText ed_yzm;
    private ImageView img_yzm;



   private CodeUtils codeUtils;

    private SharedPreferences login_sp;
    //private String userNameValue,passwordValue;

    private View loginView;                           //登录
    private View loginSuccessView;
    private TextView loginSuccessShow;
    private TextView mChangepwdText;
    private UserDataManager mUserDataManager;         //用户数据管理类
    //private Encryp mAes;                              //用户信息加密



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
      if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {

            finish();

            return;

        }




        //通过id找到相应的控件
        mAccount = (EditText) findViewById(R.id.login_edit_account);
        mPwd = (EditText) findViewById(R.id.login_edit_pwd);
        ed_yzm = (EditText) findViewById(R.id.login_edit_yzm);
        img_yzm = (ImageView) findViewById(R.id.login_img_yzm);
        mRegisterButton = (Button) findViewById(R.id.login_btn_register);
        mLoginButton = (Button) findViewById(R.id.login_btn_login);

       // mCancleButton = (Button) findViewById(R.id.login_btn_cancle);
        /*  loginView=findViewById(R.id.login_view);
          loginSuccessView=findViewById(R.id.login_success_view);
          loginSuccessShow=(TextView) findViewById(R.id.login_success_show);

         */

        mChangepwdText = (TextView) findViewById(R.id.login_text_change_pwd);
       // mRememberCheck = (CheckBox) findViewById(R.id.login_che_remember);

        login_sp = getSharedPreferences("userInfo", 0);
        String name=login_sp.getString("USER_NAME", "");
        String pwd =login_sp.getString("PASSWORD", "");

       /* boolean choseRemember =login_sp.getBoolean("mRememberCheck", false);
        boolean choseAutoLogin =login_sp.getBoolean("mAutologinCheck", false);
        //如果上次选了记住密码，那进入登录页面也自动勾选记住密码，并填上用户名和密码
        if(choseRemember){
            mAccount.setText(name);
            mPwd.setText(pwd);
            mRememberCheck.setChecked(true);
        }

        */


        codeUtils = CodeUtils.getInstance();
        Bitmap bitmap = codeUtils.createBitmap();
        img_yzm.setImageBitmap(bitmap);
        img_yzm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                codeUtils = CodeUtils.getInstance();
                Bitmap bitmap = codeUtils.createBitmap();
                img_yzm.setImageBitmap(bitmap);
                String code = codeUtils.getCode().toLowerCase();
                System.out.println("code------------:" + code);
            }        });


        mRegisterButton.setOnClickListener(mListener);                      //采用OnClickListener方法设置不同按钮按下之后的监听事件
        mLoginButton.setOnClickListener(mListener);
        //mCancleButton.setOnClickListener(mListener);
        mChangepwdText.setOnClickListener(mListener);

        ImageView image = (ImageView) findViewById(R.id.login_img_logo);             //使用ImageView显示logo
        image.setImageResource(R.drawable.log );

        if (mUserDataManager == null) {
            mUserDataManager = new UserDataManager(this);
            mUserDataManager.openDataBase();                              //建立本地数据库
        }
    }

    OnClickListener mListener = new OnClickListener() {                  //不同按钮按下的监听事件选择
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.login_btn_register:                            //登录界面的注册按钮
                    Intent intent_Login_to_Register = new Intent(LoginActivity.this, RegisterActivity.class) ;    //切换Login Activity至User Activity
                    startActivity(intent_Login_to_Register);
                    finish();
                    break;
                case R.id.login_btn_login:                              //登录界面的登录按钮
                    login();
                    break;
               /* case R.id.login_btn_cancle:                             //登录界面的注销按钮
                    cancel();
                    break;*/
                case R.id.login_text_change_pwd:                             //登录界面的修改密码按钮
                    Intent intent_Login_to_reset = new Intent(LoginActivity.this,EditUserActivity.class) ;    //切换Login Activity至User Activity
                    startActivity(intent_Login_to_reset);
                    finish();
                    break;
            }
        }
    };

   public void login() {//登录按钮监听事件
       String codeStr = ed_yzm.getText().toString().trim().toLowerCase();
       String code = codeUtils.getCode().toLowerCase();//           进行验证码判断
       if (null == codeStr || TextUtils.isEmpty(codeStr) || !code.equalsIgnoreCase(codeStr)) {
           Toast.makeText(LoginActivity.this, "请输入正确验证码", Toast.LENGTH_SHORT).show();
       }
       //                将验证码变成小写字母
       System.out.println("code------------:" + code);

       if (code.equalsIgnoreCase(codeStr)) {
           if (isUserNameAndPwdValid()) {

               String userName = mAccount.getText().toString().trim();    //获取当前输入的用户名和密码信息
               String userPwd = mPwd.getText().toString().trim();
               SharedPreferences.Editor editor = login_sp.edit();
               int result = mUserDataManager.findUserByNameAndPwd(userName, userPwd);
               if (result == 1) {                                             //返回1说明用户名和密码均正确
                   //保存用户名和密码
                   editor.putString("USER_NAME", userName);
                   editor.putString("PASSWORD", userPwd);

                 /*  //是否记住密码
                if(mRememberCheck.isChecked()){
                    editor.putBoolean("mRememberCheck", true);
                }else{
                    editor.putBoolean("mRememberCheck", false);
                }

                  */


                   editor.commit();

                   Intent intent = new Intent(LoginActivity.this, MainActivity.class);    //切换Login Activity至User Activity
                   startActivity(intent);
                   finish();
                   Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();//登录成功提示
               } else if (result == 0) {
                   Toast.makeText(this, "用户不存在或密码错误", Toast.LENGTH_SHORT).show();  //登录失败提示
               }
           }
       }
   }


  /*  public void cancel() {           //注销
        if (isUserNameAndPwdValid()) {
            String userName = mAccount.getText().toString().trim();    //获取当前输入的用户名和密码信息
            String userPwd = mPwd.getText().toString().trim();
            int result=mUserDataManager.findUserByNameAndPwd(userName, userPwd);
            if(result==1){                                             //返回1说明用户名和密码均正确
                Toast.makeText(this, getString(R.string.cancel_success),Toast.LENGTH_SHORT).show();<span style="font-family: Arial;">//注销成功提示</span>
                        mPwd.setText("");
                mAccount.setText("");
                mUserDataManager.deleteUserDatabyname(userName);
            }else if(result==0){
                Toast.makeText(this, getString(R.string.cancel_fail),Toast.LENGTH_SHORT).show();  //注销失败提示
            }
        }

    }

   */
    public boolean isUserNameAndPwdValid() {
        if (mAccount.getText().toString().trim().equals("")) {
            Toast.makeText(this, "用户名不能为空",
                    Toast.LENGTH_SHORT).show();
            return false;
        } else if (mPwd.getText().toString().trim().equals("")) {
            Toast.makeText(this,"密码不能为空",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    protected void onResume() {
        if (mUserDataManager == null) {
            mUserDataManager = new UserDataManager(this);
            mUserDataManager.openDataBase();
        }
        super.onResume();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    @Override
    protected void onPause() {
        if (mUserDataManager != null) {
            mUserDataManager.closeDataBase();
            mUserDataManager = null;
        }
        super.onPause();
    }
    @Subscribe
    public void onMessageEvent(MessageEvent event) {
        if (event.getCode() == 1000){
            Log.e("TAG","LoginActivity");
            LoginActivity.this.finish();
        }
    }


}
