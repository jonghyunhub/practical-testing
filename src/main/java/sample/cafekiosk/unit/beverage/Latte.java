package sample.cafekiosk.unit.beverage;

import sample.cafekiosk.unit.beverage.Beverage;

public class Latte implements Beverage {
    @Override
    public int getPrice() {
        return 4500;
    }

    @Override
    public String getName() {
        return "라떼";
    }
}
