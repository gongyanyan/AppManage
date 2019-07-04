package com.panchan.pc.appmanage;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.panchan.pc.appmanage.nanohttp.HttpServerImpl;
import com.panchan.pc.appmanage.nanohttp.ParamsBean;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.RootToolsException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    HttpServerImpl mHttpServer;
    Button btn1,btn2,btn3,btn4,btn5,btn6;
    Process process;
    ParamsBean paramsBean;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);
        btn4 = findViewById(R.id.btn4);
        btn5 = findViewById(R.id.btn5);
        btn6 = findViewById(R.id.btn6);

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
        btn4.setOnClickListener(this);
        btn5.setOnClickListener(this);
        btn6.setOnClickListener(this);

        paramsBean = new ParamsBean();

        mHttpServer = HttpServerImpl.getInstance();

        boolean b = ApkController.hasRootPerssion();
        Log.i("123","权限::"+b);

        try {
            mHttpServer.start();
        } catch (IOException e) {
            Log.d("123", "onCreate http start error :" ,e);
        }

        String IpAndress = getLocalIpStr(this);   //得到本机的IP地址
        Log.i("123","IP地址:"+getLocalIpStr(this));

        mHttpServer.setOnOpenDoorListener(new HttpServerImpl.OnOpenDoorListener() {
            @Override
            public void onOpenDoor(String param) {

                String str = param;
                paramsBean = JSON.parseObject(param,ParamsBean.class);   //请求的值
                Log.i("123",paramsBean+"得到的参数::"+param);

                if(paramsBean.getMsgType().equals("CQ")){

                    Log.i("123","99");

                    reBoot2();
                 }else if(paramsBean.getMsgType().contains("AIUIProductDemo")){
                    Log.i("123","AIUIProductDemo");

                    //秒装
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            SilentInstall installHelper = new SilentInstall();
                            final boolean result = installHelper.install("/system/app/AIUIProductDemo.1.37.apk");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (result) {
                                        Toast.makeText(MainActivity.this, "安装成功！", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(MainActivity.this, "安装失败！", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                        }
                    }).start();


                }else if(paramsBean.getMsgType().equals("UARTService")){
                    Log.i("123","UARTService");
                }

            }
        });

    }


    public static String getLocalIpStr(Context context) {
        WifiManager wifiManager=(WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return intToIpAddr(wifiInfo.getIpAddress());
    }

    private static String intToIpAddr(int ip) {
        return (ip & 0xff) + "." + ((ip>>8)&0xff) + "." + ((ip>>16)&0xff) + "." + ((ip>>24)&0xff);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){

            case R.id.btn1:    //关机

                shutDown2();


                break;

            case R.id.btn2:   //重启



                reBoot2();


                break;



            case R.id.btn3:   //开启智能服务


                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);


                break;


            case R.id.btn4:   //智能安装


                /*//利用ProcessBuilder
                installSilent("/sdcard/sluice.apk");

*/

                //智能安装
               /* Uri uri = Uri.fromFile(new File("/sdcard/sluice.apk"));
                Intent localIntent = new Intent(Intent.ACTION_VIEW);
                localIntent.setDataAndType(uri, "application/vnd.android.package-archive");
                startActivity(localIntent);
*/


                ApkController.install("/sdcard/sluice.apk",MainActivity.this);


                break;


            case R.id.btn5:   //秒装



                //偷偷安装
             /*   try {
                    RootTools.sendShell("pm install sdcard/sluice.apk", 5000);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (RootToolsException e) {
                    e.printStackTrace();
                } catch (TimeoutException e) {
                    e.printStackTrace();
                }*/


                //秒装
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SilentInstall installHelper = new SilentInstall();
                        final boolean result = installHelper.install("/sdcard/sluice.apk");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (result) {
                                    Toast.makeText(MainActivity.this, "安装成功！", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "安装失败！", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                    }
                }).start();


                break;

            case R.id.btn6:

                openApp("com.example.aipen.posttest","com.example.aipen.posttest.MainActivity");

                break;


        }
    }


    /**
     * 通过广播的方式，重启系统，不推荐这种方式，因为我在系统日志，发现这是系统异常关机
     */
    private void reBoot() {

        Intent i = new Intent(Intent.ACTION_REBOOT);
        i.putExtra("nowait", 1);
        i.putExtra("interval", 1);
        i.putExtra("window", 0);
        sendBroadcast(i);

    }

    /**
     * 通过Runtime，发送指令，重启系统，测试结果，不起作用，可能需要root
     */
    private void reBoot2() {

        try {
            Log.i("123", "root Runtime->reboot");
            //Process proc = Runtime.getRuntime().exec("adb shell reboot");
            Process proc = Runtime.getRuntime().exec("su -c \"/system/bin/reboot\"");
            //Process proc = Runtime.getRuntime().exec(new String[]{"su", "-c", "reboot "}); //关机
            proc.waitFor();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void reBoot3() {
        /*弹出重启设备菜单 */
        Log.v("123", "reBoot3");
        PowerManager pManager = (PowerManager) getSystemService(Context.POWER_SERVICE); //重启到fastboot模式
        pManager.reboot("重启");
        //pManager.reboot("");

    }
    /**
     * 关机
     */
    private void shutDown() {
        Log.v("123", "shutDown");
        try {
            //获得ServiceManager类
            Class ServiceManager = Class
                    .forName("android.os.ServiceManager");
            //获得ServiceManager的getService方法
            Method getService = ServiceManager.getMethod("getService", java.lang.String.class);
            //调用getService获取RemoteService
            Object oRemoteService = getService.invoke(null, Context.POWER_SERVICE);
            //获得IPowerManager.Stub类
            Class cStub = Class
                    .forName("android.os.IPowerManager$Stub");
            //获得asInterface方法
            Method asInterface = cStub.getMethod("asInterface", android.os.IBinder.class);
            //调用asInterface方法获取IPowerManager对象
            Object oIPowerManager = asInterface.invoke(null, oRemoteService);
            //获得shutdown()方法
            Method shutdown = oIPowerManager.getClass().getMethod("shutdown", boolean.class, boolean.class);
            //调用shutdown()方法
            shutdown.invoke(oIPowerManager, false, true);
        } catch (Exception e) {
            Log.e("123", e.toString(), e);
        }

    }

    private void shutDown2() {
      /*  try {
            //Process proc =Runtime.getRuntime().exec(new String[]{"su","-c","shutdown"}); //关机
            Process proc =Runtime.getRuntime().exec(new String[]{"su","-c","reboot -p"}); //关机
            proc.waitFor();
        } catch (Exception e) {
            // TODO Auto-generated catch block			e.printStackTrace();
            //
        }*/

            try {
                    process = Runtime.getRuntime().exec("su");
                    DataOutputStream out = new DataOutputStream(
                            process.getOutputStream());
                    out.writeBytes("reboot -p\n");
                    out.writeBytes("exit\n");
                    out.flush();
                } catch (IOException e) {			// TODO 自动生成的 catch 块			e.printStackTrace();

                }
    }

    private void shutDown1() {

                Intent intent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
                //其中false换成true,会弹出是否关机的确认窗口
                intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                this.startActivity(intent);

    }

    /**  * 静默安装的实现类，调用install()方法执行具体的静默安装逻辑。  * 原文地址：http://blog.csdn.net/guolin_blog/article/details/47803149  * @author guolin  * @since 2015/12/7  */
    public class SilentInstall {
        /**      * 执行具体的静默安装逻辑，需要手机ROOT。      * @param apkPath      *          要安装的apk文件的路径      * @return 安装成功返回true，安装失败返回false。      */
        public boolean install(String apkPath) {
            boolean result = false;
            DataOutputStream dataOutputStream = null;
            BufferedReader errorStream = null;
            try {
                // 申请su权限
                Process process = Runtime.getRuntime().exec("su");
                dataOutputStream = new DataOutputStream(process.getOutputStream());
                // 执行pm install命令
                String command = "pm install -r " + apkPath + "\n";
                dataOutputStream.write(command.getBytes(Charset.forName("utf-8")));
                dataOutputStream.flush();
                dataOutputStream.writeBytes("exit\n");
                dataOutputStream.flush();
                process.waitFor();
                errorStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String msg = "";
                String line;
                // 读取命令的执行结果
                while ((line = errorStream.readLine()) != null) {
                    msg += line;
                }
                Log.d("TAG", "install msg is " + msg);
                // 如果执行结果中包含Failure字样就认为是安装失败，否则就认为安装成功
                if (!msg.contains("Failure")) {
                    result = true;
                }
            } catch (Exception e) {
                Log.e("TAG", e.getMessage(), e);
            } finally {
                try {
                    if (dataOutputStream != null) {
                        dataOutputStream.close();
                    }
                    if (errorStream != null) {
                        errorStream.close();
                    }
                } catch (IOException e) {
                    Log.e("TAG", e.getMessage(), e);
                }
            }          return result;
        }

    }


    /**      * 判断手机是否拥有Root权限。      * @return 有root权限返回true，否则返回false。      */
    public boolean isRoot() {
        boolean bool = false;
        try {
            bool = new File("/system/bin/su").exists() || new File("/system/xbin/su").exists();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bool;
    }

    /**
     * install slient
     *
     * @param filePath
     * @return 0 means normal, 1 means file not exist, 2 means other exception error
     */
    public static int installSilent(String filePath) {
        File file = new File(filePath);
        if (filePath == null || filePath.length() == 0 || file == null || file.length() <= 0 || !file.exists() || !file.isFile()) {
            return 1;
        }

        String[] args = { "pm", "install", "-r", filePath };
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        Process process = null;
        BufferedReader successResult = null;
        BufferedReader errorResult = null;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder errorMsg = new StringBuilder();
        int result;
        try {
            process = processBuilder.start();
            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String s;
            while ((s = successResult.readLine()) != null) {
                successMsg.append(s);
            }
            while ((s = errorResult.readLine()) != null) {
                errorMsg.append(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (successResult != null) {
                    successResult.close();
                }
                if (errorResult != null) {
                    errorResult.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (process != null) {
                process.destroy();
            }
        }

        // TODO should add memory is not enough here
        if (successMsg.toString().contains("Success") || successMsg.toString().contains("success")) {
            result = 0;
        } else {
            result = 2;
        }
        Log.d("test-test", "successMsg:" + successMsg + ", ErrorMsg:" + errorMsg);
        return result;
    }


    public void  openApp(String packa,String appname){    //包名/应用类名
        try {
            //关闭其他应用
            //Process exec = Runtime.getRuntime().exec("adb shell am force-stop 包名");
            //打开其他应用
             //Process exec = Runtime.getRuntime().exec("adb shell am start -n 包名/启动类名称");
             Process exec = Runtime.getRuntime().exec("adb shell am start -n "+packa +"/"+appname);
            if (exec.waitFor() == 0) {
                Toast.makeText(MainActivity.this, "ok", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
