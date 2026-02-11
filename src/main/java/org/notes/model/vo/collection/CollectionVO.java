package org.notes.model.vo.collection;

import lombok.Data;

@Data
public class CollectionVO {
    private Integer collectionId;
    private String name;
    private String description;
    private NoteStatus noteStatus;

    @Data
    public static class NoteStatus {
        private Integer noteId;
        private Boolean isCollected;
    }
}