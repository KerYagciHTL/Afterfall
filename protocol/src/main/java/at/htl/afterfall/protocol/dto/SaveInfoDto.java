package at.htl.afterfall.protocol.dto;

import java.io.Serializable;

public class SaveInfoDto implements Serializable {
    private static final long serialVersionUID = 1L;

    public int    id;
    public String name;
    public String createdAt;
    public String lastSavedAt;

    public SaveInfoDto() {}

    public SaveInfoDto(int id, String name, String createdAt, String lastSavedAt) {
        this.id          = id;
        this.name        = name;
        this.createdAt   = createdAt;
        this.lastSavedAt = lastSavedAt;
    }
}
