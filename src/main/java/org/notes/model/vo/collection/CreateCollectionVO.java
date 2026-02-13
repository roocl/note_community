package org.notes.model.vo.collection;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("创建收藏夹结果")
@Data
public class CreateCollectionVO {
    @ApiModelProperty("新建收藏夹ID")
    private Integer collectionId;
}
