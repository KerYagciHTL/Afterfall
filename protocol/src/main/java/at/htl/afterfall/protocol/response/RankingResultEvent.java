package at.htl.afterfall.protocol.response;

public class RankingResultEvent extends GameResponse {
    private static final long serialVersionUID = 1L;

    public int rank;

    public RankingResultEvent() {}

    public RankingResultEvent(int rank) {
        this.rank = rank;
    }
}
