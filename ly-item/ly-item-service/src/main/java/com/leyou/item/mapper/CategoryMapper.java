package com.leyou.item.mapper;

import com.leyou.item.pojo.Category;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CategoryMapper extends Mapper<Category>,SelectByIdListMapper<Category,Long>{

    @Select("select c.* from tb_category_brand cb left join tb_category c on cb.category_id=c.id where cb.brand_id=#{bid}")
    List<Category> queryByBrandId(@Param("bid") Long bid);
}
