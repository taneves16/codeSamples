package com.company.test.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDate;

/**
 * The Advertiser Bean contains all the data that is necessary for processing
 * 
 */
public class AdvertiserBean {

    private int networkId;
    private long advertiserId;
    private String path;

    private String networkName;

    private int adNetworkId;
    private String adNetworkName;
    private String username;
    private String password;

    private Map<String, String> loginProperties;

    private boolean isInvalidLogin;

    private Map<LocalDate, Integer> datewiseStatusMap;

    /*
     * Advertiser Bean for scraping accounts for Daily process.
     */
    public AdvertiserBean(int networkId, long advertiserId, int adNetworkId, String adNetworkName, String username, String password,
            String networkName,boolean isInvalidLogin, String path) {
        this.networkId = networkId;
        this.advertiserId = advertiserId;
        this.adNetworkId = adNetworkId;
        this.adNetworkName = adNetworkName;
        this.username = username;
        this.password = password;
        this.networkName = networkName;
        this.isInvalidLogin = isInvalidLogin;
        this.path = path;

        this.loginProperties = new HashMap<String, String>();
        this.datewiseStatusMap = new HashMap<LocalDate, Integer>();
    }

    public String getNetworkName() {
        return this.networkName;
    }

    public int getNetworkId() {
        return this.networkId;
    }

    public long getAdvertiserId() {
        return this.advertiserId;
    }

    public Map<String, String> getAllLoginProperties() {
        return this.loginProperties;
    }

    public String getLoginProperty(String key) {
        return this.loginProperties.get(key);
    }

    public void setLoginProperties(Map<String, String> loginProperties) {
        this.loginProperties.putAll(loginProperties);
    }

    public int getAdNetworkId() {
        return adNetworkId;
    }

    public String getAdNetworkName() {
        return adNetworkName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isInvalidLogin() {
        return isInvalidLogin;
    }
    
    public String getPath(){
        return path;
    }

    public Map<LocalDate, Integer> getDatewiseStatusMap() {
        return this.datewiseStatusMap;
    }

    public void setDatewiseStatusMap(Map<LocalDate, Integer> datewiseStatusMap) {
        this.datewiseStatusMap.putAll(datewiseStatusMap);
    }

    public void setStatusForDate(LocalDate date, int status) {
        this.datewiseStatusMap.put(date, status);
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (advertiserId ^ (advertiserId >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AdvertiserBean other = (AdvertiserBean) obj;
        if (advertiserId != other.advertiserId)
            return false;
        return true;
    }

}