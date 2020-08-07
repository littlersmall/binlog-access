package com.littlersmall.controller;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.littlersmall.biz.BinlogProcessor;
import com.littlersmall.biz.consumer.RowDiffRawMessageConsumerPool;

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
    }

    @RequestMapping(value = "/urlMonitor")
    @ResponseBody
    public String urlMonitor() throws Exception {
        System.out.println("start services");

        return SERVER_NAME + " ok";
    }

    @RequestMapping(value = "/stop")
    @ResponseBody
    public String stop() throws Exception {
        System.out.println("stop services");
        binlogProcessor.stop();
        rowDiffRawMessageConsumerPool.stop();

        return SERVER_NAME + " ok";
    }
}
