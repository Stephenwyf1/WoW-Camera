package com.wyj.domain;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.Id;

@Data //lombok的注解，data注解包含get/set/hashcode/equals/tostring等方法
public class User {

    @Id //@Id 标注用于声明一个实体类的属性映射为数据库的主键列。
    @KeySql(useGeneratedKeys = true) //主键回填 即插入数据后 把数据库自增生成的主键返回给该对象
    private Long id;
    private String name;
    private String phoneNumber;

}
