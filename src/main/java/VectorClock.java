public class VectorClock {
    private int[] clock;
    public VectorClock(int size) {
        clock = new int[size];
    }

    public void increment(int index) {
        clock[index]++;
    }

    public void update(VectorClock other) {
        for(int i = 0; i < clock.length; i++) {
            clock[i] = Math.max(clock[i], other.clock[i]);
        }
    }

    public boolean happenedBefore(VectorClock other) {
        boolean happenedBefore = true;
        boolean equal = true;

        for(int i = 0; i < clock.length; i++) {
            if(clock[i] > other.clock[i]) {
                happenedBefore = false;
                break;
            } else if(clock[i] < other.clock[i]) {
                equal = false;
            }
        }

        return !equal && happenedBefore;
    }

    public int getTime(int index) {
        return clock[index];
    }
}
