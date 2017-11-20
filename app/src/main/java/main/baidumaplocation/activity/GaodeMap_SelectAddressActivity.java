package main.baidumaplocation.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import main.baidumaplocation.R;
import main.baidumaplocation.adapter.MyAdapter;
import main.baidumaplocation.adapter.SearchResultAdapter;
import main.baidumaplocation.utils.AMapLocUtils;
import main.baidumaplocation.utils.ToastUtil;

import static main.baidumaplocation.R.id.mListView2;


/**
 * ======================================================>
 *
 * @说明: 高德地图选择位置
 * @作者: Lino-082
 * @日期: 2017/6/10
 * @时间: 10:45
 * ======================================================>
 */
public class GaodeMap_SelectAddressActivity extends Activity implements PoiSearch.OnPoiSearchListener {
    @Bind(R.id.act_baidu_edit)
    EditText mEdit;//搜索输入框
    @Bind(R.id.act_baidu_search)
    Button mSearchTv;//搜索按钮
    @Bind(R.id.act_baidu_edit_layout)
    LinearLayout mEditLayout;//搜索框布局
    @Bind(R.id.act_aMap_back_location)
    ImageButton mAMapBackLocation;//回到定位位置按钮
    @Bind(mListView2)
    ListView mMListView2;//搜索结果listview
    @Bind(R.id.success_tv)
    TextView mSuccessTv;//确定

    private ProgressDialog mDialog;//提示框

    private String gAddress = "";//定位获取到的地址
    private String gProvince = "";//省
    private String gCity = "";//市
    private String gArea = "";//区

    private int whatlist = 1;// 判断展示数据的listview  1为默认第一个 地图下方listview 2为搜索框listview
    private ImageView mMarkerImg;

    private MarkerOptions mMarkerOptions;
    private MapView mMapView = null;
    private AMap aMap;
    private UiSettings mUiSettings;
    private MyLocationStyle myLocationStyle;
    private Marker marker;// 定位雷达小图标

    //poiSearch相关
    private PoiSearch poiSearch;
    private PoiSearch.Query query;
    boolean isPoiSearched = false; //是否进行poi搜索

    //listview
    private ListView ll;
    ArrayList<PoiItem> poiItemArrayList;
    MyAdapter adapter;//地图下方数据适配器
    SearchResultAdapter mSearchAdapter;//搜索结果适配器
    MyHandler myHandler;


    TextView title;
    ImageButton back;

    private double mCurrentLat;
    private double mCurrentLng;
    Map<String, String> currentInfo = new HashMap<>();
    int selectIndex = -1;
    ImageView currentSelectItem = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);

        findAllView();
        setAllViewOnclickLinster();
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);

        initAMap();
    }


    /**
     * 获取view对象，初始化一些对象
     */
    void findAllView() {
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);

        //地图铺盖物标记
        mMarkerImg = (ImageView) findViewById(R.id.Marker_img);

        //返回按钮
        back = (ImageButton) findViewById(R.id.back);

        //初始化listview
        ll = (ListView) findViewById(R.id.ll);

        //初始化poiItemArrayList集合
        poiItemArrayList = new ArrayList<>();
        //初始化适配器
        adapter = new MyAdapter(GaodeMap_SelectAddressActivity.this, poiItemArrayList, currentSelectItem, selectIndex);
        //设置适配器
        ll.setAdapter(adapter);
        //设置名称
        title = (TextView) findViewById(R.id.title);
        myHandler = new MyHandler();
    }

    /**
     * 设置点击事件
     */
    void setAllViewOnclickLinster() {


        //返回处理事件
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    finish();
            }
        });

        //完成事件
        mSuccessTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //点击确定的处理事件
                //获取数据并返回上一个activity即可

                if (mSuccessTv.getText().toString().trim().equals("确定")) {
                    if (selectIndex >= 0) {
                        Intent intent = new Intent();
                        intent.putExtra("sheng", gProvince);
                        intent.putExtra("shi", gCity);
                        intent.putExtra("qu", gArea);
                        intent.putExtra("address", gAddress);
                        intent.putExtra("lng", mCurrentLng + "");
                        intent.putExtra("lat", mCurrentLat + "");
                        setResult(RESULT_OK, intent);
                        finish();
                    } else {
                        ToastUtil.TextToast("请选择一个地址");
                    }
                } else {
                    mSuccessTv.setText("确定");
                    mMListView2.setVisibility(View.GONE);
                    whatlist = 1;
                }
            }
        });


        //listview点击事件
        ll.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                PoiItem item = poiItemArrayList.get(i);



                LatLng ll = new LatLng(item.getLatLonPoint().getLatitude(), item.getLatLonPoint().getLongitude());
                //清除所有marker等，保留自身
                CameraUpdate cu = CameraUpdateFactory.newLatLng(ll);
                aMap.animateCamera(cu);


                //存储当前点击位置
                selectIndex = i;

                //存储当前点击view，并修改view和上一个选中view的定位图标
                ImageView iv = (ImageView) view.findViewById(R.id.checked_iv);
                iv.setVisibility(View.VISIBLE);
                if (currentSelectItem != null) {
                    currentSelectItem.setVisibility(View.INVISIBLE);
                }
                currentSelectItem = iv;
                whatlist = 1;

                //赋值地址信息
                mCurrentLat = item.getLatLonPoint().getLatitude();
                mCurrentLng = item.getLatLonPoint().getLongitude();
                gProvince = item.getProvinceName();
                gCity = item.getCityName();
                gArea = item.getAdName();
                gAddress = item.getTitle();
            }
        });

        mMListView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PoiItem item = poiItemArrayList.get(position);

                LatLng ll = new LatLng(item.getLatLonPoint().getLatitude(), item.getLatLonPoint().getLongitude());
                CameraUpdate cu = CameraUpdateFactory.newLatLng(ll);
                aMap.animateCamera(cu);


                //存储当前点击位置
                selectIndex = position;

                //存储当前点击view，并修改view和上一个选中view的定位图标
                ImageView iv = (ImageView) view.findViewById(R.id.checked_iv);
                iv.setVisibility(View.VISIBLE);
                currentSelectItem = iv;
                if (currentSelectItem != null) {
                    currentSelectItem.setVisibility(View.VISIBLE);
                }
                mMListView2.setVisibility(View.GONE);
                whatlist = 1;
                myHandler.sendEmptyMessage(0x001);

                //赋值地址信息
                mCurrentLat = item.getLatLonPoint().getLatitude();
                mCurrentLng = item.getLatLonPoint().getLongitude();
                gProvince = item.getProvinceName();
                gCity = item.getCityName();
                gArea = item.getAdName();
                gAddress = item.getTitle();

                //修改输入框布局背景
                mEditLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_white_2dp_bg));

                mSuccessTv.setText("确定");
            }
        });
    }

    /**
     * 初始化高德地图
     */
    void initAMap() {
        //初始化地图控制器对象
        if (aMap == null) {
            aMap = mMapView.getMap();
            mUiSettings = aMap.getUiSettings();
        }

        //地图加载监听器
        aMap.setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
            @Override
            public void onMapLoaded() {
                aMap.setMyLocationEnabled(true);
                //设置缩放级别
                aMap.moveCamera(CameraUpdateFactory.zoomTo(aMap.getMaxZoomLevel()));
            }
        });

        //定位
        new AMapLocUtils().getLonLat(this, new AMapLocUtils.LonLatListener() {
            @Override
            public void getLonLat(AMapLocation aMapLocation) {
                //设置缩放级别
                aMap.moveCamera(CameraUpdateFactory.zoomTo(aMap.getMaxZoomLevel()));
                LatLng ll = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                CameraUpdate cu = CameraUpdateFactory.newLatLng(ll);
                aMap.moveCamera(cu);

                //赋值地址信息
                mCurrentLat = aMapLocation.getLatitude();
                mCurrentLng = aMapLocation.getLongitude();
                gProvince = aMapLocation.getProvince();
                gCity = aMapLocation.getCity();
                gArea = aMapLocation.getDistrict();
                gAddress = aMapLocation.getPoiName();

                selectIndex = 0;
            }
        });

        //地图状态改变监听（移动地图）
        aMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                Log.e("-----------", "onCameraChange: ");
            }

            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {
                Log.e("-----------", "onCameraChangeFinish: ");
                Log.e("onCameraChangeFinish:", "latitude: " + cameraPosition.target.latitude + "\nlongitude: " + cameraPosition.target.longitude);
                LatLng mLatlng = new LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude);
                if (isPoiSearched) {
                    //获取经纬度
                    mCurrentLat = cameraPosition.target.latitude;
                    mCurrentLng = cameraPosition.target.longitude;
                }
            }
        });

        myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类
        // 方法自5.1.0版本后支持
        myLocationStyle.showMyLocation(true);//设置是否显示定位小蓝点，用于满足只想使用定位，不想使用定位小蓝点的场景，设置false以后图面上不再有定位蓝点的概念，但是会持续回调位置信息。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);////连续定位、蓝点不会移动到地图中心点，定位点依照设备方向旋转，并且蓝点会跟随设备移动。
        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。

        myLocationStyle.strokeWidth(0.1f);//设置定位蓝点精度圈的边框宽度的方法。
        myLocationStyle.strokeColor(getResources().getColor(R.color.colorPrimaryDarkPoint));//设置定位蓝点精度圆圈的边框颜色的方法。
        myLocationStyle.radiusFillColor(getResources().getColor(R.color.colorPrimaryDark_50));//设置定位蓝点精度圆圈的填充颜色的方法。
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.navi_map_gps_locked));// 设置小蓝点的图标

        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        aMap.getUiSettings().setMyLocationButtonEnabled(false);//设置默认定位按钮是否显示，非必需设置。
        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        aMap.setMaxZoomLevel(aMap.getMaxZoomLevel());

        mUiSettings.setTiltGesturesEnabled(true);// 设置地图是否可以倾斜
        mUiSettings.setScaleControlsEnabled(true);// 设置地图默认的比例尺是否显示


        //------------------------------------------添加中心标记
        /*mMarkerOptions = new MarkerOptions();
        mMarkerOptions.draggable(true);//可拖放性
        mMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.car_error_iv_center));
        marker = aMap.addMarker(mMarkerOptions);
        ViewTreeObserver vto = mMapView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mMapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                marker.setPositionByPixels(mMapView.getWidth() >> 1, mMapView.getHeight() >> 1);
                marker.showInfoWindow();
            }
        });*/

        //地址监听事件
        aMap.setOnMyLocationChangeListener(new AMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                Log.e("-------------", "onMyLocationChange: ");
                Log.e("", "1");
                Log.e("", location.toString());
                if (!isPoiSearched) {

                    //存储定位数据
                    mCurrentLat = location.getLatitude();
                    mCurrentLng = location.getLongitude();
                    String[] args = location.toString().split("#");
                    for (String arg : args) {
                        String[] data = arg.split("=");
                        if (data.length >= 2)
                            currentInfo.put(data[0], data[1]);
                    }
                    //搜索poi
                    searchPoi("", 0, currentInfo.get("cityCode"), true);
                    //latitude=41.652146#longitude=123.427205#province=辽宁省#city=沈阳市#district=浑南区#cityCode=024#adCode=210112#address=辽宁省沈阳市浑南区创新一路靠近东北大学浑南校区#country=中国#road=创新一路#poiName=东北大学浑南校区#street=创新一路#streetNum=193号#aoiName=东北大学浑南校区#poiid=#floor=#errorCode=0#errorInfo=success#locationDetail=24 #csid:1cce9508143d493182a8da7745eb07b3#locationType=5

                    //将地图移动到定位点
                    aMap.animateCamera(CameraUpdateFactory.changeLatLng(new LatLng(location.getLatitude(), location.getLongitude())));

                }
            }
        });

        /**
         * 地图触摸监听
         */
        aMap.setOnMapTouchListener(new AMap.OnMapTouchListener() {
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

                        searchPoi("", 0, "", true);
                        break;
                    }
                }
            }
        });
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //点击返回键时，将浏览器后退
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mSuccessTv.getText().toString().equals("确定")) {
                finish();
            } else {
                mSuccessTv.setText("确定");
                mMListView2.setVisibility(View.GONE);
                whatlist = 1;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    // TODO: 2017/6/10 点击事件
    @OnClick({R.id.act_baidu_search, R.id.act_aMap_back_location, R.id.success_tv})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.act_baidu_search:
                //搜索按钮
                String key = mEdit.getText().toString();
                if (!key.trim().isEmpty()) {
                    if (currentSelectItem != null) {
                        currentSelectItem.setVisibility(View.INVISIBLE);
                    }
                    whatlist = 2;
                    //显示Dialog加载框
                    initDialogShow();
                    searchPoi(key, 0, currentInfo.get("cityCode"), false);
                }
                break;
            case R.id.act_aMap_back_location:
                //定位按钮
                new AMapLocUtils().getLonLat(this, new AMapLocUtils.LonLatListener() {
                    @Override
                    public void getLonLat(AMapLocation aMapLocation) {
                        //设置缩放级别
                        aMap.animateCamera(CameraUpdateFactory.zoomTo(aMap.getMaxZoomLevel()));
                        LatLng ll = new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                        CameraUpdate cu = CameraUpdateFactory.newLatLng(ll);
                        aMap.animateCamera(cu);

                        //对经纬度赋值
                        mCurrentLat = aMapLocation.getLatitude();
                        mCurrentLng = aMapLocation.getLongitude();
                        gProvince = aMapLocation.getProvince();
                        gCity = aMapLocation.getCity();
                        gArea = aMapLocation.getDistrict();
                        gAddress = aMapLocation.getPoiName();

                        selectIndex = 0;

                        //进行poi检索
                        searchPoi("", 0, "", true);
                        Log.e("--------地址信息", "onClick: \ngetProvince:" + aMapLocation.getProvince());
                        Log.e("--------地址信息", "onClick: \ngetCity:" + aMapLocation.getCity());
                        Log.e("--------地址信息", "onClick: \ngetAddress:" + aMapLocation.getAddress());
                        Log.e("--------地址信息", "onClick: \ngetDescription:" + aMapLocation.getDescription());
                        Log.e("--------地址信息", "onClick: \ngetDistrict:" + aMapLocation.getDistrict());
                        Log.e("--------地址信息", "onClick: \ngetLocationDetail:" + aMapLocation.getLocationDetail());
                        Log.e("--------地址信息", "onClick: \ngetStreet:" + aMapLocation.getStreet());
                        Log.e("--------地址信息", "onClick: \ngetFloor:" + aMapLocation.getFloor());
                        Log.e("--------地址信息", "onClick: \ngetPoiName:" + aMapLocation.getPoiName());
                    }
                });
                break;
            case R.id.success_tv:
                //确定按钮
                if (selectIndex != -1) {
                    PoiItem poiItem = poiItemArrayList.get(selectIndex);
                    ToastUtil.TextToast(poiItem.getProvinceName() + poiItem.getCityName() + poiItem.getAdName() + poiItem.getTitle() + "\n" + poiItem.getLatLonPoint());
                    Log.e("--------地址信息", "onClick: \ngetProvinceName:" + poiItem.getProvinceName());
                    Log.e("--------地址信息", "onClick: \ngetCityName:" + poiItem.getCityName());
                    Log.e("--------地址信息", "onClick: \ngetBusinessArea:" + poiItem.getBusinessArea());
                    Log.e("--------地址信息", "onClick: \ngetAdName:" + poiItem.getAdName());
                    Log.e("--------地址信息", "onClick: \ngetDirection:" + poiItem.getDirection());
                    Log.e("--------地址信息", "onClick: \ngetSnippet:" + poiItem.getSnippet());
                } else {
                    ToastUtil.TextToast("请选择一个地址");
                }
                break;
        }
    }


    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x001:
                    //加载listview1(地图下方)中数据
                    adapter.notifyDataSetChanged();

                    if (currentSelectItem != null) {
                        currentSelectItem.setVisibility(View.VISIBLE);
                    }
                    break;
                case 0x002:
                    //强制关闭键盘
                    closeKeyboard(GaodeMap_SelectAddressActivity.this);
                    //设置标题栏右侧按钮文字
                    mSuccessTv.setText("返回");
                    //修改输入框布局背景
                    mEditLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_white_top_2dp_bg));
                    //加载listview2(搜索框下方)中数据
                    mSearchAdapter.notifyDataSetChanged();
                    //关闭DIialog加载框
                    dismissProgressDialog();
                    break;
            }
        }
    }



    /**
     * 搜索poi
     *
     * @param key      关键字
     * @param pageNum  页码
     * @param cityCode 城市代码，或者城市名称
     * @param nearby   是否搜索周边
     */
    void searchPoi(String key, int pageNum, String cityCode, boolean nearby) {
        isPoiSearched = true;
        query = new PoiSearch.Query(key, "", cityCode);
        //keyWord表示搜索字符串，
        //第二个参数表示POI搜索类型，二者选填其一，
        //POI搜索类型共分为以下20种：汽车服务|汽车销售|
        //汽车维修|摩托车服务|餐饮服务|购物服务|生活服务|体育休闲服务|医疗保健服务|
        //住宿服务|风景名胜|商务住宅|政府机构及社会团体|科教文化服务|交通设施服务|
        //金融保险服务|公司企业|道路附属设施|地名地址信息|公共设施
        //cityCode表示POI搜索区域，可以是城市编码也可以是城市名称，也可以传空字符串，空字符串代表全国在全国范围内进行搜索
        query.setPageSize(50);// 设置每页最多返回多少条poiitem
        query.setPageNum(pageNum);//设置查询页码
        poiSearch = new PoiSearch(this, query);
        poiSearch.setOnPoiSearchListener(this);
        if (nearby)
            poiSearch.setBound(new PoiSearch.SearchBound(new LatLonPoint(mCurrentLat, mCurrentLng), 1500));//设置周边搜索的中心点以及半径
        poiSearch.searchPOIAsyn();
    }

    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        Log.e("--------------", "onPoiSearched: ");
        int index = 0;
        //填充数据，并更新listview
        if (poiResult != null) {
            Log.e("------poiResult!=null-", "onPoiSearched: ");
            ArrayList<PoiItem> result = poiResult.getPois();
            if (result.size() > 0) {
                Log.e("------result.size()", "onPoiSearched: ");
                poiItemArrayList.clear();
                //                selectIndex = -1;
                poiItemArrayList.addAll(result);
                if (whatlist == 1)
                    myHandler.sendEmptyMessage(0x001);
                else {
                    mSearchAdapter = new SearchResultAdapter(GaodeMap_SelectAddressActivity.this, poiItemArrayList, currentSelectItem, selectIndex);
                    mMListView2.setAdapter(mSearchAdapter);
                    mMListView2.setVisibility(View.VISIBLE);
                    mSuccessTv.setText("返回");
                    myHandler.sendEmptyMessage(0x002);
                }

                //赋值地址信息 取第一条数据
                mCurrentLat = poiItemArrayList.get(0).getLatLonPoint().getLatitude();
                mCurrentLng = poiItemArrayList.get(0).getLatLonPoint().getLongitude();
                gProvince = poiItemArrayList.get(0).getProvinceName();
                gCity = poiItemArrayList.get(0).getCityName();
                gArea = poiItemArrayList.get(0).getAdName();
                gAddress = poiItemArrayList.get(0).getTitle();
            }else{
                ToastUtil.TextToast("暂无数据");
            }
        }else{
            ToastUtil.TextToast("暂无数据");
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }


    /**
     * 强制关闭软键盘
     */
    public void closeKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            //如果开启
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            //关闭软键盘，开启方法相同，这个方法是切换开启与关闭状态的
        }
    }


     /*+++++++++++++++++++++++++++++++提示框+++++++++++++++++++++++++++++++++++++++++=*/

    /**
     * 显示Dialog🔒ด้้้้้็็็็
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
        mDialog = new ProgressDialog(GaodeMap_SelectAddressActivity.this, R.style.Dialog);
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

}
