package no.finn.unleash.metric;

class ToggleCount {
    private long yes;
    private long no;

    public ToggleCount() {
        this.yes = 0;
        this.no = 0;
    }

    public void register(boolean active) {
        if(active) {
            yes++;
        } else {
            no++;
        }
    }

    public long getYes() {
        return yes;
    }

    public long getNo() {
        return no;
    }
}
