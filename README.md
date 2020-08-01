# SQL

DROP TABLE IF EXISTS TRANSACTIONS;

CREATE TABLE TRANSACTIONS(TRANSACTIONID INT PRIMARY KEY,TRADEID INT NOT NULL,VERSION INT NOT NULL, SECURITYCODE CHAR(3) NOT NULL,QUANTITY INT NOT NULL,DML  CHAR(6) NOT NULL,OPT VARCHAR(4) NOT NULL);

CREATE UNIQUE INDEX TRADEVERSION ON TRANSACTIONS (TRADEID,VERSION);



DROP TABLE IF EXISTS SECURITYCODETB;

CREATE TABLE SECURITYCODETB(SECURITYCODE CHAR(3) PRIMARY KEY);

INSERT INTO SECURITYCODETB (SECURITYCODE) VALUES('REL'),('ITC'),('INF');



# 表结构设计说明

1.使用H2数据库，大小写未做处理，使用数据库默认设置，TRANSACTIONS表用做数据存根满足操作幂等性。

2.数据由其他系统对接而来，所以TRANSACTIONS表主键不加 IDENTITY(1,1)，也不自行生成主键。

3.TRADEID与VERSION作为联合唯一索引。

4.CHAR类型字段可使用INT，配合枚举。

5.数值暂时设计为INT，如果需要小数要用DECIMAL保证精确度。

6.SECURITYCODETB为SecurityCode 信息，可动态添加或删除。（暂时只提供查询接口）

# 架构模块功能说明

1.有些场景已考虑到，但未做具体实现（代码中TODO标出的部分）。

2.为防止中间人攻击，保证数据安全，已启用https(自签名证书，生产环境需申请正式证书)。生成合法token过程未做实现。若要防止接口恶意调用还需配合防火墙限制访问ip。

3.controller层提供一个方法接收数据，对调用方合法性与数据合法性进行验证，并返回计算后的position数据，以便进行展示（其他系统的数据可先发送到中间件中转）。并且对异常做了统一处理。

**4.service 的updatePosition方法包含核心逻辑，对并发及数据重复发送及数据到达无序的情况都有做处理，详见代码注释。使用hashMap保存计算后的position数据，未做持久化处理，服务重启后计算结果会丢失。**

5.使用编程式事务保存本地存根数据，主键与唯一索引保证操作幂等性。ORM使用Mybatis。

6.使用重入锁防止并发问题。

7.日志模块仅实现控制台输出功能，实际可写入文件或存入mongo。

8.提供了单元测试数据，也可使用swagger。（自签名证书某些浏览器禁止访问）

9.swagger     https://localhost:8080/swagger-ui.html

