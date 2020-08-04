package com.zhangtao.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * @author :  张涛 zhangtao
 * @version :  1.0
 * @createDate :  2020/8/1 09:48
 * @description :
 * @updateUser :
 * @updateDate :
 * @updateRemark :
 */
@ApiModel("TradeEntity对象")
public class TradeEntity {

    //事务id，主键
    @NotNull(message = "transactionId不能为空")
    @ApiModelProperty(value = "事务id，主键", required = true)
    private Integer transactionId;

    //交易id
    @ApiModelProperty(value = "交易id", required = true)
    private int tradeId;

    //版本号，从1开始
    @Min(value = 1, message = "version最小为1")
    @ApiModelProperty(value = "版本号，从1开始", required = true)
    private int version = 1;

    //安全标识符
    @NotNull(message = "安全标识符不能为空")
    @ApiModelProperty(value = "安全标识符", required = true, allowableValues = "REL,ITC,INF")
    private String securityCode;

    //交易数量
    @ApiModelProperty(value = "交易数量", required = true)
    private int quantity;

    //数据操作类型（新增、更新、取消）
    @NotNull(message = "数据操作类型不能为空")
    @ApiModelProperty(value = "数据操作类型", allowableValues = "INSERT,UPDATE,CANCEL", required = true)
    private String dml;

    //交易类型（买入、卖出）
    @NotNull(message = "交易类型不能为空")
    @ApiModelProperty(value = "交易类型", allowableValues = "BUY,SELL", required = true)
    private String opt;

    public Integer getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public int getTradeId() {
        return tradeId;
    }

    public void setTradeId(int tradeId) {
        this.tradeId = tradeId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getSecurityCode() {
        return securityCode;
    }

    public void setSecurityCode(String securityCode) {
        this.securityCode = securityCode;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getDml() {
        return dml;
    }

    public void setDml(String dml) {
        this.dml = dml;
    }

    public String getOpt() {
        return opt;
    }

    public void setOpt(String opt) {
        this.opt = opt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TradeEntity)) return false;
        TradeEntity that = (TradeEntity) o;
        return transactionId.equals(that.transactionId) &&
                getTradeId() == that.getTradeId() &&
                getVersion() == that.getVersion() &&
                getQuantity() == that.getQuantity() &&
                getSecurityCode().equals(that.getSecurityCode()) &&
                getDml().equals(that.getDml()) &&
                getOpt().equals(that.getOpt());
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId, getTradeId(), getVersion(), getSecurityCode(), getQuantity(), getDml(), getOpt());
    }

    @Override
    public String toString() {
        return "TradeEntity{" +
                "transactionId=" + transactionId +
                ", tradeId=" + tradeId +
                ", version=" + version +
                ", securityCode='" + securityCode + '\'' +
                ", quantity=" + quantity +
                ", dml='" + dml + '\'' +
                ", opt='" + opt + '\'' +
                '}';
    }

}
