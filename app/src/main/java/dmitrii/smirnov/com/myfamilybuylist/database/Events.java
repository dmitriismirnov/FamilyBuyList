package dmitrii.smirnov.com.myfamilybuylist.database;

/**
 * Created by Дмитрий on 23.04.2017.
 */

public class Events {
    private String fromUserId, toUserId, message;
    private int typeOfEvent;
    private boolean solved;

    public final static int FRIEND_INVITE = 1, SIMPLE_MESSAGE = 2;

    public Events() {
    }

    public Events(String fromUserId, String toUserId, int typeOfEvent){
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.typeOfEvent = typeOfEvent;
        this.solved = false;

        switch (typeOfEvent){
            case FRIEND_INVITE:
                this.message = "Friend request";
                break;
            default:
                this.message = "no message yet for Event Type #"+typeOfEvent;
                break;
        }

    }

    public Events(String fromUserId, String toUserId, String message, int typeOfEvent, boolean solved) {
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.message = message;
        this.typeOfEvent = typeOfEvent;
        this.solved = solved;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getTypeOfEvent() {
        return typeOfEvent;
    }

    public void setTypeOfEvent(int typeOfEvent) {
        this.typeOfEvent = typeOfEvent;
    }

    public boolean isSolved() {
        return solved;
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
    }

    @Override
    public String toString() {
        return "Events{" +
                "fromUserId='" + fromUserId + '\'' +
                ", toUserId='" + toUserId + '\'' +
                ", message='" + message + '\'' +
                ", typeOfEvent=" + typeOfEvent +
                ", solved=" + solved +
                '}';
    }
}
