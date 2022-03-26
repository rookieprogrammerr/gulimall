package com.zc.common.to.es;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Attrs implements Serializable {
    private Long attrId;

    private String attrName;

    private String attrValue;
}
