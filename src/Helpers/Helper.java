package Helpers;

import form.GridData;

public class Helper {
    //Выводит GridData на экран
    public static void outGridData(GridData data) {
        for(int i = 0; i < data.getHeight(); i++) {
            System.out.println("");
            for(int j = 0; j < data.getWidth(); j++) {
                System.out.print(data.getDataFromGrid(i,j) ? 1 : 0);
            }
        }
        System.out.println("");
    }

    //Для представления массива типа int[]
    public static void outIntArr(int[] arr) {
        for(int i = 0; i < arr.length; i++) {
            System.out.print(arr[i]);
        }
        System.out.println("");
    }

    //Вывод double[][] на экран
    public static void outDouble2D(double[][] arr) {
        System.out.println("Height = " + arr.length);
        System.out.println("Width = " + arr[0].length);

        for(int i = 0; i < arr.length; i++) {
            System.out.println("");
            for(int j = 0; j < arr[i].length; j++) {
                System.out.print(arr[i][j] + " ");
            }
        }
        System.out.println("");
    }
}
