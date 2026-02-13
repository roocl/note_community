package org.notes.model.base;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@ApiModel("带Token的API响应")
@Data
@EqualsAndHashCode(callSuper = true)
public class TokenApiResponse<T> extends ApiResponse<T> {
    @ApiModelProperty("认证Token")
    private String token;

    public TokenApiResponse(Integer code, String msg, T data, String token) {
        super(code, msg, data);
        this.token = token;
    }
}
