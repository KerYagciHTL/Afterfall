package at.htl.afterfall.protocol.response;

import at.htl.afterfall.protocol.dto.SaveInfoDto;

import java.util.List;

public class SaveListResponse extends GameResponse {
    private static final long serialVersionUID = 1L;

    public List<SaveInfoDto> saves;

    public SaveListResponse() {}

    public SaveListResponse(List<SaveInfoDto> saves) {
        this.saves = saves;
    }
}
