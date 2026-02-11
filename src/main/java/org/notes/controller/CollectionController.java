package org.notes.controller;

import org.notes.model.base.ApiResponse;
import org.notes.model.dto.collection.CollectionQueryParams;
import org.notes.model.dto.collection.CreateCollectionBody;
import org.notes.model.dto.collection.UpdateCollectionBody;
import org.notes.model.vo.collection.CollectionVO;
import org.notes.model.vo.collection.CreateCollectionVO;
import org.notes.service.CollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.notes.model.base.EmptyVO;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CollectionController {

    @Autowired
    private CollectionService collectionService;

    @GetMapping("/collections")
    public ApiResponse<List<CollectionVO>> getCollections(
            @Valid
            CollectionQueryParams queryParams) {
        return collectionService.getCollections(queryParams);
    }

    @PostMapping("/collections")
    public ApiResponse<CreateCollectionVO> createCollection(
            @Valid
            @RequestBody
            CreateCollectionBody requestBody) {
        return collectionService.createCollection(requestBody);
    }

    @DeleteMapping("/collections/{collectionId}")
    public ApiResponse<EmptyVO> deleteCollection(
            @PathVariable
            @Min(value = 1, message = "collectionId 必须为正整数")
            Integer collectionId) {
        return collectionService.deleteCollection(collectionId);
    }

    @PostMapping("/collections/batch")
    public ApiResponse<EmptyVO> batchModifyCollection(
            @Valid
            @RequestBody
            UpdateCollectionBody collectionBody) {
        return collectionService.batchModifyCollection(collectionBody);
    }
}