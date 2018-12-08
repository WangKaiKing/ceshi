package com.leyou.search.controller;

import com.leyou.common.pojo.PageResult;
import com.leyou.search.pojo.Goods;
import com.leyou.search.service.SearchService;
import com.leyou.search.utils.SearchRequest;
import com.leyou.search.utils.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchController {

    @Autowired
    private SearchService searchService;

    @PostMapping("page")
    public ResponseEntity<SearchResult> page(@RequestBody SearchRequest searchRequest){

        SearchResult pageResult = searchService.pageQuery(searchRequest);

        if (pageResult != null && 0!=pageResult.getItems().size()) {
            return ResponseEntity.ok(pageResult);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
