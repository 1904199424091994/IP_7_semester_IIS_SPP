package form;

/**
 * Created by 1 on 16.11.2014.
 */
public class GridData implements Comparable, Cloneable{
    protected boolean grid[][]; //сетка
    protected char symbol; //символ
    public GridData(char symbol, int width, int height) {
        grid = new boolean[width][height];
        this.symbol = symbol;
        System.out.println("ОЧень не нравиться метод compareTo - зачем он?");
    }

    //Высота сетки
    public int getHeight() {
        return grid[0].length;
    }

    //Ширина сетки
    public int getWidth() {
        return grid.length;
    }

    //Получить символ
    public char getSymbol() {
        return symbol;
    }

    //Установить символ
    public void setSymbol(char symbol) {
        this.symbol = symbol;
    }

    //Установить данные в сетку
    public void setDataToGrid(int x, int y, boolean v) {
        grid[x][y]=v;
    }

    //Получить данные с сетки
    public boolean getDataFromGrid(int x,int y) {
        return grid[x][y];
    }

    //Очистить сетку
    public void clear() {
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[0].length; y++) {
                grid[x][y] = false;
            }
        }
    }

    //TODO очень странный метод
    @Override
    public int compareTo(Object o) {
        GridData obj = (GridData)o;
        if (this.getSymbol() > obj.getSymbol())
            return 1;
        else
            return -1;
    }

    @Override
    public String toString() {
        return ""+symbol;
    }

    @Override
    //Клонирование сетки
    public Object clone() {
        GridData obj = new GridData(symbol, getWidth(), getHeight());
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                obj.setDataToGrid(x, y, getDataFromGrid(x, y));
            }
        }
        return obj;
    }
}
