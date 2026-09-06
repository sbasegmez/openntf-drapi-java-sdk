package org.openntf.drapi.meta;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Represents the metadata of a document in the DRAPI system.
 * <p>
 * Form is only returned via /docmeta/{unid} endpoint. That's why we didn't make it a required field in the DocumentMeta record.
 *
 * @param noteId The note ID of the document
 * @param unid The UNID of the document
 * @param created The creation timestamp of the document
 * @param addedToFile The timestamp when the document was added to the file
 * @param lastModified The last modified timestamp of the document
 * @param lastModifiedInFile The last modified timestamp of the document in the file
 * @param lastAccessed The last accessed timestamp of the document
 * @param noteClass The note class of the document
 * @param unread Whether the document is unread
 * @param editable Whether the document is editable
 * @param revision The revision of the document
 * @param etag The ETag of the document
 * @param topLevelChildUnids The top-level child UNIDs of the document
 * @param size The size of the document
 * @param form The form of the document
 */
public record DocumentMeta(
    Long noteId,
    String unid,
    OffsetDateTime created,
    OffsetDateTime addedToFile,
    OffsetDateTime lastModified,
    OffsetDateTime lastModifiedInFile,
    OffsetDateTime lastAccessed,
    List<String> noteClass,
    Boolean unread,
    Boolean editable,
    String revision,
    String etag,
    List<String> topLevelChildUnids,
    Long size,
    String form) {

    public DocumentMeta {
        // Defensive copy and null safety for lists
        topLevelChildUnids = topLevelChildUnids != null ? List.copyOf(topLevelChildUnids) : List.of();
        noteClass = noteClass != null ? List.copyOf(noteClass) : List.of();
    }

}
