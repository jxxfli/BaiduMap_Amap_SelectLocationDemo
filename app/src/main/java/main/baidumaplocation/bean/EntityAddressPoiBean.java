package main.baidumaplocation.bean;

/**
 * 选择详细地址的时候暂时保存
 *
 * @author yangfei
 */
public class EntityAddressPoiBean {

    private String name;
    private String address;
    private String latitude;
    private String longitude;
    private boolean checked;

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public EntityAddressPoiBean() {
    }

    public EntityAddressPoiBean(String name, String address, String latitude, String longitude, boolean checked) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.checked = checked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

}
