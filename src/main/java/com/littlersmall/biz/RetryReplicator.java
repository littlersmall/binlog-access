package com.littlersmall.biz;

import com.google.code.or.OpenReplicator;
import com.littlersmall.biz.meta.BinlogMetaBuilder;
import com.littlersmall.model.BinlogMeta;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * Created by littlersmall on 17/1/4.
 */
@Slf4j
public class RetryReplicator extends OpenReplicator {
    private boolean stop = false;
    private BinlogMetaBuilder binlogMetaBuilder;

    public void stop() {
        this.stop = true;
    }

    public RetryReplicator(BinlogMetaBuilder binlogMetaBuilder) {
        this.binlogMetaBuilder = binlogMetaBuilder;
    }

    @Override
    public void start() {
        new Thread(() -> {
            while (!stop) {
                try {
                    if (!isRunning()) {
                        if (this.transport != null
                                || this.binlogParser != null) {
                            this.stopQuietly(0, TimeUnit.SECONDS);
                            this.transport = null;
                            this.binlogParser = null;
                        }

                        BinlogMeta binlogMeta = binlogMetaBuilder.getBinlogMeta();
                        setBinlogFileName(binlogMeta.getBinlogName());
                        setBinlogPosition(binlogMeta.getPos());

                        log.info(binlogMeta.toString());

                        super.start();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        Thread.sleep(10 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
