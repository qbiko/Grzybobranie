/**
 * Klasa przechowująca wynik danej rozgrywki wraz z imieniem gracza.
 */
public class Score {
    private String name;
    private int points;
    private int level;

    public Score(int level, String name) {
        this.name = name;
        this.points = 0;
        this.level = level;
    }

    public Score(String name, int points) {
        this.name = name;
        this.points = points;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPoints() {
        return points;
    }

    /**
     * Metoda powiększająca ilość zdobytych punktów, zależnie od poziomu gry.
     */
    public void incrementPoints() {
        this.points += level;
    }
}
