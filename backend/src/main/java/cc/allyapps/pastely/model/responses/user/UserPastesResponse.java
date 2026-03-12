package cc.allyapps.pastely.model.responses.user;

import cc.allyapps.pastely.model.responses.folder.FolderResponse;
import cc.allyapps.pastely.model.responses.paste.PasteResponse;

import java.util.List;

public class UserPastesResponse {
    public List<PasteResponse> pastes;
    public List<FolderResponse> folder;
}
