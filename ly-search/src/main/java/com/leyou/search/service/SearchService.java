package com.leyou.search.service;

import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import com.leyou.item.pojo.SpecParam;
import com.leyou.search.clients.BrandClient;
import com.leyou.search.clients.CategoryClient;
import com.leyou.search.clients.SpecClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.utils.SearchRequest;
import com.leyou.search.utils.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SearchService {

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private SpecClient specClient;

    public SearchResult pageQuery(SearchRequest searchRequest) {

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        // 2.对结果进行筛选
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","skus","subTitle"}, null));

        //封装查询条件
        QueryBuilder query = buildBasicQueryWithFilter(searchRequest);

        //把查询条件加入查询构造器
        queryBuilder.withQuery(query);
        //3.分页
        queryBuilder.withPageable(PageRequest.of((searchRequest.getPage()-1), searchRequest.getSize()));
        //4.排序
        String sortBy = searchRequest.getSortBy();
        Boolean desc = searchRequest.getDescending();
        if (StringUtils.isNotBlank(sortBy)) {
            queryBuilder.withSort(SortBuilders.fieldSort(sortBy).order(desc ? SortOrder.DESC : SortOrder.ASC));
        }

        // 1.3、聚合
        String categoryAggName = "category"; // 商品分类聚合名称
        String brandAggName = "brand"; // 品牌聚合名称

        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));

        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));


        AggregatedPage<Goods> search = (AggregatedPage<Goods>) goodsRepository.search(queryBuilder.build());


        LongTerms categoryTerms = (LongTerms) search.getAggregation(categoryAggName);

        List<LongTerms.Bucket> categoryTermsBuckets = categoryTerms.getBuckets();

        //聚合后所有的分类的id
        List<Long> cids = new ArrayList<>();

        for (LongTerms.Bucket categoryTermsBucket : categoryTermsBuckets) {
            Long cid = categoryTermsBucket.getKeyAsNumber().longValue();

            cids.add(cid);
        }

        LongTerms brandTerms = (LongTerms) search.getAggregation(brandAggName);

        List<LongTerms.Bucket> brandTermsBuckets = brandTerms.getBuckets();

        //聚合后所有的品牌的id
        List<Long> brandIds = new ArrayList<>();
        for (LongTerms.Bucket brandTermsBucket : brandTermsBuckets) {
            brandIds.add(brandTermsBucket.getKeyAsNumber().longValue());
        }

        List<Category>  categories = new ArrayList<>();

        //根据分类的id查询分类对应的名字
        List<String> names = this.categoryClient.queryNameByIds(cids);

        //根据分类的id以及名字封装分类的对象结果
        for (int i = 0; i < names.size(); i++) {
            Category category = new Category();
            category.setId(cids.get(i));
            category.setName(names.get(i));

            categories.add(category);
        }

        List<Brand> brands = new ArrayList<>();

        brandIds.forEach(brandId->brands.add(brandClient.queryBrandById(brandId)));

        //规格参数过滤
        List<Map<String, Object>> specs = null;
        if (1 == categories.size()) {
            specs = getSpecs(categories.get(0).getId(),query);
        }

        return new SearchResult(search.getTotalElements(), new Long(search.getTotalPages()), search.getContent(),categories,brands,specs);
    }

    private QueryBuilder buildBasicQueryWithFilter(SearchRequest searchRequest) {

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        boolQuery.must(QueryBuilders.matchQuery("all", searchRequest.getKey()).operator(Operator.AND));

        BoolQueryBuilder filterQueryBuild = QueryBuilders.boolQuery();

        Map<String, String> filters = searchRequest.getFilter();

        Iterator<Map.Entry<String, String>> iterator = filters.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, String> next = iterator.next();
            String key = next.getKey();
            String value = next.getValue();
            if ("brandId".equals(key) || "cid3".equals(key)) {
                continue;
            } else {
                key = "specs." + key + ".keyword";
                filterQueryBuild.must(QueryBuilders.termQuery(key,value));
            }
        }
        boolQuery.filter(filterQueryBuild);

        return boolQuery;
    }

    //封装所有的规格参数的聚合
    private List<Map<String, Object>> getSpecs(Long cid,QueryBuilder query) {

        List<Map<String, Object>> specs =new ArrayList<>();
        //聚合规格参数
        //所有需要被聚合的规格参数集合
        List<SpecParam> specParams = specClient.querySpecParam(null, cid, true, null);

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();

        //规格参数做聚合时早考虑查询条件的变更
        queryBuilder.withQuery(query);

        specParams.forEach(specParam -> {
            String name = specParam.getName();
            //添加聚合条件
            queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs." + name + ".keyword"));
        });

        AggregatedPage<Goods> aggregatedPage = (AggregatedPage<Goods>) goodsRepository.search(queryBuilder.build());

        Map<String, Aggregation> aggregationMap = aggregatedPage.getAggregations().asMap();

        //解析聚合条件
        specParams.forEach(specParam -> {
            String name = specParam.getName();

            StringTerms aggregation = (StringTerms) aggregationMap.get(name);

            List<String> paramResults = new ArrayList<>();
            List<StringTerms.Bucket> buckets = aggregation.getBuckets();

            for (StringTerms.Bucket bucket : buckets) {
                paramResults.add(bucket.getKeyAsString());
            }

            Map<String, Object> spec = new HashMap<>();
            spec.put("k", name);
            spec.put("options", paramResults);

            specs.add(spec);

        });

        return specs;
    }
}
