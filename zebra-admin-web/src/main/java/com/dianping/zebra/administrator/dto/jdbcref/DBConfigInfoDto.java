package com.dianping.zebra.administrator.dto.jdbcref;

/**
 * Created by taochen on 2018/11/12.
 */
public class DBConfigInfoDto {
    private int readDBNum;

    private int writeDBNum;

    public int getReadDBNum() {
        return readDBNum;
    }

    public void setReadDBNum(int readDBNum) {
        this.readDBNum = readDBNum;
    }

    public int getWriteDBNum() {
        return writeDBNum;
    }

    public void setWriteDBNum(int writeDBNum) {
        this.writeDBNum = writeDBNum;
    }
}
