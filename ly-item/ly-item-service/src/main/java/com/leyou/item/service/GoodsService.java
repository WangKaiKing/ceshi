package com.leyou.item.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.item.pojo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    public PageResult<SpuBo> querySpuByPage(String key, Boolean saleable, Integer page, Integer rows) {
        // 1、查询SPU
        // 分页
        PageHelper.startPage(page, rows);
        // 创建查询条件
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        // 是否模糊查询
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("title", "%" + key + "%");
        }
        // 是否过滤上下架
        if (saleable != null) {
            criteria.andEqualTo("saleable", saleable);
        }

        Page<Spu> spuPage = (Page<Spu>) this.spuMapper.selectByExample(example);

        List<SpuBo> spuBoList = new ArrayList<>();

        //TODO 把spu转换为spuBo
        List<Spu> spuList = spuPage.getResult();
        spuList.forEach(spu -> {
            SpuBo spuBo = new SpuBo();
            // 属性拷贝
            BeanUtils.copyProperties(spu, spuBo);
            // 2、查询spu的商品分类名称,要查三级分类
            List<String> names = this.categoryService.queryNamesByIds(Arrays.asList(spu.getCid1(),spu.getCid2(),spu.getCid3()));
            // 将分类名称拼接后存入
            spuBo.setCname(StringUtils.join(names,"/"));
            // 3、查询spu的品牌名称
            Brand brand = this.brandService.queryBrandById(spu.getBrandId());
            spuBo.setBname(brand.getName());

            spuBoList.add(spuBo);
        });
        return new PageResult<>(spuPage.getTotal(),new Long(spuPage.getPages()),spuBoList);
    }

    @Transactional
    public void saveGoods(SpuBo spuBo) {
        //TODO 保存商品数据
        spuBo.setSaleable(true);
        spuBo.setValid(true);
        spuBo.setCreateTime(new Date());
        spuBo.setLastUpdateTime(spuBo.getCreateTime());

        this.spuMapper.insertSelective(spuBo);

        // 保存spu详情
        spuBo.getSpuDetail().setSpuId(spuBo.getId());
        this.spuDetailMapper.insert(spuBo.getSpuDetail());

        // 保存sku和库存信息
        spuBo.getSkus().forEach(sku -> {
            sku.setSpuId(spuBo.getId());
            sku.setCreateTime(spuBo.getCreateTime());
            sku.setLastUpdateTime(spuBo.getCreateTime());
            //保存sku
            this.skuMapper.insertSelective(sku);

            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            this.stockMapper.insertSelective(stock);
        });
        amqpTemplate.convertAndSend("item.insert",spuBo.getId());
    }

    public SpuDetail querySpuDetailById(Long spuid) {
        return this.spuDetailMapper.selectByPrimaryKey(spuid);
    }

    public List<Sku> querySkuBySpuId(Long id) {

        Sku sku = new Sku();
        sku.setSpuId(id);
        // 查询sku
        List<Sku> skus = this.skuMapper.select(sku);

        skus.forEach(sku1 -> {
            // 同时查询出库存
            sku1.setStock(this.stockMapper.selectByPrimaryKey(sku1.getId()).getStock());
        });
        return skus;
    }

    @Transactional
    public void updateGoods(SpuBo spuBo) {

        //其他可能变可能不变，但是修改时间一定要变
        spuBo.setLastUpdateTime(new Date());

        //直接修改spu
        this.spuMapper.updateByPrimaryKeySelective(spuBo);

        //直接修改spuDetail
        this.spuDetailMapper.updateByPrimaryKeySelective(spuBo.getSpuDetail());

        //处理skus

        //sku将有三种，新增的，修改的，下架的

        //本次提交过来全新的sku
        List<Sku> allNewSkus = spuBo.getSkus();

        //取出新的sku的所有的id
        List<Long> allNewIds = new ArrayList<>();

        allNewSkus.forEach(newSku->allNewIds.add(newSku.getId()));

        //数据库中查询的原来的sku
        List<Sku> oldSkus = this.querySkuBySpuId(spuBo.getId());


        List<Long> oldSkuIds = new ArrayList<>();
        //原来所有sku的id集合
        oldSkus.forEach(oldSku->oldSkuIds.add(oldSku.getId()));

        //要被下架的sku
        List<Sku> toBeDeleteSku = new ArrayList<>();

        //如果某个id在原来的sku集合中存在，但是在新的sku集合中不存在，就表明要被下架了
        oldSkus.forEach(oldSku->{
            if (!allNewIds.contains(oldSku.getId())){
                toBeDeleteSku.add(oldSku);
            }
        });

        allNewSkus.forEach(newSku->{
            //把新增的sku进行了保存
            if (newSku.getId()==null){

                newSku.setSpuId(spuBo.getId());


                newSku.setCreateTime(new Date());

                newSku.setLastUpdateTime(newSku.getCreateTime());

                //保存sku
                this.skuMapper.insertSelective(newSku);

                Stock stock = new Stock();

                stock.setSkuId(newSku.getId());

                stock.setStock(newSku.getStock());

                this.stockMapper.insertSelective(stock);

                //新来的sku的id在旧的skuId中包含，说明就是要修改的
            }else if (oldSkuIds.contains(newSku.getId())){

                newSku.setSpuId(spuBo.getId());

                newSku.setLastUpdateTime(new Date());

                //修改sku
                this.skuMapper.updateByPrimaryKeySelective(newSku);

                Stock stock = new Stock();

                stock.setSkuId(newSku.getId());

                stock.setStock(newSku.getStock());

                //根据sku修改库存
                this.stockMapper.updateByPrimaryKey(stock);
            }
        });

        //下架sku
        toBeDeleteSku.forEach(sku -> {
            sku.setLastUpdateTime(new Date());
            sku.setEnable(false);

            sku.setSpuId(spuBo.getId());

            this.skuMapper.updateByPrimaryKeySelective(sku);

        });

        amqpTemplate.convertAndSend("item.update",spuBo.getId());
    }

    public Spu querySpuById(Long spuId) {
        return this.spuMapper.selectByPrimaryKey(spuId);
    }
}