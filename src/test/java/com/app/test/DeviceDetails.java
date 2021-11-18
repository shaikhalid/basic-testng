package com.app.test;

public class DeviceDetails {

    private String os;
    private String os_version;
    private String device;
    private boolean realMobile;

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getOs_version() {
        return os_version;
    }

    public void setOs_version(String os_version) {
        this.os_version = os_version;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public boolean isRealMobile() {
        return realMobile;
    }

    public void setRealMobile(boolean realMobile) {
        this.realMobile = realMobile;
    }

    @Override
    public String toString() {
        return "Devices{" +
                "os='" + os + '\'' +
                ", os_version='" + os_version + '\'' +
                ", device='" + device + '\'' +
                ", realMobile='" + realMobile + '\'' +
                '}';
    }
}
