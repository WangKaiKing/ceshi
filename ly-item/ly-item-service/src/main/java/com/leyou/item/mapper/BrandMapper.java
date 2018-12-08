package com.leyou.item.mapper;

import com.leyou.item.pojo.Brand;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BrandMapper extends Mapper<Brand>{

    @Insert("insert into tb_category_brand (category_id,brand_id) values (#{cid},#{bid})")
    void insertCategoryBrand(@Param("cid") Long cid,@Param("bid") Long bid);


    @Delete("delete from tb_category_brand where brand_id = #{bid}")
    void deleteBrandCategory(@Param("bid") Long bid);

    @Select("select b.* from tb_brand b left join tb_category_brand cb on b.id = cb.brand_id where cb.category_id=#{cid}")
    List<Brand> queryBrandByCategory(@Param("cid") Long cid);
}
