package com.languo.mooccar.main.view;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.languo.mooccar.MoocCarApplication;
import com.languo.mooccar.R;
import com.languo.mooccar.account.model.AccountManagerImpl;
import com.languo.mooccar.account.model.IAccountManager;
import com.languo.mooccar.account.view.PhoneInputDialog;
import com.languo.mooccar.account.model.response.Account;
import com.languo.mooccar.common.databus.RxBus;
import com.languo.mooccar.common.http.IHttpClient;
import com.languo.mooccar.common.http.impl.OkHttpClientImpl;
import com.languo.mooccar.common.storage.SharedPreferencesDao;
import com.languo.mooccar.common.util.ToastUtil;
import com.languo.mooccar.main.presenter.MainPresenterImpl;

/**
 * 1、检查本地状态
 * 2、若用户没登录则登录
 * 3、登录之前校验验证码
 * 4、token 有效，使用 token 自动登录
 * <p>
 * TODO:地图初始化
 */
public class MainActivity extends AppCompatActivity implements IMainView, LocationSource,
        AMapLocationListener {

    private MainPresenterImpl mainPresenter;
    private AMap aMap;
    private MapView mMapView;
    OnLocationChangedListener mListener;
    AMapLocationClient mlocationClient;
    AMapLocationClientOption mLocationOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMapView = (MapView) findViewById(R.id.main_map_view);
        mMapView.onCreate(savedInstanceState);// 此方法必须重写
        init();

        IHttpClient httpClient = new OkHttpClientImpl();
        SharedPreferencesDao preferencesDao =
                new SharedPreferencesDao(MoocCarApplication.getApplication(),
                        SharedPreferencesDao.FILE_ACCOUNT);
        IAccountManager accountManager = new AccountManagerImpl(httpClient, preferencesDao);
        mainPresenter = new MainPresenterImpl(this, accountManager);
//        checkLoginState();
        mainPresenter.loginByToken();
        RxBus.getInstance().register(mainPresenter);
    }

    private void init() {
        if (aMap == null) {
            aMap = mMapView.getMap();
            setUpMap();
        }
    }

    private void setUpMap() {
        // 自定义系统定位小蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory
                .fromResource(R.drawable.location_marker));// 设置小蓝点的图标
        myLocationStyle.strokeColor(Color.BLACK);// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(100, 0, 0, 180));// 设置圆形的填充颜色
        // myLocationStyle.anchor(int,int)//设置小蓝点的锚点
        myLocationStyle.strokeWidth(1.0f);// 设置圆形的边框粗细
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // aMap.setMyLocationType()
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
     * 检查用户登录状态
     */
    private void checkLoginState() {
        //登录是否过期
        boolean tokenValid = false;
        //获取本地保存的登录信息
        SharedPreferencesDao sharedPreferencesDao =
                new SharedPreferencesDao(MoocCarApplication.getApplication(), SharedPreferencesDao.FILE_ACCOUNT);
        final Account account = (Account) sharedPreferencesDao.get(SharedPreferencesDao.KEY_ACCOUNT, Account.class);
        if (account != null) {
            if (account.getExpired() > System.currentTimeMillis()) {
                //token，没有过期
                tokenValid = true;
            }
        }
        if (!tokenValid) {
            //token过期，显示输入手机号对话框
            showPhoneInputDialog();
        } else {
            //自动登录，使用token
            mainPresenter.loginByToken();
        }
    }

    /**
     * 显示输入手机号的对话框
     */
    private void showPhoneInputDialog() {
        PhoneInputDialog phoneInputDialog = new PhoneInputDialog(MainActivity.this);
        phoneInputDialog.show();
    }


    @Override
    public void showLoading() {

    }

    @Override
    public void showError(int code, String msg) {
        switch (code) {
            case IAccountManager.SERVER_FAIL:
                showPhoneInputDialog();
                break;
            case IAccountManager.TOKEN_INVALID:
                ToastUtil.show(this, getString(R.string.token_invalid));
                showPhoneInputDialog();
                break;
        }
    }

    @Override
    public void showLoginSuc() {
        ToastUtil.show(this, getString(R.string.login_suc));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.getInstance().unRegister(mainPresenter);
        mMapView.onDestroy();
        if(null != mlocationClient){
            mlocationClient.onDestroy();
        }
    }


    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (mListener != null && aMapLocation != null) {
            if (aMapLocation != null
                    && aMapLocation.getErrorCode() == 0) {
                mListener.onLocationChanged(aMapLocation);// 显示系统小蓝点
            } else {
                String errText = "定位失败," + aMapLocation.getErrorCode() + ": " + aMapLocation.getErrorInfo();
                Log.e("AmapErr", errText);
            }
        }
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mlocationClient == null) {
            //初始化定位
            mlocationClient = new AMapLocationClient(this);
            //初始化定位参数
            mLocationOption = new AMapLocationClientOption();
            //设置定位回调监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();//启动定位
        }
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }
}
