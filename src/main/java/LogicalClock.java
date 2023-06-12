public class LogicalClock {
    private int value;

    public LogicalClock() {
        this.value = 0;
    }

    public int getTime() {
        return value;
    }

    public void increment() {
        value++;
    }

    public void setValue(int newValue) {
        this.value = newValue;
    }

//    public void update(LogicalClock other) {
//        for(int i = 0; i < clock.length; i++) {
//            clock[i] = Math.max(clock[i], other.clock[i]);
//        }
//    }

//    public boolean happenedBefore(LogicalClock other) {
//        boolean happenedBefore = true;
//        boolean equal = true;
//
//        for(int i = 0; i < clock.length; i++) {
//            if(clock[i] > other.clock[i]) {
//                happenedBefore = false;
//                break;
//            } else if(clock[i] < other.clock[i]) {
//                equal = false;
//            }
//        }
//
//        return !equal && happenedBefore;
//    }

}
