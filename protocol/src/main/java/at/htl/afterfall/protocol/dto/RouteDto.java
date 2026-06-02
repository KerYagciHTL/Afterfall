package at.htl.afterfall.protocol.dto;

import java.io.Serializable;
import java.util.List;

public class RouteDto implements Serializable {
    private static final long serialVersionUID = 1L;

    public int          id;
    public String       name;
    public String       colorHex;
    public boolean      active;
    public boolean      circular;
    public List<Integer> stopIds;
    public List<Integer> trainIds;

    public RouteDto() {}

    public RouteDto(int id, String name, String colorHex, boolean active,
                    boolean circular, List<Integer> stopIds, List<Integer> trainIds) {
        this.id       = id;
        this.name     = name;
        this.colorHex = colorHex;
        this.active   = active;
        this.circular = circular;
        this.stopIds  = stopIds;
        this.trainIds = trainIds;
    }
}
