package de.interaapps.pastely.model.responses.user;

import de.interaapps.pastely.model.responses.folder.FolderResponse;
import de.interaapps.pastely.model.responses.paste.PasteResponse;

import java.util.List;

public class UserPastesResponse {
    public List<PasteResponse> pastes;
    public List<FolderResponse> folder;
}
