package com.auv.sienlockdemo;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jeremyliao.liveeventbus.LiveEventBus;

import java.math.BigDecimal;
import java.util.List;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.MainTotalCell)
    EditText MainTotalCell;
    @BindView(R.id.openDoor)
    Button openDoor;

    private String TAG="MainActivity";

    Boolean bOpen;

    @BindView(R.id.successCount)
            TextView succesTxt;
    @BindView(R.id.failCount)
    TextView failTxt;

    int successCount=0;
    int failCount=0;
    boolean isFinish;

    Boolean isOpenPort;

    @BindView(R.id.openAndClosePort)
    Button openAndClosePort;

    @BindView(R.id.serialPortName)
    TextView serialPortName;
    boolean  isResult=false;
    public MainActivity() {
        Boolean bool=Boolean.TRUE;
        this.isOpenPort=bool;
        this.bOpen=bool;
        this.isFinish=false;
    }

    private void modifyCellTotal() {
        if (this.MainTotalCell.getText().toString().isEmpty()) {
            showToast("主板柜位数不能为空");
            return;
        }
        int count=Integer.parseInt(this.MainTotalCell.getText().toString());
        BaseApplication.countTotal=count;
        BaseApplication.getStrSecondaryCount=(int)Math.ceil(deciMal((count-Integer.parseInt(BaseApplication.strMainCellTotal)),Integer.parseInt(BaseApplication.strSecondaryCellTotal)))/1+"";
        showToast("修改成功");
    }

    private void openAndClosePort() {
        BaseApplication.PORT_NAME=this.serialPortName.getText().toString();
        String stringBuilder=this.TAG +
                "PORT_NAME =";
        Log.d(stringBuilder, BaseApplication.PORT_NAME);
        if (this.isOpenPort) {
            this.isOpenPort=Boolean.FALSE;
            LockControlBoardUtils.getInstances().closeSerialPort();
            this.openAndClosePort.setText("打开串口");
            return;
        }
        this.isOpenPort=Boolean.TRUE;
        LockControlBoardUtils.getInstances().openSerialPort();
        this.openAndClosePort.setText("关闭串口");
    }
    private double deciMal(int top, int below) {
        double result = new BigDecimal((float)top / below).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        return result;
    }
    private void openDoor() {
        if(BaseApplication.countTotal==0){
            showToast("柜子总数不能为空");
            return;
        }
        if (!bOpen) {
            openDoor.setText("关闭测试");
            bOpen=true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (bOpen){
                        for(int i=0;i<BaseApplication.countTotal;i++){
                              isResult=false;
                            if(!bOpen){
                                break;
                            }
                            AUVCellController.getInstance(getApplication()).openDoor(i+1);
                            /*LockControlBoardUtils.OpenCell(i+1, new LockControlBoardUtils.OpenDoorCallBack() {
                                public void fail(String param1String) {
                                    Log.d(MainActivity.this.TAG, "fail: ");
                                }
                                public void success(boolean param1Boolean) {
                                    Log.d(MainActivity.this.TAG, "success: ");
                                    showToast(param1Boolean ? "打开成功" : "打开失败");
                                    if(param1Boolean){
                                        successCount++;
                                        MainActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                succesTxt.setText("成功数："+successCount);
                                            }
                                        });
                                    }else {
                                        failCount++;
                                        MainActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                failTxt.setText("失败数："+failCount);
                                            }
                                        });
                                    }

                                }
                            });*/
                            msleep(1000);
                            if(!isResult){
                                failCount++;
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        failTxt.setText("失败数："+failCount);
                                    }
                                });
                            }
                        }

                    }
                }
            }).start();


        }else{
            openDoor.setText("开启测试");
            this.bOpen=false;
        }

    }


    private static void msleep(int arg2) {
        try {
            Thread.sleep(arg2);
        } catch (InterruptedException v0_1) {
            v0_1.printStackTrace();
        }
    }
    private void openDoorAll() {
        this.isFinish=false;
        LockControlBoardUtils.OpenCell(0, new LockControlBoardUtils.OpenDoorCallBack() {
            public void fail(String param1String) {
                Log.d(MainActivity.this.TAG, "fail: ");
                MainActivity.this.isFinish=true;
                showToast(param1String);
            }

            public void success(boolean param1Boolean) {
                Log.d(MainActivity.this.TAG, "success: ");
                showToast(param1Boolean ? "打开成功" : "打开失败");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        AUVCellController.getInstance(this).initUsbControl();
        AUVCellController.getInstance(this).onDeviceStateChange();
    }

    @Override
    protected void onPause() {
        super.onPause();
        AUVCellController.getInstance(this).onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bOpen=false;
    }

    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        //this.serialPortName.setText(BaseApplication.PORT_NAME);
        //this.cellNumber.setText("1");
        //LockControlBoardUtils.getInstances().openSerialPort();
        //this.openAndClosePort.setText("关闭串口");
        ActionBar actionBar=getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setTitle("返回");
        }

        LiveEventBus.get("newData",byte[].class).observe(this, result->{
            String s = Bcc16Util.byteTo16String(result).toUpperCase();
            Log.d("MainActivity","收到 16进制数据"+s);
            String[] split = s.split(" ");
            if(split.length==5){
                String boardId= split[1];
                String lockId= split[2];
                String statue=split[3];
                int cellNo=0;
                if(!lockId.equals("00")) {
                    if (DataUtils.HexToInt(boardId) == 1) {
                        cellNo = DataUtils.HexToInt(lockId);
                    } else if (DataUtils.HexToInt(boardId) > 1) {
                        cellNo = (DataUtils.HexToInt(boardId) - 2) * Integer.parseInt(BaseApplication.strSecondaryCellTotal) + Integer.parseInt(BaseApplication.strMainCellTotal) + DataUtils.HexToInt(lockId);
                    }
                }
                switch (split[0]){
                    case "81":

                        break;
                    case "8A"://开锁反馈和锁关反馈

                        if(cellNo!=0){
                            if(statue.equals("11")){
                                successCount++;
                                succesTxt.setText("成功数："+successCount);
                                isResult=true;
                            }
                        }
                    break;

                }
            }


        });
    }

    public boolean onOptionsItemSelected(MenuItem paramMenuItem) {
        if (paramMenuItem.getItemId() != 16908332)
            return super.onOptionsItemSelected(paramMenuItem);
        finish();
        return false;
    }

    @OnClick({R.id.quite, R.id.modifyCellTotal, R.id.openAndClosePort, R.id.openDoor})
    void onclick(View paramView) {
        switch (paramView.getId()) {
            case R.id.quite:
                finish();
                break;
            case R.id.openDoor:
                openDoor();
                break;
            case R.id.openAndClosePort:
                openAndClosePort();
                break;
            case R.id.modifyCellTotal:
                modifyCellTotal();
                break;
        }
    }

    private void showToast(final String msg) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });

    }
}
