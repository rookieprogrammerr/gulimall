package com.zc.gulimall.product.entity.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class SpuItemBaseAttrGroupVo {
    private String groupName;
    private List<Attr> attrs;
}