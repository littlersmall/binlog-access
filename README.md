# binlog-access
![mysql.jpeg](http://upload-images.jianshu.io/upload_images/1397675-ff7595dd58a2cfbb.jpeg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

# 20200807 更新  
1 调整依赖，使项目可以顺利编译
2 该项目依赖 redis-access 和 rabbitmq-access，需要 git clone https://github.com/littlersmall/rabbitmq-access.git 和 git clone https://github.com/littlersmall/redis-access.git 之后在本地 mvn clean install 两个工程。然后才可以正确编译运行该工程

一 背景

1 binlog定义

**binlog基本定义**：二进制日志，也成为二进制日志，记录对数据发生或潜在发生更改的SQL语句，并以二进制的形式保存在磁盘中。

**作用**：MySQL的作用类似于Oracle的归档日志，可以用来查看数据库的变更历史（具体的时间点所有的SQL操作）、数据库增量备份和恢复（增量备份和基于时间点的恢复）、Mysql的复制（主主数据库的复制、主从数据库的复制）。

2 开启binlog

找到mysql的配置文件，linux下一般为my.cnf在/etc 下，window下一般为my.ini
在[mysqld]下添加
```
log-bin=mysql-bin
binlog_format="ROW"
```
添加完成后重启mysql
```
mysql> show binary logs;
```
会显示如下：
```
+------------------+-----------+
| Log_name         | File_size |
+------------------+-----------+
| mysql-bin.000001 |       732 |
+------------------+-----------+
```
3 binlog格式

mysql的binlog有多种格式
a Statement：每一条会修改数据的sql都会记录在binlog中

b Row:不记录sql语句上下文相关信息，仅保存哪条记录被修改

c Mixed:是以上两种level的混合使用，一般的语句修改使用statment格式保存binlog，如一些函数，statement无法完成主从复制的操作，则采用row格式保存binlog

**注：我们的binlog-access只支持row格式的解析**

二 binlog-accessor

由于我们的项目中需要实时获取mysql中某些字段的修改，考虑到添加触发器或者在代码层面监听修改过大，因此最终决定通过监听myslq的binlog来完成。
调研了一些现有的方案后，最终基于**open-replicator**实现了一套binlog的监听及解析程序。

1 open-replicator

open-replicator是一个开源的binlog解析框架。
>https://github.com/whitesock/open-replicator

它的主要**原理**是将自己伪装成一台mysql的备库从而从主库获取binlog数据。
比如删除mysql中的一条数据，open-replicator会返回：
```
DeleteRowsEventV2[header=BinlogEventV4HeaderImpl[timestamp=1488177443000,eventType=32,serverId=1,eventLength=72,nextPosition=1653,flags=0,timestampOfReceipt=1488177443997],tableId=116,reserved=1,extraInfoLength=2,extraInfo=<null>,columnCount=5,usedColumns=11111,rows=[Row[columns=[13, 0, 0, 0, 100]]]]
```
这个返回结果基本和binlog的格式完全一样，但对于我们实际的使用中，有许多不方便的地方。
比如：tableId是mysql内部使用的，如果对外使用，我们需要将tableId翻译为tableName。还有row的值，只描述了原始值，并没有描述列的字段名。鉴于此，我们需要对open-replicator做诸多的加工。

2 加工数据

我们只关注binlog中的4种event类型
a tableMapEvent，该event主要描述tableId和tableName的对应

b insertEvent，该event描述insert事件

c updateEvent，该event描述update事件

d deleteEvent，该event描述delete事件

加工分为两个截断

a 通过tableId获取tableName(解析tableMapEvent)

b 获取每个字段的列名，主要功过调用 desc tableName 得到

加工后的输出结果为一个bean：
```
@Data
public class RowDiffModel {
    long timestamp;
    String tableName;
    List<String> pkColumnName = new ArrayList<>();  //主键列
    List<Object> pk = new ArrayList<>();
    int type;  //1 新建 //2 更新 //3 删除
    List<String> diffColumns = new ArrayList<>();
    Map<String, Object> preValue = new HashMap<>();
    Map<String, Object> newValue = new HashMap<>();
}
```
比如上条的删除事件，加工后返回的结果为：
```
RowDiffModel(timestamp=1488177443000, tableName=lx_charge.user_fund, pkColumnName=[], pk=[], type=3, diffColumns=[user_id, invest, extend, rebate, balance], preValue={extend=0, balance=100, user_id=13, rebate=0, invest=0}, newValue={})
```
3 订阅数据

我们将加工后的binlog发送到rabbitmq的一个topic中，所有的需求放订阅需要的数据即可。这里贴一个订阅的示例:
```
@Service
public class RowDiffRawMessageConsumerPool {
        private static final String EXCHANGE = "db-diff";
        private static final String ROUTING = "row-diff";
        private static final String QUEUE = "row-diff-raw";

        @Autowired
        ConnectionFactory connectionFactory;

        private ThreadPoolConsumer<RowDiffModel> threadPoolConsumer;

        @PostConstruct
        public void init() {
            MQAccessBuilder mqAccessBuilder = new MQAccessBuilder(connectionFactory);
            MessageProcess<RowDiffModel> messageProcess = message -> {
                System.out.println("received: " + message);

                return new DetailRes(true, "");
            };

            threadPoolConsumer = new ThreadPoolConsumer.ThreadPoolConsumerBuilder<RowDiffModel>()
                    .setThreadCount(Constants.CONSUMER_THREAD_COUNT).setIntervalMils(Constants.INTERVAL_MILS)
                    .setExchange(EXCHANGE).setRoutingKey(ROUTING).setQueue(QUEUE).setType("topic")
                    .setMQAccessBuilder(mqAccessBuilder).setMessageProcess(messageProcess)
                    .build();
        }

        public void start() throws IOException {
            threadPoolConsumer.start();
        }

        public void stop() {
            threadPoolConsumer.stop();
        }
}
```
在本例中，将所有的binlog直接打印。
关于rabbitmq的使用请参考
>http://www.jianshu.com/p/4112d78a8753

4 高可用性

任何一个项目都需要考虑高可用性，尤其是一些偏底层的模块。在binlog-access中，我们从两方面考虑高可用性

a mysql的可用性。我们需要考虑mysql挂掉，网络异常的情况。我们对原始的open-replicator做了一个加强，重写了它的start方法，保证在各种情况下的自动重试

```
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
```
b 多机器部署。为了避免单点效应，我们需要将binlog-access支持多机部署。这里引入redis来保证不会发送重复数据到topic中，主要通过日志偏移去重：
```
    @Log
    public DetailRes send(long pos, List<RowDiffModel> rowDiffModels) {
        if (redisCache.cacheIfAbsent("binlog:" + pos, Constants.TIMESTAMP_VALID_TIME)) {
            DetailRes detailRes = new DetailRes(true, "");

            for (RowDiffModel rowDiffModel : rowDiffModels) {
                if (detailRes.isSuccess()) {
                    String dbName = rowDiffModel.getTableName().split("\\.")[0].toLowerCase();

                    if (dbSet.isEmpty()) {
                        detailRes = messageSender.send(rowDiffModel);
                    } else {
                        if (dbSet.contains(dbName)) {
                            detailRes = messageSender.send(rowDiffModel);
                        }
                    }
                } else {
                    break;
                }
            }

            return detailRes;
        } else {
            return new DetailRes(true, "");
        }
    }
```
关于redis的使用，请参考
>http://www.jianshu.com/p/fa036f364ae2

5 项目依赖
a open-replicator
```
<dependency>
    <groupId>com.flipkart</groupId>
    <artifactId>open-replicator</artifactId>
    <version>1.0.8</version>
</dependency>
```
b rabbitmq-access
```
<dependency>
	<groupId>com.littlersmall.rabbitmq-access</groupId>
	<artifactId>rabbitmq-access</artifactId>
	<version>1.0-SNAPSHOT</version>
</dependency>
```
**注**:该模块需要自己打包成jar包导入项目或者deploy在自己的代码库中

c redis-access
```
<dependency>
	<groupId>com.littlersmall.redis-access</groupId>
	<artifactId>redis-access</artifactId>
	<version>1.0-SNAPSHOT</version>
</dependency>
```
**注**:同上

三 binlog-access的使用

1 准备好所依赖的jar包(或deploy在自己的代码库中，rabbitmq-access & redis-access)

2 安装好rabbitmq和redis

3 **确定所监听的mysql开启了binlog，且binlog的格式为ROW**

4 配置文件(resources/application.properties)，如下

```
#db
db.host=127.0.0.1
db.port=3306
db.username=root
db.password=root
db.url=jdbc:mysql://${db.host}:${db.port}/?useUnicode=true&characterEncoding=utf8

#rabbitmq
rabbit.ip=127.0.0.1
rabbit.port=5672
rabbit.user_name=guest
rabbit.password=guest

#redis
redis.ip=127.0.0.1
redis.port=6379

#监听的库','分割，例如： diff.db=user,info，不配置则表示监听全部库
diff.db=
```
5 权限配置。需要确保mysql账户拥有**备库的全部权限**+所有表的**读**权限

6 项目启动：java -jar binlog-access.jar

项目代码见
>https://github.com/littlersmall/binlog-access

路过的麻烦点个星星，谢谢(*^__^*)
