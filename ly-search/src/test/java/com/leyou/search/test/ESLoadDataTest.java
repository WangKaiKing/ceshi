package com.leyou.search.test;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.search.clients.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.service.IndexService;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SearchService.class)
public class ESLoadDataTest {

    @Autowired
    private IndexService indexService;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private GoodsRepository goodsRepository;
    @Test
    public void loadData(){

            // 查询spu
            PageResult<SpuBo> result = this.goodsClient.querySpuByPage(null, null, 1, Integer.MAX_VALUE);
            result.getItems().forEach(spuBo -> this.goodsRepository.save(indexService.buildGoods(spuBo)));


    }
}
