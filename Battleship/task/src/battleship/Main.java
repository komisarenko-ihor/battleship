package battleship;

import java.util.Scanner;

public class Main {
    private static final int FIELD_SIZE_DEFAULT = 10;
    private static final Ship[] SHIPS_1 = {
            new Ship(Name.AIRCRAFT_CARRIER, 5),
            new Ship(Name.BATTLESHIP, 4),
            new Ship(Name.SUBMARINE, 3),
            new Ship(Name.CRUISER, 3),
            new Ship(Name.DESTROYER, 2)
    };
    private static final Ship[] SHIPS_2 = {
            new Ship(Name.AIRCRAFT_CARRIER, 5),
            new Ship(Name.BATTLESHIP, 4),
            new Ship(Name.SUBMARINE, 3),
            new Ship(Name.CRUISER, 3),
            new Ship(Name.DESTROYER, 2)
    };
    private static final String INPUT_PATTERN = "[A-J]([1-9]|10) [A-J]([1-9]|10)";

    private static final int[][] pointShip_1 = new int[FIELD_SIZE_DEFAULT + 1][FIELD_SIZE_DEFAULT + 1];
    private static int restOfShips_1 = SHIPS_1.length;
    private static final int[][] pointShip_2 = new int[FIELD_SIZE_DEFAULT + 1][FIELD_SIZE_DEFAULT + 1];
    private static int restOfShips_2 = SHIPS_2.length;

    public static void main(String[] args) {
        // Write your code here
        Scanner scanner = new Scanner(System.in);
        runGame(scanner);
    }

    public static void runGame(Scanner scanner) {
        String[][] hiddenField_1 = createField();
        printField(hiddenField_1);
        addShipsToField(hiddenField_1, scanner, Player.FIRST);
        String[][] showedField_1 = createField();
        promptEnterKey();

        String[][] hiddenField_2 = createField();
        printField(hiddenField_2);
        addShipsToField(hiddenField_2, scanner, Player.SECOND);
        String[][] showedField_2 = createField();
        promptEnterKey();

        while (true) {
            printField(showedField_2);
            System.out.println("---------------------");
            printField(hiddenField_1);
            System.out.println("Player 1, it's your turn:");
            if (makeShot(scanner, hiddenField_2, showedField_2, Player.FIRST)) {
                break;
            }
            promptEnterKey();
            printField(showedField_1);
            System.out.println("---------------------");
            printField(hiddenField_2);
            System.out.println("Player 2, it's your turn:");
            if (makeShot(scanner, hiddenField_1, showedField_1, Player.SECOND)) {
                break;
            }
            promptEnterKey();
        }
    }

    private static boolean makeShot(Scanner scanner, String[][] hiddenField, String[][] showedField, Player player) {
        boolean allSunk = false;
        while (true) {
            String line = scanner.nextLine();
            Coordinate coordinate = new Coordinate(getIntValue(line.substring(0, 1)), Integer.parseInt(line.substring(1)));
            if (ifCoordinateInField(coordinate)) {
                System.out.println();
                sinkShip(coordinate, hiddenField, showedField, player);
                if ((Player.FIRST == player ? restOfShips_1 : restOfShips_2) == 0) {
                    allSunk = true;
                }
                break;
            } else {
                System.out.println("Error! You entered the wrong coordinates! Try again:");
            }
        }
        return allSunk;
    }

    private static boolean sinkShip(Coordinate coordinate, String[][] showedField, String[][] hiddenField, Player player) {
        boolean result = false;
        String sign = showedField[coordinate.getHorizontal()][coordinate.getVertical()];
        String message;
        int horizontal = coordinate.getHorizontal();
        int vertical = coordinate.getVertical();
        if (Sign.SHIP.getValue().equals(sign) || Sign.HIT.getValue().equals(sign)) {
            message = "You hit a ship!";
            showedField[horizontal][vertical] = Sign.HIT.getValue();
            hiddenField[horizontal][vertical] = Sign.HIT.getValue();

            int num = Player.FIRST == player ? pointShip_2[horizontal][vertical] : pointShip_1[horizontal][vertical];
            Name forOriginal = Name.getByOriginal(num);
            for (Ship ship : Player.FIRST == player ? SHIPS_2 : SHIPS_1) {
                if (ship.getName().equals(forOriginal)) {
                    int restParts = ship.getRestParts();
                    restParts--;
                    ship.setRestParts(restParts);
                    if (restParts == 0) {
                        if (Player.FIRST == player) {
                            restOfShips_1--;
                        } else {
                            restOfShips_2--;
                        }
                        if (restOfShips_1 == 0 || restOfShips_2 == 0) {
                            message = "You sank the last ship. You won. Congratulations!";
                            result = true;
                        } else {
                            message = "You sank a ship!";
                        }
                    }
                }
            }
        } else {
            message = "You missed!";
            showedField[coordinate.getHorizontal()][coordinate.getVertical()] = Sign.MISS.getValue();
            hiddenField[coordinate.getHorizontal()][coordinate.getVertical()] = Sign.MISS.getValue();
        }
        System.out.println(message);
        return result;
    }

    private static boolean ifCoordinateInField(Coordinate... coordinates) {
        boolean result = true;
        for (Coordinate coordinate : coordinates) {
            if (coordinate.getHorizontal() < 1 || coordinate.getHorizontal() > FIELD_SIZE_DEFAULT
                    || coordinate.getVertical() < 1 || coordinate.getVertical() > FIELD_SIZE_DEFAULT) {
                result = false;
                break;
            }
        }
        return result;
    }

    private static void addShipsToField(String[][] field, Scanner scanner, Player player) {
        for (Ship ship : SHIPS_1) {
            System.out.printf("Enter the coordinates of the %s (%d cells):\n", ship.getName().getValue(), ship.getLength());
            while (true) {
                Coordinates coordinates = getCoordinates(scanner);

                switchCoordinatesIfNeeded(coordinates);
                checkCoordinates(coordinates, ship.getLength(), field);
                if (coordinates.getError() != null) {
                    System.out.println(coordinates.getError().getMessage() + " Try again: ");
                } else {
                    addShip(coordinates, field, ship, player);
                    printField(field);
                    break;

                }
            }
        }
    }

    private static void switchCoordinatesIfNeeded(Coordinates coordinates) {
        if (coordinates.getStart().getVertical() == coordinates.getFinish().getVertical()) {
            if (coordinates.getStart().getHorizontal() > coordinates.getFinish().getHorizontal()) {
                switchCoordinates(coordinates);
            }
        } else if (coordinates.getStart().getHorizontal() == coordinates.getFinish().getHorizontal()) {
            if (coordinates.getStart().getVertical() > coordinates.getFinish().getVertical()) {
                switchCoordinates(coordinates);
            }
        }
    }

    private static void switchCoordinates(Coordinates coordinates) {
        Coordinate temp = coordinates.getStart();
        coordinates.setStart(coordinates.getFinish());
        coordinates.setFinish(temp);
    }

    private static void addShip(Coordinates coordinates, String[][] field, Ship ship, Player player) {
        if (coordinates.getStart().getHorizontal() == coordinates.getFinish().getHorizontal()) {
            int x = coordinates.getStart().getHorizontal();
            int y = coordinates.getStart().getVertical();
            for (int i = 0; i < ship.getLength(); i++, y++) {
                field[x][y] = Sign.SHIP.getValue();
                if (Player.FIRST == player) {
                    pointShip_1[x][y] = ship.getName().ordinal();
                } else {
                    pointShip_2[x][y] = ship.getName().ordinal();
                }
            }
        }
        if (coordinates.getStart().getVertical() == coordinates.getFinish().getVertical()) {
            int x = coordinates.getStart().getHorizontal();
            int y = coordinates.getStart().getVertical();
            for (int i = 0; i < ship.getLength(); i++, x++) {
                field[x][y] = Sign.SHIP.getValue();
                if (Player.FIRST == player) {
                    pointShip_1[x][y] = ship.getName().ordinal();
                } else {
                    pointShip_2[x][y] = ship.getName().ordinal();
                }
            }
        }
    }

    private static void checkCoordinates(Coordinates coordinates, int shipLength, String[][] field) {
        int dif = 0;

        if (coordinates.getStart().getHorizontal() == coordinates.getFinish().getHorizontal()) {
            dif = checkCoordinatesIfHorizontalEqual(coordinates, shipLength, field);
        }
        if (coordinates.getStart().getVertical() == coordinates.getFinish().getVertical()) {
            dif = checkCoordinatesIfVerticalEqual(coordinates, shipLength, field);
        }
        if (!ifCoordinateInField(coordinates.getStart(), coordinates.getFinish())) {
            coordinates.setError(new Coordinates.Error("Error! Coordinate is not in field range."));
        }
        if (dif == 0) {
            coordinates.setError(new Coordinates.Error("Error! Ship can be placed horizontal or vertical."));
        }
        if (dif != shipLength) {
            coordinates.setError(new Coordinates.Error("Error! Ship's length doesn't fit."));
        }
    }

    private static int checkCoordinatesIfVerticalEqual(Coordinates coordinates, int shipLength, String[][] field) {
        int x = coordinates.getStart().getHorizontal();
        int y = coordinates.getStart().getVertical();
        int endX = coordinates.getFinish().getHorizontal();
        if (x - 1 > 0 && Sign.SHIP.getValue().equals(field[x - 1][y])) {
            coordinates.setError(new Coordinates.Error("Error! Ship intersections."));
        }
        if (endX + 1 <= FIELD_SIZE_DEFAULT && Sign.SHIP.value.equals(field[endX + 1][y])) {
            coordinates.setError(new Coordinates.Error("Error! Ship intersections."));
        }
        if (coordinates.getError() == null) {
            for (int i = x; i < x + shipLength; i++) {
                if (y - 1 > 0 && Sign.SHIP.getValue().equals(field[x][y - 1])) {
                    coordinates.setError(new Coordinates.Error("Error! Ship intersections."));
                    break;
                }
                if (y + 1 <= FIELD_SIZE_DEFAULT && Sign.SHIP.getValue().equals(field[x][y + 1])) {
                    coordinates.setError(new Coordinates.Error("Error! Ship intersections."));
                    break;
                }
            }
        }
        return coordinates.getFinish().getHorizontal() - coordinates.getStart().getHorizontal() + 1;
    }

    private static int checkCoordinatesIfHorizontalEqual(Coordinates coordinates, int shipLength, String[][] field) {
        int x = coordinates.getStart().getHorizontal();
        int y = coordinates.getStart().getVertical();
        int endY = coordinates.getFinish().getVertical();
        if (y - 1 > 0 && Sign.SHIP.getValue().equals(field[x][y - 1])) {
            coordinates.setError(new Coordinates.Error("Error! Ship intersections."));
        }
        if (endY + 1 <= FIELD_SIZE_DEFAULT && Sign.SHIP.getValue().equals(field[x][endY + 1])) {
            coordinates.setError(new Coordinates.Error("Error! Ship intersections."));
        }
        if (coordinates.getError() == null) {
            for (int i = y; i < y + shipLength; i++) {
                if (x - 1 > 0 && Sign.SHIP.getValue().equals(field[x - 1][y])) {
                    coordinates.setError(new Coordinates.Error("Error! Ship intersections."));
                    break;
                }
                if (x + 1 <= FIELD_SIZE_DEFAULT && Sign.SHIP.getValue().equals(field[x + 1][y])) {
                    coordinates.setError(new Coordinates.Error("Error! Ship intersections."));
                    break;
                }
            }
        }
        return coordinates.getFinish().getVertical() - coordinates.getStart().getVertical() + 1;
    }

    private static Coordinates getCoordinates(Scanner scanner) {
        String line = scanner.nextLine();
        Coordinates coordinates;
        if (line == null || !line.matches(INPUT_PATTERN)) {
            coordinates = new Coordinates(new Coordinates.Error("Error! Format of coordinates"));
        } else {
            String[] arr = line.split(" ");
            coordinates = new Coordinates(
                    new Coordinate(
                            getIntValue(arr[0].substring(0, 1)),
                            Integer.parseInt(arr[0].substring(1))),
                    new Coordinate(
                            getIntValue(arr[1].substring(0, 1)),
                            Integer.parseInt(arr[1].substring(1))));
        }
        return coordinates;
    }

    private static int getIntValue(String val) {
        return val.toCharArray()[0] - 64;
    }

    private static void printField(String[][] field) {
        for (String[] a1 : field) {
            System.out.println(String.join(" ", a1));
        }
    }

    private static String[][] createField() {
        int size = FIELD_SIZE_DEFAULT + 1;
        String[][] result = new String[size][size];
        int firstRow = 1;
        char firstColumn = 65;
        for (int i = 0; i < size; i++) {
            for (int k = 0; k < size; k++) {
                if (i == 0 && k == 0) {
                    result[i][k] = Sign.EMPTY.value;
                    continue;
                }
                if (i == 0) {
                    result[i][k] = String.valueOf(firstRow);
                    firstRow++;
                }
                if (k == 0) {
                    result[i][k] = Character.toString(firstColumn);
                    firstColumn++;
                }
                if (i != 0 && k != 0) {
                    result[i][k] = Sign.SMOG.value;
                }
            }
        }

        return result;
    }

    private static class Coordinates {
        private Coordinate start;
        private Coordinate finish;
        private Error error;

        public Coordinates(Coordinate start, Coordinate finish) {
            this.start = start;
            this.finish = finish;
        }

        public Coordinates(Error error) {
            this.error = error;
        }

        public Coordinate getStart() {
            return start;
        }

        public void setStart(Coordinate start) {
            this.start = start;
        }

        public Coordinate getFinish() {
            return finish;
        }

        public void setFinish(Coordinate finish) {
            this.finish = finish;
        }

        public Error getError() {
            return error;
        }

        public void setError(Error error) {
            this.error = error;
        }

        public static class Error {
            private final String message;

            public Error(String message) {
                this.message = message;
            }

            public String getMessage() {
                return message;
            }
        }
    }

    private static class Coordinate {
        private final int horizontal;
        private final int vertical;

        public Coordinate(int horizontal, int vertical) {
            this.horizontal = horizontal;
            this.vertical = vertical;
        }

        public int getHorizontal() {
            return horizontal;
        }

        public int getVertical() {
            return vertical;
        }
    }

    private enum Sign {
        EMPTY(" "), SMOG("~"), SHIP("O"), HIT("X"), MISS("M");

        String value;

        Sign(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private static class Ship {
        private final Name name;
        private final int length;
        private int restParts;

        public Ship(Name name, int length) {
            this.name = name;
            this.length = length;
            this.restParts = length;
        }

        public Name getName() {
            return name;
        }

        public int getLength() {
            return length;
        }

        public int getRestParts() {
            return restParts;
        }

        public void setRestParts(int restParts) {
            this.restParts = restParts;
        }
    }

    public enum Name {
        AIRCRAFT_CARRIER("Aircraft Carrier"),
        BATTLESHIP("Battleship"),
        SUBMARINE("Submarine"),
        CRUISER("Cruiser"),
        DESTROYER("Destroyer");

        private final String value;

        Name(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Name getByOriginal(int original) {
            Name result = null;
            for (Name value : Name.values()) {
                if (original == value.ordinal()) {
                    result = value;
                }
            }
            return result;
        }
    }

    private enum Player {
        FIRST, SECOND
    }

    private static void promptEnterKey() {
        System.out.println("Press Enter and pass the move to another player");
        try {
            System.in.read();
        } catch (Exception ex) {
            System.out.println("do nothing");
        }
    }
}
