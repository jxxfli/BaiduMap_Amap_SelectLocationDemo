package main.baidumaplocation.utils;

import android.content.Context;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;


/**
 * ======================================================>
 *
 * @说明: 高德地图定位方法
 * @作者: Lino-082
 * @日期: 2017/6/10
 * @时间: 11:39
 * ======================================================>
 */

public class AMapLocUtils implements AMapLocationListener {
    private AMapLocationClient locationClient = null;  // 定位
    private AMapLocationClientOption locationOption = null;  // 定位设置

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        mLonLatListener.getLonLat(aMapLocation);
        locationClient.stopLocation();
        locationClient.onDestroy();
        locationClient = null;
        locationOption = null;
    }

    private LonLatListener mLonLatListener;
//    private StopLocationListener mStopLocationListener;

    public void getLonLat(Context context, LonLatListener lonLatListener) {
        locationClient = new AMapLocationClient(context);

        locationOption = new AMapLocationClientOption();
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);// 设置定位模式为高精度模式
        locationClient.setLocationListener(this);// 设置定位监听
        locationOption.setOnceLocation(true); // 单次定位
        locationOption.setNeedAddress(true);//逆地理编码
        mLonLatListener = lonLatListener;//接口
//        mStopLocationListener = mStopLocationListener;//接口
        locationClient.setLocationOption(locationOption);// 设置定位参数
        locationClient.startLocation(); // 启动定位
    }

    /**
     * 获取经纬度
     */
    public interface LonLatListener {
        void getLonLat(AMapLocation aMapLocation);
    }

    /**
     * 停止定位
     */
   /* public interface StopLocationListener {
        void StopLocation(AMapLocationClient mapLocationClient);
    }*/
}