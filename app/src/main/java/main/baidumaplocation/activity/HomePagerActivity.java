package main.baidumaplocation.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import main.baidumaplocation.R;
import main.baidumaplocation.utils.AnimationHelper;

public class HomePagerActivity extends AppCompatActivity {
    @Bind(R.id.seekBar)
    SeekBar mSeekBar;
    @Bind(R.id.tv_seekbar)
    TextView mTvSeekbar;
    @Bind(R.id.main_layout)
    LinearLayout mMainLayout;
    private long durationMills = 500;//动画时长

    private String gAddress = "";//定位获取到的地址
    private String gProvince = "";//省
    private String gCity = "";//市
    private String gArea = "";//区

    private String gLat = "";//纬度
    private String gLng = "";//经度

    @Bind(R.id.cbx_gaode)
    CheckBox mCbxGaode;
    @Bind(R.id.cbx_baidu)
    CheckBox mCbxBaidu;
    @Bind(R.id.tv_address)
    TextView mTvAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_pager);
        ButterKnife.bind(this);
        setListener();
    }

    public void setListener() {
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                durationMills = progress;
                mTvSeekbar.setText("durationMills:" + durationMills);
                int num = (int) (Math.random() * -16777216);
                String hex = Integer.toHexString(num);
                Log.e("----hex", "onProgressChanged: " + "#" + hex);
//                mTvSeekbar.setTextColor(Color.parseColor("#" + hex));
                mMainLayout.setBackgroundColor(Color.parseColor("#" + hex));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @OnClick({R.id.cbx_gaode, R.id.cbx_baidu})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.cbx_gaode:
                //高德地图
                Intent intentGD = new Intent();
                intentGD.setClass(HomePagerActivity.this, GaodeMap_SelectAddressActivity.class);
                //                startActivityForResult(intentGD,001);
                AnimationHelper.startActivityForResult(HomePagerActivity.this, intentGD, 001, null, mCbxGaode, R.color.colorPrimaryDark, durationMills);
                break;
            case R.id.cbx_baidu:
                //百度地图
                Intent intentBD = new Intent();
                intentBD.setClass(HomePagerActivity.this, BaiduMao_SelectAddressActivity.class);
                //                startActivityForResult(intentBD,002);
                AnimationHelper.startActivityForResult(HomePagerActivity.this, intentBD, 002, null, mCbxBaidu, R.color.colorPrimaryDark, durationMills);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 001:
                    //高德地图
                    gProvince = data.getStringExtra("sheng");
                    gCity = data.getStringExtra("shi");
                    gArea = data.getStringExtra("qu");
                    gAddress = data.getStringExtra("address");
                    gLng = data.getStringExtra("lng");
                    gLat = data.getStringExtra("lat");
                    mTvAddress.setText("\n\n高德地图\n\n" + "省：" + gProvince + "\n市：" + gCity + "\n区：" + gArea + "\n地址：" + gAddress + "\n经度：" + gLng + "\n纬度：" + gLat);
                    break;
                case 002:
                    //百度地图
                    gProvince = data.getStringExtra("sheng");
                    gCity = data.getStringExtra("shi");
                    gArea = data.getStringExtra("qu");
                    gAddress = data.getStringExtra("address");
                    gLng = data.getStringExtra("lng");
                    gLat = data.getStringExtra("lat");
                    mTvAddress.setText("百度地图\n\n" + "省：" + gProvince + "\n市：" + gCity + "\n区：" + gArea + "\n地址：" + gAddress + "\n经度：" + gLng + "\n纬度：" + gLat);
                    break;
            }
        }
    }
}
