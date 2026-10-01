DELIMITER $$
CREATE PROCEDURE ensure_note_interaction_constraints()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM (
            SELECT GROUP_CONCAT(column_name ORDER BY seq_in_index) AS columns_in_key
            FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'note_like' AND non_unique = 0
            GROUP BY index_name
        ) AS indexes_found
        WHERE columns_in_key IN ('user_id,note_id', 'note_id,user_id')
    ) THEN
        ALTER TABLE note_like ADD UNIQUE KEY uk_note_like_user_note (user_id, note_id);
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM (
            SELECT GROUP_CONCAT(column_name ORDER BY seq_in_index) AS columns_in_key
            FROM information_schema.statistics
            WHERE table_schema = DATABASE() AND table_name = 'collection_note' AND non_unique = 0
            GROUP BY index_name
        ) AS indexes_found
        WHERE columns_in_key IN ('collection_id,note_id', 'note_id,collection_id')
    ) THEN
        ALTER TABLE collection_note ADD UNIQUE KEY uk_collection_note_collection_note (collection_id, note_id);
    END IF;
END$$
DELIMITER ;
CALL ensure_note_interaction_constraints();
DROP PROCEDURE ensure_note_interaction_constraints;

UPDATE note n SET
    like_count = (SELECT COUNT(*) FROM note_like l WHERE l.note_id = n.note_id),
    collect_count = (
        SELECT COUNT(DISTINCT c.creator_id)
        FROM collection_note cn JOIN collection c ON c.collection_id = cn.collection_id
        WHERE cn.note_id = n.note_id
    ),
    comment_count = (SELECT COUNT(*) FROM comment c WHERE c.note_id = n.note_id);
