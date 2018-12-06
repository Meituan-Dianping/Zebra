package com.dianping.zebra.administrator.controller;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Created by tong.xin on 18/1/19.
 */
public abstract class AbstractController {
    protected Logger logger = LoggerFactory.getLogger(this.getClass().getName());
    protected static final Gson defaultGson = new Gson();

}
