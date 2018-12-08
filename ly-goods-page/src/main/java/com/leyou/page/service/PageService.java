package com.leyou.page.service;

import com.leyou.item.pojo.*;
import com.leyou.page.clients.BrandClient;
import com.leyou.page.clients.CategoryClient;
import com.leyou.page.clients.GoodsClient;
import com.leyou.page.clients.SpecClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PageService {

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecClient specClient;

   /* @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private BrandClient brandClient;*/

    public Map<String, Object> loadModel(Long id) {
        Map<String, Object> map = new HashMap<>();

        //spu
        Spu spu = goodsClient.querySpuById(id);

        map.put("spu", spu);

        //spuDetail
        SpuDetail spuDetail = goodsClient.querySpuDetailById(id);

        map.put("spuDetail", spuDetail);

        //skus
        List<Sku> skus = goodsClient.querySkuBySpuId(id);

        map.put("skus", skus);

        /*// 准备商品分类
        List<Category> categories = getCategories(spu);
        if (categories != null) {
            map.put("categories", categories);
        }

        // 准备品牌数据
        List<Brand> brands = this.brandClient.queryBrandByIds(
                Arrays.asList(spu.getBrandId()));
        map.put("brand", brands.get(0));
*/

        //所有的规格参数
        List<SpecGroup> specGroups = specClient.queryGroupByCid(spu.getCid3());

        map.put("specGroups",specGroups);

        //所有的特有规格参数
        List<SpecParam> specParams = specClient.querySpecParam(null, spu.getCid3(), null, false);

        //处理成id:name的键值对
        Map<Long, String> paramMap = new HashMap<>();

        for (SpecParam specParam : specParams) {
            paramMap.put(specParam.getId(),specParam.getName());
        }

        map.put("paramMap", paramMap);

        return map;
    }
}
