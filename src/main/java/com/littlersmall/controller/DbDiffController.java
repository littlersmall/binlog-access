package com.littlersmall.controller;

import com.littlersmall.biz.BinlogProcessor;
import com.littlersmall.biz.consumer.RowDiffRawMessageConsumerPool;
import com.yidian.commerce.common.utils.frame.Log;
import com.yidian.commerce.common.utils.frame.LogMetric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;

/**
 * Created by littlersmall on 16/11/30.
 */
@Controller
@RequestMapping("/")
public class DbDiffController {
    private final static String SERVER_NAME = "db-diff-20170216";

    @Autowired
    BinlogProcessor binlogProcessor;

    @Autowired
    RowDiffRawMessageConsumerPool rowDiffRawMessageConsumerPool;

    @PostConstruct
    public void init() throws Exception {
        binlogProcessor.start();
        rowDiffRawMessageConsumerPool.start();
        LogMetric.start("db-diff");
    }

    @Log
    @RequestMapping(value = "/urlMonitor")
    @ResponseBody
    public String urlMonitor() throws Exception {
        System.out.println("start services");

        return SERVER_NAME + " ok";
    }

    @Log
    @RequestMapping(value = "/stop")
    @ResponseBody
    public String stop() throws Exception {
        System.out.println("stop services");
        binlogProcessor.stop();
        rowDiffRawMessageConsumerPool.stop();

        return SERVER_NAME + " ok";
    }
}
