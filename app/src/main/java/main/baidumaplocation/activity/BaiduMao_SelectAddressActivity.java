package main.baidumaplocation.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import main.baidumaplocation.R;
import main.baidumaplocation.adapter.CommonAdapter;
import main.baidumaplocation.bean.EntityAddressPoiBean;
import main.baidumaplocation.holder.ViewHolder;
import main.baidumaplocation.utils.InputTools;
import main.baidumaplocation.utils.StringUtil;
import main.baidumaplocation.utils.ToastUtil;



/**
 * @Description: 选择地址
 * @Author: Lino-082
 * @Date: 2017/5/16
 * @Time: 17:56
 */
public class BaiduMao_SelectAddressActivity extends Activity {
    @Bind(R.id.loading_progressbar)
    ProgressBar mLoadingProgressbar;
    @Bind(R.id.loading_text)
    TextView mLoadingText;
    @Bind(R.id.loaging_layout)
    LinearLayout mLoagingLayout;
    private boolean isLoading = false;//是否正在显示loading
    private boolean isClick = false;//是否点击定位按钮
    private ProgressDialog mDialog;//提示框
    private String lat;//纬度
    private String lon;//经度
    private String gProvince;//省
    private String gCity;//市
    private String gArea;//区
    private String gStreet;//路
    private String gLocation;//具体所选位置


    @Bind(R.id.act_baidu_confirm)
    TextView mActBaiduConfirm;
    @Bind(R.id.act_baidu_edit_layout)
    LinearLayout mActBaiduEditLayout;
    @Bind(R.id.info_layout)
    RelativeLayout mInfoLayout;
    private ImageView checkedIv;
    private boolean isShow = true;//结果列表是否显示
    private boolean hasMeasured = false;
    private int listViewHight;


    @Bind(R.id.Marker_img)
    ImageView mMarkerImg;
    @Bind(R.id.activity_main)
    LinearLayout mActivityMain;
    @Bind(R.id.im_down)
    ImageView mImDown;
    // 地图
    private BaiduMap mBaiduMap = null;
    // 地理编码器
    private GeoCoder mGeoCoder = null;
    @Bind(R.id.common_title_left)
    ImageButton mCommonTitleLeft;
    @Bind(R.id.act_baidu_edit)
    EditText act_baidu_edit;
    @Bind(R.id.act_baidu_search)
    TextView act_baidu_search;
    // 基础地图控件
    @Bind(R.id.mMapView)
    MapView mMapView;
    @Bind(R.id.act_baidu_back_start)
    ImageButton mActBaiduBackStart;
    @Bind(R.id.mListView)
    ListView mListView;
    @Bind(R.id.mListView2)
    ListView mListView2;
    private String address;

    private PoiSearch mPoiSearch;

    private CommonAdapter<EntityAddressPoiBean> mCommonAdapter = null;
    private List<EntityAddressPoiBean> mLists = null;

    private CommonAdapter<EntityAddressPoiBean> mCommonAdapter2 = null;
    private List<EntityAddressPoiBean> mLists2 = null;
    /*起点的覆盖物*/
    private Marker mMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baidu_location);
        ButterKnife.bind(this);
        initView();
    }

    @OnClick({R.id.common_title_left, R.id.act_baidu_search, R.id.act_baidu_back_start, R.id.act_baidu_confirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.common_title_left:
                //返回
                if (mActBaiduConfirm.getText().toString().equals("确定")) {
                    finish();
                } else {
                    mActBaiduConfirm.setText("确定");
                    mListView2.setVisibility(View.GONE);

                }
                break;
            case R.id.act_baidu_confirm:
                //确定
                if (mActBaiduConfirm.getText().toString().equals("确定")) {
                    if (TextUtils.isEmpty(gLocation)) {
                        ToastUtil.TextToast("请选择一个地址");
                    } else {
                        /*Intent mIntent = new Intent();
                        mIntent.putExtra("location", gLocation);
                        mIntent.putExtra("lat", lat);
                        mIntent.putExtra("lon", lon);
                        mIntent.putExtra("province", gProvince);
                        mIntent.putExtra("city", gCity);
                        mIntent.putExtra("area", gArea);
                        // 设置结果，并进行传送
                        this.setResult(0, mIntent);
                        this.finish();*/

                        Intent intent = new Intent();
                        intent.putExtra("sheng",gProvince);
                        intent.putExtra("shi",gCity);
                        intent.putExtra("qu",gArea);
                        intent.putExtra("street", gStreet);
                        intent.putExtra("address",gLocation);
                        intent.putExtra("lng",lon);
                        intent.putExtra("lat",lat);
                        setResult(RESULT_OK,intent);
                        finish();
                    }
                } else {
                    mActBaiduEditLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_white_2dp_bg));
                    mActBaiduConfirm.setText("确定");
                    mListView2.setVisibility(View.GONE);
                }
                break;
            case R.id.act_baidu_search:
                //搜索
                if (act_baidu_search.getText().toString().equals("搜索")) {
                    if (StringUtil.isEmpty(act_baidu_edit.getText().toString())) {
                        ToastUtil.TextToast("请先输入要搜索的地址");
                    } else {
                        initDialogShow();//显示Dialog
                        mActBaiduConfirm.setText("返回");
                        act_baidu_search.setText("取消");
                        mPoiSearch.searchInCity((new PoiCitySearchOption()).city(city).keyword(act_baidu_edit.getText().toString().trim()).pageNum(0)// 分页编号
                                .pageCapacity(50));// 设置每页容量，默认为每页10条
                    }
                } else {
                    mActBaiduEditLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_white_2dp_bg));
                    act_baidu_edit.setText("");
                    act_baidu_search.setText("搜索");
                    mActBaiduConfirm.setText("确定");
                    mListView2.setVisibility(View.GONE);
                }
                break;
            case R.id.act_baidu_back_start:
                //回到定位
                // /*设置该经纬度在地图居中*/
                if (mMarker != null) {
                    //设置缩放级别
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom(19).overlook(-45).build()));
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(mMarker.getPosition()));
                    isClick = true;
                }
                break;
        }
    }

    @OnClick(R.id.loading_text)
    public void onViewClicked() {
        //点击重新加载
        if (!isLoading) {
            isLoading = true;
            mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(mLatLng));
            if (mLoadingProgressbar.getVisibility() == View.GONE)
                mLoadingProgressbar.setVisibility(View.VISIBLE);
            mLoadingText.setText("加载中...");
        }
    }

    public void initView() {
        mLists = new ArrayList<EntityAddressPoiBean>();
        mCommonAdapter = new CommonAdapter<EntityAddressPoiBean>(BaiduMao_SelectAddressActivity.this, mLists, R.layout.activity_baidu_poi_item) {
            @Override
            public void convert(ViewHolder mHolder, EntityAddressPoiBean item, int position) {
                checkedIv = (ImageView) mHolder.getConvertView().findViewById(R.id.checked_iv);
                if (item.isChecked()) {
                    checkedIv.setVisibility(View.VISIBLE);
                } else {
                    checkedIv.setVisibility(View.GONE);
                }
                mHolder.setText(R.id.act_baidu_name, item.getName());
                mHolder.setText(R.id.act_baidu_address, item.getAddress());
            }
        };
        mListView.setAdapter(mCommonAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                /*boolean flag = true;
                for (int i = 0; i < mCitys.size(); i++) {
                    if (!(mLists.get(position).getAddress() + mLists.get(position).getName()).contains(mCitys.get(i).getName())) {
                        flag = false;
                    }
                }
                if (!flag) {
                    ToastUtil.TextToast("该城市未开通服务");
                    return;
                }*/
                isClick = false;

                if (mLoagingLayout.getVisibility() == View.VISIBLE) {
                    mLoagingLayout.setVisibility(View.GONE);//隐藏loading布局
                }

                mLists.get(position).setChecked(true);
                for (int i = 0; i < mLists.size(); i++) {
                    if (i != position) {
                        mLists.get(i).setChecked(false);
                    }
                }
                mCommonAdapter.notifyDataSetChanged();
                String name = mLists.get(position).getName();//地名
                String latitude = mLists.get(position).getLatitude();//纬度
                String longitude = mLists.get(position).getLongitude();//经度
                double getLatitude = Double.valueOf(latitude).doubleValue();
                double getLongitude = Double.valueOf(longitude).doubleValue();

                lat = latitude;
                lon = longitude;
                gLocation = name;

                mLatLng = new LatLng(getLatitude, getLongitude);
               /*设置该经纬度在地图居中*/
                MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(mLatLng);
                mBaiduMap.animateMapStatus(msu);
                address = mLists.get(position).getAddress();


            }
        });

        //
        mLists2 = new ArrayList<EntityAddressPoiBean>();
        mCommonAdapter2 = new CommonAdapter<EntityAddressPoiBean>(BaiduMao_SelectAddressActivity.this, mLists2, R.layout.activity_baidu_poi_item) {
            @Override
            public void convert(ViewHolder mHolder, EntityAddressPoiBean item, int position) {
                mHolder.setText(R.id.act_baidu_name, item.getName());
                mHolder.setText(R.id.act_baidu_address, item.getAddress());
            }
        };
        mListView2.setAdapter(mCommonAdapter2);
        mListView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                /*boolean flag = true;
                for (int i = 0; i < mCitys.size(); i++) {
                    if (!(mLists2.get(position).getAddress() + mLists2.get(position).getName()).contains(mCitys.get(i).getName())) {
                        flag = false;
                    }
                }
                if (!flag) {
                    ToastUtil.TextToast("该城市未开通服务");
                    return;
                }*/

                mActBaiduEditLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_white_2dp_bg));

                isClick = false;

                mActBaiduConfirm.setText("确定");
                act_baidu_search.setText("搜索");
                InputTools.HideKeyboard(act_baidu_edit);//隐藏输入法

                String name = mLists2.get(position).getName();//地名
                String latitude = mLists2.get(position).getLatitude();//纬度
                String longitude = mLists2.get(position).getLongitude();//经度
                double getLatitude = Double.valueOf(latitude).doubleValue();
                double getLongitude = Double.valueOf(longitude).doubleValue();

                lat = latitude;
                lon = longitude;
                gLocation = name;

                mLatLng = new LatLng(getLatitude, getLongitude);
               /*设置该经纬度在地图居中*/
                MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(mLatLng);
                mBaiduMap.animateMapStatus(msu);

                if (mLatLng == null) {
                    mLoagingLayout.setVisibility(View.GONE);
                    ToastUtil.TextToast("获取位置失败");
                } else {
                    isLoading = true;
                    mLoagingLayout.setVisibility(View.VISIBLE);
                    if (mLoadingProgressbar.getVisibility() == View.GONE)
                        mLoadingProgressbar.setVisibility(View.VISIBLE);
                    mLoadingText.setText("加载中...");

                    mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(mLatLng));
                    mListView2.setVisibility(View.GONE);
                }
            }
        });

        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(poiListener);
        initLocation();
    }

    private OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener() {
        @Override
        public void onGetPoiResult(PoiResult poiResult) {
            // 获取POI检索结果
            Log.e("获取POI检索结果", poiResult.toString());

            if (poiResult == null || (poiResult.error != SearchResult.ERRORNO.NO_ERROR)) {
                mLoagingLayout.setVisibility(View.GONE);//隐藏loading布局
                ToastUtil.TextToast("抱歉，未能找到结果");
                mActBaiduConfirm.setText("确定");
            } else {
                if ("取消".equals(act_baidu_search.getText().toString())) {
                    List<PoiInfo> poiLists = poiResult.getAllPoi();
                    if (StringUtil.isListNotEmpty(poiLists)) {
                        mLists2.clear();
                        for (int i = 0; i < poiLists.size(); i++) {
                            PoiInfo poiInfo = poiLists.get(i);
                            if (poiInfo.name != null && poiInfo.address != null && poiInfo.location != null) {
                                mLists2.add(new EntityAddressPoiBean(poiInfo.name, poiInfo.address, poiInfo.location.latitude + "", poiInfo.location.longitude + "", false));
                            }
                        }
                        if (StringUtil.isListNotEmpty(mLists2)) {
                            mListView2.setVisibility(View.VISIBLE);
                            mCommonAdapter2.notifyDataSetChanged();
                            InputTools.HideKeyboard(act_baidu_edit);//隐藏输入法
                            mActBaiduEditLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_white_top_2dp_bg));
                            mListView2.setSelection(0);
                        } else {
                            mListView2.setVisibility(View.GONE);
                        }
                    } else {
                        mListView2.setVisibility(View.GONE);
                    }
                }
            }

            dismissProgressDialog();//关闭dialog
            act_baidu_search.setText("搜索");
        }

        @Override
        public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
            // 获取Place详情页检索结果
            Log.e("获取Place详情页检索结果", poiDetailResult.toString());
        }

        @Override
        public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {
            // 获取到Poi室内结果
            Log.e("获取到Poi室内结果", poiIndoorResult.toString());
        }
    };

    private LocationClient mLocationClient = null;
    private BDLocationListener myListener = new MyLocationListener();
    private LatLng mLatLng;
    //    private LatLng mMoveLatLng;//移动位置的经纬度
    private String city;// 当前城市

    /**
     * 初始定位，获取当前位置经纬度
     */
    private void initLocation() {
        mLocationClient = new LocationClient(getApplicationContext());// 声明LocationClient类
        mLocationClient.registerLocationListener(myListener);// 注册监听函数
        initLocationClientOption();
        mLocationClient.start();
    }

    private void initLocationClientOption() {
        LocationClientOption option = new LocationClientOption();
        // 可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        // 可选，默认gcj02，设置返回的定位结果坐标系
        option.setCoorType("bd09ll");
        // 可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setScanSpan(1000);
        // 可选，设置是否需要地址信息，默认不需要
        option.setIsNeedAddress(true);
        // 可选，默认false,设置是否使用gps
        option.setOpenGps(true);
        // 可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setLocationNotify(true);
        // 可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationDescribe(true);
        // 可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIsNeedLocationPoiList(true);
        // 可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.setIgnoreKillProcess(false);
        // 可选，默认false，设置是否收集CRASH信息，默认收集
        option.SetIgnoreCacheException(false);
        // 可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        option.setEnableSimulateGps(false);
        mLocationClient.setLocOption(option);
    }


    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {

            city = location.getCity();

            gProvince = location.getProvince();
            gCity = location.getCity();
            gArea = location.getDistrict();

            mLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            mLocationClient.stop();//停止定位

            if (mLatLng == null) {
                ToastUtil.TextToast("获取位置失败");
                mLocationClient.start();//开启定位
            } else
                initBaiduMap(mLatLng);
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }

    }


    /**
     * 设置地图的监听事件，获取新的中点坐标后，再查询并显示周围地址
     */
    private void initBaiduMap(LatLng latlon) {

        mBaiduMap = mMapView.getMap();

        /*设置地图类型*/
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);// 普通地图:MAP_TYPE_NORMAL 卫星地图:MAP_TYPE_SATELLITE

        /*设置该经纬度在地图居中*/
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latlon);
        mBaiduMap.setMapStatus(msu);


        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.abi);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions().position(latlon).title("我的位置")//显示文字
                .draggable(true)//设置是否可以拖拽，默认为否
                .icon(bitmap);//图标覆盖物
        //在地图上添加Marker，并显示
        mMarker = (Marker) mBaiduMap.addOverlay(option);
        draw(latlon);//设置圆形覆盖物

        //设置缩放级别
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom(18).overlook(0).build()));

        /*地图状态改变事件监听*/
        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {// 地图状态变化的开始
            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {// 地图状态变化
            }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {// 在地图状态更改完成
                mLatLng = mapStatus.target;
                if (mLatLng == null) {
                    ToastUtil.TextToast("获取位置失败");
                    isLoading = false;
                    mLoagingLayout.setVisibility(View.GONE);
                    if (mLoadingProgressbar.getVisibility() == View.VISIBLE)
                        mLoadingProgressbar.setVisibility(View.GONE);
                    mLoadingText.setText("获取位置失败，点击重新加载");
                } else {
                    isLoading = true;
                    if (isClick) {
                        isClick = false;
                        mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(mLatLng));
                    }
                }
            }
        });


        isLoading = true;

        // 反地理编码得到地址信息
        mGeoCoder = GeoCoder.newInstance();
        mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(mLatLng));


        mGeoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {// 在得到地理编码结果
                if (geoCodeResult == null || (geoCodeResult.error != SearchResult.ERRORNO.NO_ERROR)) {
                    mLoagingLayout.setVisibility(View.GONE);//隐藏loading布局
                    ToastUtil.TextToast("抱歉，未能找到结果");
                    mActBaiduConfirm.setText("确定");
                } else {
                    mLatLng = geoCodeResult.getLocation();
                     /*设置该经纬度在地图居中*/
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLng(mLatLng));
                }
            }

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {// 在得到反向地理编码结果
                if (result == null || (result.error != SearchResult.ERRORNO.NO_ERROR)) {
                    mLoagingLayout.setVisibility(View.GONE);//隐藏loading布局
                    ToastUtil.TextToast("抱歉，未能找到结果");
                    mActBaiduConfirm.setText("确定");
                } else {
					
                    Log.e("----反地理结果province", result.getAddressDetail().province);
                    Log.e("----反地理结果city", result.getAddressDetail().city);
                    Log.e("----反地理结果district", result.getAddressDetail().district);
                    Log.e("----反地理结果street", result.getAddressDetail().street);
                    Log.e("----反地理结果streetNumber", result.getAddressDetail().streetNumber);
                    gProvince = result.getAddressDetail().province;
                    gCity = result.getAddressDetail().city;
                    gArea = result.getAddressDetail().district;
                    gStreet = result.getAddressDetail().street + result.getAddressDetail().streetNumber;
					
                    List<PoiInfo> poiLists = result.getPoiList();
                    if (poiLists != null && poiLists.size() > 0) {
                        mLists.clear();
                        for (int i = 0; i < poiLists.size(); i++) {
                            //                            {
                            //                                "address": "南昌市青山湖区南京东路106号",
                            //                                    "city": "南昌市",
                            //                                    "hasCaterDetails": false,
                            //                                    "isPano": false,
                            //                                    "location": {
                            //                                          "latitude": 28.688203362230396,
                            //                                          "latitudeE6": 28688203.362230398,
                            //                                          "longitude": 115.93786925325217,
                            //                                          "longitudeE6": 115937869.25325218
                            //                                     },
                            //                                "name": "南昌市国家税务局(南京东路)",
                            //                                "phoneNum": "",
                            //                                "postCode": "",
                            //                                "uid": "50f7d146ccc23fb873210bbb"
                            //                            }
                            PoiInfo poiInfo = poiLists.get(i);
                            mLists.add(new EntityAddressPoiBean(poiInfo.name, poiInfo.address, poiInfo.location.latitude + "", poiInfo.location.longitude + "", false));
                        }
                        mLoagingLayout.setVisibility(View.GONE);
                        mCommonAdapter.notifyDataSetChanged();
                        mListView.setSelection(0);
                        isLoading = false;
                    }
                }
            }
        });

        /**
         * 地图触摸监听
         */
        mBaiduMap.setOnMapTouchListener(new BaiduMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {

                    case MotionEvent.ACTION_DOWN: {
                        //按住事件发生后执行代码的区域
                        TranslateAnimation animation = new TranslateAnimation(0f, 0f, 0f, -20f);
                        animation.setFillAfter(true);
                        animation.setDuration(200);
                        mMarkerImg.startAnimation(animation);

                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        //移动事件发生后执行代码的区域
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        //松开事件发生后执行代码的区域
                        TranslateAnimation animation = new TranslateAnimation(0f, 0f, -20f, 0f);
                        animation.setFillAfter(true);
                        animation.setDuration(200);
                        mMarkerImg.startAnimation(animation);

                        if (mLatLng == null) {
                            ToastUtil.TextToast("获取位置失败");
                            isLoading = false;
                            mLoagingLayout.setVisibility(View.GONE);
                            if (mLoadingProgressbar.getVisibility() == View.VISIBLE)
                                mLoadingProgressbar.setVisibility(View.GONE);
                            mLoadingText.setText("获取位置失败，点击重新加载");
                        } else {
                            isLoading = true;
                            mLoagingLayout.setVisibility(View.VISIBLE);
                            if (mLoadingProgressbar.getVisibility() == View.GONE)
                                mLoadingProgressbar.setVisibility(View.VISIBLE);
                            mLoadingText.setText("加载中...");
                            mListView2.setVisibility(View.GONE);

                            mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(mLatLng));
                        }
                        break;
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mGeoCoder.destroy();
        mPoiSearch.destroy();

        // 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    private void draw(LatLng latLng) {
        //定义一个圆 ： 圆心+半径

        //1.创建自己
        CircleOptions circleOptions = new CircleOptions();
        //2.给自己设置数据
        circleOptions.center(latLng) //圆心
                .radius(100)//半径 单位米
                .fillColor(0x600098D6)//填充色
                .stroke(new Stroke(2, 0x0098D6));//边框宽度和颜色

        //3.把覆盖物添加到地图中
        mBaiduMap.addOverlay(circleOptions);
    }

    /*+++++++++++++++++++++++++++++++提示框+++++++++++++++++++++++++++++++++++++++++=*/

    /**
     * 显示Dialog
     */
    public void initDialogShow() {
        initViews();
        mDialog.show();
        mDialog.setContentView(R.layout.progress_bar_dialog);
    }

    /**
     * 初始化Dialog
     */
    private void initViews() {
        mDialog = new ProgressDialog(BaiduMao_SelectAddressActivity.this, R.style.Dialog);
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(true);
    }

    /**
     * 关闭Dialog
     */
    protected void dismissProgressDialog() {
        if (mDialog.isShowing()) {
            // 对话框显示中
            mDialog.dismiss();
        }
    }

    /**
     * 拦截返回按钮事件
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mActBaiduConfirm.getText().toString().equals("确定")) {
            finish();
        } else {
            mActBaiduConfirm.setText("确定");
            mListView2.setVisibility(View.GONE);
        }
    }
}
