package org.notes.service;


public interface ElasticsearchSyncService {
    public void syncAllNotes();

    public void syncAllUsers();

    public void syncAll();
}
