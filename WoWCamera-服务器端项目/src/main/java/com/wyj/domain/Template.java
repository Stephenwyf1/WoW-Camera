package com.wyj.domain;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;

@Data
public class Template {

    @Id
    @KeySql(useGeneratedKeys = true)
    private Long id;
    private Long userId;
    private String name;
    private String img_uri;
    private String uuid;
}
