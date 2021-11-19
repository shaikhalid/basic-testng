package com.web.parallel.test;

public class BrowserDetails {

    private String os;
    private String os_version;
    private String browser;
    private String device;
    private String browser_version;
    private boolean real_mobile;

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

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getBrowser_version() {
        return browser_version;
    }

    public void setBrowser_version(String browser_version) {
        this.browser_version = browser_version;
    }

    public boolean isReal_mobile() {
        return real_mobile;
    }

    public void setReal_mobile(boolean real_mobile) {
        this.real_mobile = real_mobile;
    }

    @Override
    public String toString() {
        return "BrowserDetails{" +
                "os='" + os + '\'' +
                ", os_version='" + os_version + '\'' +
                ", browser='" + browser + '\'' +
                ", device='" + device + '\'' +
                ", browser_version='" + browser_version + '\'' +
                ", real_mobile=" + real_mobile +
                '}';
    }
}
