package org.notes.model.vo.question;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@ApiModel("题目笔记VO")
@Data
public class QuestionNoteVO {
    @ApiModelProperty("题目标题")
    private String title;

    @ApiModelProperty("问题难度: 1=简单, 2=中等, 3=困难")
    private Integer difficulty;

    @ApiModelProperty("题目考点")
    private String examPoint;

    @ApiModelProperty("浏览量")
    private Integer viewCount;

    @ApiModelProperty("用户笔记信息")
    private UserNote userNote;

    @ApiModel("用户笔记")
    @Data
    public static class UserNote {
        @ApiModelProperty("是否已完成")
        private boolean finished = false;

        @ApiModelProperty("笔记ID")
        private Integer noteId;

        @ApiModelProperty("笔记内容")
        private String content;
    }
}
