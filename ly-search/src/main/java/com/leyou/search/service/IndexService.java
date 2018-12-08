package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.search.clients.CategoryClient;
import com.leyou.search.clients.GoodsClient;
import com.leyou.search.clients.SpecClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class IndexService {
    //TODO 首先根据spu的分页查询方法查到一堆sku，取出其中一个进行变更为goods

    //TODO 对于sku

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecClient specClient;

    @Autowired
    private GoodsRepository goodsRepository;

    public Goods buildGoods(SpuBo spuBo) {
        Goods goods = new Goods();

        //把spu中的值对应复制给goods
        BeanUtils.copyProperties(spuBo,goods);

        //goods中还缺四个
        List<String> names = categoryClient.queryNameByIds(Arrays.asList(spuBo.getCid1(), spuBo.getCid2(), spuBo.getCid3()));


        String all = spuBo.getTitle() + StringUtils.join(names," ");

        goods.setAll(all);

        List<Sku> skus = this.goodsClient.querySkuBySpuId(spuBo.getId());

        List<Map<String,Object>> skuMaps = new ArrayList<>();

        List<Long> price = new ArrayList<>();

        skus.forEach(sku -> {
            Map<String,Object> skuMap = new HashMap<>();
            skuMap.put("id",sku.getId());
            skuMap.put("title",sku.getTitle());
            skuMap.put("price",sku.getPrice());
            skuMap.put("image",sku.getImages());
            skuMaps.add(skuMap);
            price.add(sku.getPrice());
        });

        goods.setSkus(JsonUtils.serialize(skuMaps));

        goods.setPrice(price);


        //首先根据分类查询所有的可搜索的规格参数；
        List<SpecParam> specParams = this.specClient.querySpecParam(null, spuBo.getCid3(), true, null);


        //spuDetail详情
        SpuDetail spuDetail = this.goodsClient.querySpuDetailById(spuBo.getId());

        //通用规格参数对应的值
        Map<Long,String> genericSpecMap = JsonUtils.parseMap(spuDetail.getGenericSpec(),Long.class,String.class);

        //特有规格参数
        Map<Long,List<String>> specialSpecMap = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() {
        });

        //处理规格参数显示问题，默认显示id+值，处理后显示id对应的名称+值
        Map<String, Object> specs = new HashMap<>();


        for (SpecParam specParam : specParams) {
            String name = specParam.getName();//cpu品牌，


            Object value = null;

            //获取是否通用
            if (specParam.getGeneric()){
                value = genericSpecMap.get(specParam.getId());

                if (null==value){
                    value = "其他";
                    continue;
                }

                //如果通用属性对应的为数值，此时应该去选择这个值对应的具体区间
                if (specParam.getNumeric()){
                    value =  chooseSegment(value.toString(),specParam);
                }
            }else{//特有规格参数
                value = specialSpecMap.get(specParam.getId());
            }

            specs.put(name,value);
        }

        goods.setSpecs(specs);

        return goods;
    }

    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    public void createIndex(Long id) {
        Spu spu = goodsClient.querySpuById(id);

        SpuBo spuBo = new SpuBo();

        BeanUtils.copyProperties(spu,spuBo);

        goodsRepository.save(buildGoods(spuBo));
    }

    public void deleteIndex(Long id) {
        this.goodsRepository.deleteById(id);
    }
}
