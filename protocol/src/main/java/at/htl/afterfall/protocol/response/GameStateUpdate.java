package at.htl.afterfall.protocol.response;

import at.htl.afterfall.protocol.dto.EconomyDto;

import java.io.Serializable;
import java.util.List;

public class GameStateUpdate extends GameResponse {
    private static final long serialVersionUID = 1L;

    public EconomyDto            economy;
    public double                satisfaction;
    public List<TrainPosition>   trainPositions;
    public List<StationWaiting>  stationWaiting;

    public GameStateUpdate() {}

    public GameStateUpdate(EconomyDto economy, double satisfaction,
                           List<TrainPosition> trainPositions,
                           List<StationWaiting> stationWaiting) {
        this.economy        = economy;
        this.satisfaction   = satisfaction;
        this.trainPositions = trainPositions;
        this.stationWaiting = stationWaiting;
    }

    public static class TrainPosition implements Serializable {
        private static final long serialVersionUID = 1L;

        public int     trainId;
        public int     stopIndex;
        public double  position;
        public boolean forward;

        public TrainPosition() {}

        public TrainPosition(int trainId, int stopIndex, double position, boolean forward) {
            this.trainId   = trainId;
            this.stopIndex = stopIndex;
            this.position  = position;
            this.forward   = forward;
        }
    }

    public static class StationWaiting implements Serializable {
        private static final long serialVersionUID = 1L;

        public int stationId;
        public int waitingCount;

        public StationWaiting() {}

        public StationWaiting(int stationId, int waitingCount) {
            this.stationId    = stationId;
            this.waitingCount = waitingCount;
        }
    }
}
