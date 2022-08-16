package com.zc.common.to.mq;

import lombok.Data;

import java.util.List;

@Data
public class StockLockedTo {
    private Long id; //库存工作单id
    private StockDetailTo detailTo; //库存工作单详情id
}
