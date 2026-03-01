package Models;

/*
    This class is the structure of a particular prayer
*/

public class Prayer {
    private int day;
    private String prayer, takenFrom;
    private Boolean isPrayed;

    // Constructor
    public Prayer() {}

    public int getDay() { return day;}

    public void setDay(int day) {
        this.day = day;
    }

    public String getPrayer() {
        return prayer;
    }

    public void setPrayer(String prayer) {
        this.prayer = prayer;
    }

    public String getTakenFrom() {
        return takenFrom;
    }

    public void setTakenFrom(String takenFrom) {
        this.takenFrom = takenFrom;
    }

    public Boolean getIsPrayed() {
        return isPrayed;
    }

    public void setIsPrayed(Boolean isPrayed) {
        this.isPrayed = isPrayed;
    }
}
