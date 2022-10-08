package org.petrushin.graphics.figure;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FiguresReader {

    public static Figure readFigureFromFile(String fileName){
        Figure fig = new Figure();
        try {
            File file = new File(fileName);
            Scanner scanner = new Scanner(file);
            String str[];
            List<Dot> dots = new ArrayList<>();
            while(scanner.hasNextLine()){
                str = scanner.nextLine().split("\\s+");
                if(str[0].equals("v")){
                    double x = Double.parseDouble( str[1]);
                    double y = Double.parseDouble( str[2]);
                    double z = Double.parseDouble( str[3]);
                    System.out.println(x);
                    dots.add(new Dot(x, y, z));
                }
                if(str[0].equals("f")){
                    fig.addTriangle(new Triangle(dots.get(Integer.parseInt(str[1]) - 1), dots.get(Integer.parseInt(str[2]) - 1), dots.get(Integer.parseInt(str[3]) - 1)));
                }
            }
            return fig;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("File has not been read");
        return null;
    }


}
