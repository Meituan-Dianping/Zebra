package com.dianping.zebra.administrator.dto.jdbcref;

/**
 * Created by taochen on 2018/11/13.
 */
public class DBAddressDto {
    //ip+port
    private String address;

    private int readWeight;

    private int writeWeight;

    private String userName;

    private String password;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getReadWeight() {
        return readWeight;
    }

    public void setReadWeight(int readWeight) {
        this.readWeight = readWeight;
    }

    public int getWriteWeight() {
        return writeWeight;
    }

    public void setWriteWeight(int writeWeight) {
        this.writeWeight = writeWeight;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
