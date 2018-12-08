package com.leyou.item.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import com.leyou.common.pojo.PageResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.util.StringUtil;

import java.util.List;


@Service
public class BrandService {

    @Autowired
    private BrandMapper brandMapper;

    public PageResult<Brand> queryBrandByPage(Integer page, Integer rows, String sortBy, Boolean desc, String key) {

        //开启分页助手
        PageHelper.startPage(page,rows);

        Example example = new Example(Brand.class);

        Example.Criteria criteria1 = example.createCriteria();
        Example.Criteria criteria2 = example.createCriteria();

        if (StringUtils.isNoneBlank(key)) {
            criteria1.andLike("name","%"+key+"%");
            criteria2.andEqualTo("letter", key);
        }

        if (StringUtils.isNoneBlank(sortBy)) {
            example.setOrderByClause(sortBy+(desc ? " DESC" : " ASC"));
        }

        example.or(criteria2);

        //把查询结果操作一个统计函数
        Page<Brand> brandList = (Page<Brand>) this.brandMapper.selectByExample(example);

//        PageInfo<Brand> pageInfo = new PageInfo<>(brandList);

        return new PageResult<>(brandList.getTotal(), new Long(brandList.getPages()), brandList.getResult());
    }

    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        this.brandMapper.insertSelective(brand);

        cids.forEach(cid ->this.brandMapper.insertCategoryBrand(cid,brand.getId()));
    }

    @Transactional
    public void updataBrand(Brand brand, List<Long> cids) {
        this.brandMapper.updateByPrimaryKeySelective(brand);

        this.brandMapper.deleteBrandCategory(brand.getId());

        cids.forEach(cid->this.brandMapper.insertCategoryBrand(cid,brand.getId()));
    }

    public Brand queryBrandById(Long brandId) {
        return this.brandMapper.selectByPrimaryKey(brandId);
    }

    public List<Brand> queryBrandByCategory(Long cid) {
        return this.brandMapper.queryBrandByCategory(cid);
    }
}
